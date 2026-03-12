package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class StoreModelAndEventTest {

  @Test
  void shouldInitializeStoreWithNameConstructor() {
    Store store = new Store("Main Street");

    assertEquals("Main Street", store.name);
    assertNull(store.quantityProductsInStock);
  }

  @Test
  void shouldExposeStoreFromCreatedAndUpdatedEvents() {
    Store store = new Store("Downtown");

    StoreCreatedEvent createdEvent = new StoreCreatedEvent(store);
    StoreUpdatedEvent updatedEvent = new StoreUpdatedEvent(store);

    assertSame(store, createdEvent.getStore());
    assertSame(store, updatedEvent.getStore());
  }
}
