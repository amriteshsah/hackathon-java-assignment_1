package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.OptimisticLockException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOG = Logger.getLogger(WarehouseRepository.class);

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  public List<Warehouse> search(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      int page,
      int pageSize) {
    StringBuilder query = new StringBuilder("archivedAt is null");
    Map<String, Object> parameters = new HashMap<>();

    if (location != null && !location.isBlank()) {
      query.append(" and location = :location");
      parameters.put("location", location);
    }

    if (minCapacity != null) {
      query.append(" and capacity >= :minCapacity");
      parameters.put("minCapacity", minCapacity);
    }

    if (maxCapacity != null) {
      query.append(" and capacity <= :maxCapacity");
      parameters.put("maxCapacity", maxCapacity);
    }

    String sortField = "capacity".equals(sortBy) ? "capacity" : "createdAt";
    Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.Descending : Sort.Direction.Ascending;

    LOG.infov(
        "Searching warehouses [location={0}, minCapacity={1}, maxCapacity={2}, sortBy={3}, sortOrder={4}, page={5}, pageSize={6}]",
        location,
        minCapacity,
        maxCapacity,
        sortField,
        direction,
        page,
        pageSize);

    return find(query.toString(), Sort.by(sortField, direction), parameters)
        .page(page, pageSize)
        .list()
        .stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
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

    LOG.infov("Created warehouse with business unit code {0}", warehouse.businessUnitCode);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse current = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (current == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
    }

    if (warehouse.version != null && !current.version.equals(warehouse.version)) {
      throw new OptimisticLockException(
          "Warehouse with business unit code '"
              + warehouse.businessUnitCode
              + "' was concurrently modified");
    }

    current.location = warehouse.location;
    current.capacity = warehouse.capacity;
    current.stock = warehouse.stock;
    current.archivedAt = warehouse.archivedAt;

    this.persistAndFlush(current);

    warehouse.version = current.version;
    LOG.infov("Updated warehouse with business unit code {0}", warehouse.businessUnitCode);
  }

  @Override
  public void remove(Warehouse warehouse) {
    delete("businessUnitCode", warehouse.businessUnitCode);
    LOG.infov("Removed warehouse with business unit code {0}", warehouse.businessUnitCode);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}
