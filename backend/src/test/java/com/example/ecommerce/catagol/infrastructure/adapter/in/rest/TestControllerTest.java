package com.example.ecommerce.catagol.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TestControllerTest {

  private TestController testController;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    testController = new TestController();
    mockMvc = standaloneSetup(testController).build();
  }

  @Test
  void returnsSuccessWhenTestMethodIsCalled() {
    assertEquals("Success", testController.test());
  }

  @Test
  void returnsSuccessFromTestEndpointOverHttp() throws Exception {
    mockMvc.perform(get("/api/v1/test"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
      .andExpect(content().string("Success"));
  }


}
