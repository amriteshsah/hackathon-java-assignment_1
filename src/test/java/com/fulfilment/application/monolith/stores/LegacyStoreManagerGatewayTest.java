package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

  @Test
  void shouldCreateAndUpdateOnLegacySystemWithoutThrowing() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("LegacyGatewayTest_" + System.nanoTime());
    store.quantityProductsInStock = 12;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  void shouldSwallowFileErrorsWhenStoreDataIsInvalid() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store invalidStore = new Store();
    invalidStore.quantityProductsInStock = 1;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(invalidStore));
  }
}
