package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class WarehouseSearchResourceTest {

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setUp() {
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();

    persistWarehouse("SEARCH-001", "AMSTERDAM-001", 50, 20, null);
    persistWarehouse("SEARCH-002", "AMSTERDAM-001", 80, 30, null);
    persistWarehouse("SEARCH-003", "ZWOLLE-001", 30, 10, null);
    persistWarehouse("SEARCH-ARCHIVED", "AMSTERDAM-001", 90, 50, java.time.LocalDateTime.now());
  }

  @Test
  void shouldFilterByLocationAndCapacity() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .queryParam("minCapacity", 60)
        .when()
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("businessUnitCode", hasSize(1))
        .body("businessUnitCode[0]", equalTo("SEARCH-002"));
  }

  @Test
  void shouldExcludeArchivedWarehouses() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .when()
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("businessUnitCode", hasSize(2));
  }

  @Test
  void shouldSortAndPaginateResults() {
    given()
        .queryParam("sortBy", "capacity")
        .queryParam("sortOrder", "desc")
        .queryParam("page", 0)
        .queryParam("pageSize", 2)
        .when()
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("businessUnitCode", hasSize(2))
        .body("businessUnitCode[0]", equalTo("SEARCH-002"))
        .body("businessUnitCode[1]", equalTo("SEARCH-001"));
  }

  @Test
  void shouldRejectInvalidCapacityRange() {
    given()
        .queryParam("minCapacity", 50)
        .queryParam("maxCapacity", 10)
        .when()
        .get("/warehouse/search")
        .then()
        .statusCode(400);
  }

  private void persistWarehouse(
      String businessUnitCode,
      String location,
      int capacity,
      int stock,
      java.time.LocalDateTime archivedAt) {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = java.time.LocalDateTime.now();
    warehouse.archivedAt = archivedAt;
    em.persist(warehouse);
  }
}
