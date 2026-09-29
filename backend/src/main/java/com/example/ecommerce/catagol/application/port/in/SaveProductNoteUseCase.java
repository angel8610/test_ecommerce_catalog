package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteRequest;
import com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto.ProductNoteResponse;

public interface SaveProductNoteUseCase {

  ProductNoteResponse saveProductNote(ProductNoteRequest productNoteRequest);


}
