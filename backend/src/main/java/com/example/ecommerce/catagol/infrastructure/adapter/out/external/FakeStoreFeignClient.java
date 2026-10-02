package com.example.ecommerce.catagol.infrastructure.adapter.out.external;

import com.example.ecommerce.catagol.application.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "fakeStoreClient", url = "${external.api.fake-store.url}")
public interface FakeStoreFeignClient {

  @GetMapping("/products")
  List<Product> getAllProducts();


}
