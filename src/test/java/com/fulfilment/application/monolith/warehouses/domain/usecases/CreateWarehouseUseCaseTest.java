package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldRejectNullWarehouse() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> useCase.create(null));

    assertTrue(exception.getMessage().contains("cannot be null"));
    verifyNoInteractions(warehouseStore, locationResolver);
  }

  @Test
  void shouldRejectNegativeCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = -1;
    warehouse.stock = 0;

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("capacity"));
    verifyNoInteractions(warehouseStore, locationResolver);
  }

  @Test
  void shouldRejectNegativeStock() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 10;
    warehouse.stock = -1;

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("stock"));
    verifyNoInteractions(warehouseStore, locationResolver);
  }

  @Test
  void shouldRejectBlankBusinessUnitCode() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "   ";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 10;
    warehouse.stock = 5;

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("Business unit code"));
    verifyNoInteractions(warehouseStore, locationResolver);
  }

  @Test
  void shouldRejectBlankLocation() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "";
    warehouse.capacity = 10;
    warehouse.stock = 5;

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("Location"));
    verifyNoInteractions(warehouseStore, locationResolver);
  }

  @Test
  void shouldRejectCapacityOverLocationLimit() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 101;
    warehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("BU-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 10, 100));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("exceeds location max capacity"));
  }
}
