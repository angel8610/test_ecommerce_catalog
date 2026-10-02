package com.example.ecommerce.catagol.application.port.in;

public record ProductNoteCreateCommand(

  Long extProdId,
  String note,
  String createdBy


) {
}
