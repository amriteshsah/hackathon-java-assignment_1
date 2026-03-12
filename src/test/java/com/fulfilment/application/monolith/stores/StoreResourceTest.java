package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

  @Test
  void shouldCreateUpdatePatchAndDeleteStore() {
    String uniqueName = "StoreResourceTest_" + System.currentTimeMillis();

    Long storeId =
        given()
            .contentType("application/json")
            .body("{\"name\":\"" + uniqueName + "\",\"quantityProductsInStock\":5}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo(uniqueName))
            .extract()
            .path("id");

    given()
        .when()
        .get("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("name", equalTo(uniqueName))
        .body("quantityProductsInStock", equalTo(5));

    given()
        .contentType("application/json")
        .body("{\"quantityProductsInStock\":10}")
        .when()
        .put("/store/" + storeId)
        .then()
        .statusCode(422);

    given()
        .contentType("application/json")
        .body("{\"name\":\"" + uniqueName + "_updated\",\"quantityProductsInStock\":12}")
        .when()
        .put("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("name", equalTo(uniqueName + "_updated"))
        .body("quantityProductsInStock", equalTo(12));

    given()
        .contentType("application/json")
        .body("{\"quantityProductsInStock\":20}")
        .when()
        .patch("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("name", equalTo(uniqueName + "_updated"))
        .body("quantityProductsInStock", equalTo(20));

    given().when().delete("/store/" + storeId).then().statusCode(204);

    given().when().get("/store/" + storeId).then().statusCode(404);
  }

  @Test
  void shouldRejectCreateWhenIdProvided() {
    given()
        .contentType("application/json")
        .body("{\"id\":99,\"name\":\"invalid\",\"quantityProductsInStock\":1}")
        .when()
        .post("/store")
        .then()
        .statusCode(422);
  }

  @Test
  void shouldReturnNotFoundForUnknownStoreOnPatchAndDelete() {
    given()
        .contentType("application/json")
        .body("{\"name\":\"does-not-exist\"}")
        .when()
        .patch("/store/999999")
        .then()
        .statusCode(404);

    given().when().delete("/store/999999").then().statusCode(404);
  }
}
