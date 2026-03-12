package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class ProductResourceErrorMapperTest {

  @Test
  void shouldMapGenericExceptionToInternalServerErrorJson() {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new RuntimeException("boom"));

    assertEquals(500, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertNotNull(body);
    assertEquals(RuntimeException.class.getName(), body.get("exceptionType").asText());
    assertEquals(500, body.get("code").asInt());
    assertEquals("boom", body.get("error").asText());
  }

  @Test
  void shouldMapWebApplicationExceptionStatusAndMessage() {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new WebApplicationException("missing", 404));

    assertEquals(404, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(WebApplicationException.class.getName(), body.get("exceptionType").asText());
    assertEquals(404, body.get("code").asInt());
    assertEquals("missing", body.get("error").asText());
  }
}
