package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
@Path("/warehouse/search")
@Produces(MediaType.APPLICATION_JSON)
public class WarehouseSearchResource {

  private static final Logger LOG = Logger.getLogger(WarehouseSearchResource.class);

  @Inject WarehouseRepository warehouseRepository;

  @GET
  public List<Warehouse> search(
      @QueryParam("location") String location,
      @QueryParam("minCapacity") Integer minCapacity,
      @QueryParam("maxCapacity") Integer maxCapacity,
      @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
      @QueryParam("sortOrder") @DefaultValue("asc") String sortOrder,
      @QueryParam("page") @DefaultValue("0") @Min(0) int page,
      @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int pageSize) {

    if (!"createdAt".equals(sortBy) && !"capacity".equals(sortBy)) {
      throw new WebApplicationException("sortBy must be either 'createdAt' or 'capacity'", 400);
    }

    if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
      throw new WebApplicationException("sortOrder must be either 'asc' or 'desc'", 400);
    }

    if (minCapacity != null && maxCapacity != null && minCapacity > maxCapacity) {
      throw new WebApplicationException("minCapacity must be less than or equal to maxCapacity", 400);
    }

    LOG.infov(
        "Warehouse search request received [location={0}, minCapacity={1}, maxCapacity={2}, sortBy={3}, sortOrder={4}, page={5}, pageSize={6}]",
        location,
        minCapacity,
        maxCapacity,
        sortBy,
        sortOrder,
        page,
        pageSize);

    return warehouseRepository.search(location, minCapacity, maxCapacity, sortBy, sortOrder, page, pageSize)
        .stream()
        .map(this::toWarehouseResponse)
        .toList();
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    Warehouse response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }
}
