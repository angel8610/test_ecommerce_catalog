package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.application.model.ProductCatalogItem;

import java.util.List;

public interface GetProductUseCase {

  List<ProductCatalogItem> getEnrichedCatalog();


}
