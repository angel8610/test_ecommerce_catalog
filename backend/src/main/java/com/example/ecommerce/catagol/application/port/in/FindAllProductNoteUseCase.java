package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;

import java.util.List;

public interface FindAllProductNoteUseCase {

  List<ProductNoteResponse> findAll();


}
