package com.example.ecommerce.catagol.application.port.out;

import com.example.ecommerce.catagol.domain.model.ProductNote;

import java.util.List;

public interface ProductNoteRepositoryPort {

  ProductNote save(ProductNote productNote);

  List<ProductNote> findAll();


}
