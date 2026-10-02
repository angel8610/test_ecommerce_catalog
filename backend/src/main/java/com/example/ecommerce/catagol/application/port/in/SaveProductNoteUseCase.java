package com.example.ecommerce.catagol.application.port.in;

import com.example.ecommerce.catagol.domain.model.ProductNote;

public interface SaveProductNoteUseCase {

  ProductNote saveProductNote(ProductNoteCreateCommand productNoteCreateCommand);


}
