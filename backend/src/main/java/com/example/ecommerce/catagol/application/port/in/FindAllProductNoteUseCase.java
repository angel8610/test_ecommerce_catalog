package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.domain.model.ProductNote;

import java.util.List;

public interface FindAllProductNoteUseCase {

  List<ProductNote> findAll();


}
