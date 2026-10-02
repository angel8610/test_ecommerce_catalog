package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.application.model.Product;

import java.util.List;

public interface ExternalCatalogPort {

  List<Product> fetchAllProducts();


}
