package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOG = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOG.infov("Replacing warehouse with business unit code {0}", newWarehouse.businessUnitCode);

    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '"
              + newWarehouse.businessUnitCode
              + "' does not exist");
    }

    if (existing.archivedAt != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '"
              + newWarehouse.businessUnitCode
              + "' is archived and cannot be replaced");
    }

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Location '" + newWarehouse.location + "' is not valid");
    }

    if (newWarehouse.capacity > location.maxCapacity()) {
      throw new IllegalArgumentException(
          "Warehouse capacity ("
              + newWarehouse.capacity
              + ") exceeds location max capacity ("
              + location.maxCapacity()
              + ")");
    }

    if (newWarehouse.stock > newWarehouse.capacity) {
      throw new IllegalArgumentException(
          "Warehouse stock ("
              + newWarehouse.stock
              + ") exceeds warehouse capacity ("
              + newWarehouse.capacity
              + ")");
    }

    existing.location = newWarehouse.location;
    existing.capacity = newWarehouse.capacity;
    existing.stock = newWarehouse.stock;

    warehouseStore.update(existing);
    LOG.infov("Warehouse {0} replaced successfully", newWarehouse.businessUnitCode);
  }
}
