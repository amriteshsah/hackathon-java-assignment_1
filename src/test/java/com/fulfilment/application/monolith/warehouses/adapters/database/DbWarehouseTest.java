package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DbWarehouseTest {

  @Test
  void shouldMapEntityToDomainWarehouse() {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = "BU-123";
    dbWarehouse.location = "AMSTERDAM-001";
    dbWarehouse.capacity = 120;
    dbWarehouse.stock = 45;
    dbWarehouse.createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
    dbWarehouse.archivedAt = LocalDateTime.of(2025, 2, 1, 11, 0);
    dbWarehouse.version = 7L;

    var warehouse = dbWarehouse.toWarehouse();

    assertEquals("BU-123", warehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouse.location);
    assertEquals(120, warehouse.capacity);
    assertEquals(45, warehouse.stock);
    assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), warehouse.createdAt);
    assertEquals(LocalDateTime.of(2025, 2, 1, 11, 0), warehouse.archivedAt);
    assertEquals(7L, warehouse.version);
  }
}
