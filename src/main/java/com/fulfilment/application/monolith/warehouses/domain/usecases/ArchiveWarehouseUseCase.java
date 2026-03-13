package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOG = Logger.getLogger(ArchiveWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public Warehouse archive(Warehouse warehouse) {
    LOG.infov("Archiving warehouse with business unit code {0}", warehouse.businessUnitCode);

    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
    }

    if (existing.archivedAt != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '"
              + warehouse.businessUnitCode
              + "' is already archived");
    }

    existing.archivedAt = java.time.LocalDateTime.now();
    warehouseStore.update(existing);

    LOG.infov("Warehouse {0} archived successfully", warehouse.businessUnitCode);
    return existing;
  }
}
