package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  void shouldResolveExistingLocation() {
    var location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identifier());
    assertEquals(1, location.maxNumberOfWarehouses());
    assertEquals(40, location.maxCapacity());
  }

  @Test
  void shouldReturnNullForUnknownLocation() {
    assertNull(locationGateway.resolveByIdentifier("UNKNOWN-001"));
  }
}
