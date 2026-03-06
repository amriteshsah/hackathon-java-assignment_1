package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.OptimisticLockException;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persistAndFlush(dbWarehouse);
    warehouse.version = dbWarehouse.version;
  }

  @Override
  public void update(Warehouse warehouse) {
    Long versionToUse = warehouse.version;
    if (versionToUse == null) {
      DbWarehouse current = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
      if (current == null) {
        throw new IllegalArgumentException(
            "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
      }
      versionToUse = current.version;
    }

    int updatedRows =
        getEntityManager()
            .createQuery(
                "UPDATE DbWarehouse w SET w.location = :loc, w.capacity = :cap, "
                    + "w.stock = :stock, w.archivedAt = :archived, w.version = w.version + 1 "
                    + "WHERE w.businessUnitCode = :code AND w.version = :version")
            .setParameter("loc", warehouse.location)
            .setParameter("cap", warehouse.capacity)
            .setParameter("stock", warehouse.stock)
            .setParameter("archived", warehouse.archivedAt)
            .setParameter("code", warehouse.businessUnitCode)
            .setParameter("version", versionToUse)
            .executeUpdate();

    if (updatedRows == 0) {
      throw new OptimisticLockException(
          "Warehouse with business unit code '"
              + warehouse.businessUnitCode
              + "' was concurrently modified");
    }

    warehouse.version = versionToUse + 1;
  }

  @Override
  public void remove(Warehouse warehouse) {
    delete("businessUnitCode", warehouse.businessUnitCode);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}
