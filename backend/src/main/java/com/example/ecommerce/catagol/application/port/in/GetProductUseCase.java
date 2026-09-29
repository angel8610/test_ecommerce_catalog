package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductResponse;

import java.util.List;

public interface GetProductUseCase {

  List<ProductResponse> getEnrichedCatalog();


}
