package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testCrudProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void shouldRejectCreateWhenIdProvided() {
    given()
        .contentType("application/json")
        .body("{\"id\":99,\"name\":\"invalid-product\",\"stock\":1}")
        .when()
        .post("/product")
        .then()
        .statusCode(422);
  }

  @Test
  public void shouldRejectUpdateWhenNameMissing() {
    String uniqueName = "ProductEndpointTestUpdate_" + System.currentTimeMillis();

    Number productId =
        given()
            .contentType("application/json")
            .body("{\"name\":\"" + uniqueName + "\",\"stock\":2}")
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .contentType("application/json")
        .body("{\"description\":\"no-name\",\"stock\":4}")
        .when()
        .put("/product/" + productId.longValue())
        .then()
        .statusCode(422);
  }

  @Test
  public void shouldReturnNotFoundForUnknownProduct() {
    given().when().get("/product/999999").then().statusCode(404);

    given()
        .contentType("application/json")
        .body("{\"name\":\"missing\",\"stock\":1}")
        .when()
        .put("/product/999999")
        .then()
        .statusCode(404);

    given().when().delete("/product/999999").then().statusCode(404);
  }
}
