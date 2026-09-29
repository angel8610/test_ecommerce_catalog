package com.example.ecommerce.catagol.infrastructure.adapter.in.rest.dto;

public record ProductNoteResponse(

  Long noteId,
  Long extProdId,
  String note,
  String createdBy


) {
}
