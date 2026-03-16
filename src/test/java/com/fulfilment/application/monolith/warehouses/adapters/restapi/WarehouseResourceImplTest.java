package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class WarehouseResourceImplTest {

  private WarehouseRepository warehouseRepository;
  private CreateWarehouseOperation createWarehouseOperation;
  private ArchiveWarehouseOperation archiveWarehouseOperation;
  private ReplaceWarehouseOperation replaceWarehouseOperation;

  private WarehouseResourceImpl resource;

  @BeforeEach
  void setUp() throws Exception {
    warehouseRepository = Mockito.mock(WarehouseRepository.class);
    createWarehouseOperation = Mockito.mock(CreateWarehouseOperation.class);
    archiveWarehouseOperation = Mockito.mock(ArchiveWarehouseOperation.class);
    replaceWarehouseOperation = Mockito.mock(ReplaceWarehouseOperation.class);

    resource = new WarehouseResourceImpl();
    inject(resource, "warehouseRepository", warehouseRepository);
    inject(resource, "createWarehouseOperation", createWarehouseOperation);
    inject(resource, "archiveWarehouseOperation", archiveWarehouseOperation);
    inject(resource, "replaceWarehouseOperation", replaceWarehouseOperation);
  }

  @Test
  void shouldListAllWarehousesUnits() {
    var warehouse = domainWarehouse("BU-1", "UTRECHT", 100, 5);
    when(warehouseRepository.getAll()).thenReturn(List.of(warehouse));

    var result = resource.listAllWarehousesUnits();

    assertEquals(1, result.size());
    assertEquals("BU-1", result.get(0).getBusinessUnitCode());
    assertEquals("UTRECHT", result.get(0).getLocation());
    assertEquals(100, result.get(0).getCapacity());
    assertEquals(5, result.get(0).getStock());
  }

  @Test
  void shouldCreateNewWarehouseUnitAndDefaultStockToZero() {
    Warehouse request = new Warehouse();
    request.setBusinessUnitCode("BU-2");
    request.setLocation("EINDHOVEN");
    request.setCapacity(120);

    Warehouse created = resource.createANewWarehouseUnit(request);

    ArgumentCaptor<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> captor =
        ArgumentCaptor.forClass(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);
    verify(createWarehouseOperation).create(captor.capture());
    var captured = captor.getValue();

    assertEquals("BU-2", captured.businessUnitCode);
    assertEquals("EINDHOVEN", captured.location);
    assertEquals(120, captured.capacity);
    assertEquals(0, captured.stock);

    assertEquals("BU-2", created.getBusinessUnitCode());
    assertEquals("EINDHOVEN", created.getLocation());
    assertEquals(120, created.getCapacity());
    assertEquals(0, created.getStock());
  }

  @Test
  void shouldReturnBadRequestWhenCreateValidationFails() {
    Warehouse request = new Warehouse();
    request.setBusinessUnitCode("BAD");
    when(createWarehouseOperation.create(any()))
        .thenThrow(new IllegalArgumentException("invalid warehouse"));

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(request));

    assertEquals(400, exception.getResponse().getStatus());
    assertEquals("invalid warehouse", exception.getMessage());
  }

  @Test
  void shouldGetWarehouseByBusinessUnitCode() {
    when(warehouseRepository.findByBusinessUnitCode("BU-3"))
        .thenReturn(domainWarehouse("BU-3", "ROTTERDAM", 90, 11));

    Warehouse result = resource.getAWarehouseUnitByID("BU-3");

    assertEquals("BU-3", result.getBusinessUnitCode());
    assertEquals("ROTTERDAM", result.getLocation());
    assertEquals(90, result.getCapacity());
    assertEquals(11, result.getStock());
  }

  @Test
  void shouldReturnNotFoundWhenWarehouseDoesNotExistForGet() {
    when(warehouseRepository.findByBusinessUnitCode("UNKNOWN")).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("UNKNOWN"));

    assertEquals(404, exception.getResponse().getStatus());
    assertEquals("Warehouse with business unit code 'UNKNOWN' not found", exception.getMessage());
  }

  @Test
  void shouldArchiveWarehouse() {
    when(warehouseRepository.findByBusinessUnitCode("BU-4"))
        .thenReturn(domainWarehouse("BU-4", "BREDA", 70, 12));

    resource.archiveAWarehouseUnitByID("BU-4");

    verify(archiveWarehouseOperation).archive(any());
  }

  @Test
  void shouldReturnNotFoundWhenWarehouseDoesNotExistForArchive() {
    when(warehouseRepository.findByBusinessUnitCode("NOPE")).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("NOPE"));

    assertEquals(404, exception.getResponse().getStatus());
    assertEquals("Warehouse with business unit code 'NOPE' not found", exception.getMessage());
  }

  @Test
  void shouldReturnBadRequestWhenArchiveValidationFails() {
    when(warehouseRepository.findByBusinessUnitCode("BU-5"))
        .thenReturn(domainWarehouse("BU-5", "DELFT", 70, 12));
    Mockito.doThrow(new IllegalArgumentException("cannot archive"))
        .when(archiveWarehouseOperation)
        .archive(any());

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("BU-5"));

    assertEquals(400, exception.getResponse().getStatus());
    assertEquals("cannot archive", exception.getMessage());
  }

  @Test
  void shouldReplaceCurrentActiveWarehouseAndUsePathBusinessUnitCode() {
    Warehouse request = new Warehouse();
    request.setBusinessUnitCode("SHOULD_NOT_BE_USED");
    request.setLocation("ARNHEM");
    request.setCapacity(140);

    when(warehouseRepository.findByBusinessUnitCode("BU-6"))
        .thenReturn(domainWarehouse("BU-6", "ARNHEM", 140, 0));

    Warehouse result = resource.replaceTheCurrentActiveWarehouse("BU-6", request);

    ArgumentCaptor<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> captor =
        ArgumentCaptor.forClass(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);
    verify(replaceWarehouseOperation).replace(captor.capture());
    var captured = captor.getValue();

    assertEquals("BU-6", captured.businessUnitCode);
    assertEquals("ARNHEM", captured.location);
    assertEquals(140, captured.capacity);
    assertEquals(0, captured.stock);

    assertNotNull(result);
    assertEquals("BU-6", result.getBusinessUnitCode());
    assertEquals("ARNHEM", result.getLocation());
    assertEquals(140, result.getCapacity());
    assertEquals(0, result.getStock());
  }

  @Test
  void shouldReturnBadRequestWhenReplaceValidationFails() {
    Warehouse request = new Warehouse();
    request.setLocation("LEIDEN");
    request.setCapacity(10);

    Mockito.doThrow(new IllegalArgumentException("cannot replace"))
        .when(replaceWarehouseOperation)
        .replace(any());

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () -> resource.replaceTheCurrentActiveWarehouse("BU-7", request));

    assertEquals(400, exception.getResponse().getStatus());
    assertEquals("cannot replace", exception.getMessage());
  }

  private static com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse(
      String businessUnitCode, String location, Integer capacity, Integer stock) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private static void inject(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
