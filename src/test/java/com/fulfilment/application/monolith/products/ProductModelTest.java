package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ProductModelTest {

  @Test
  void shouldInitializeProductWithNameConstructor() {
    Product product = new Product("Lamp");

    assertEquals("Lamp", product.name);
    assertNull(product.id);
    assertNull(product.description);
  }
}
