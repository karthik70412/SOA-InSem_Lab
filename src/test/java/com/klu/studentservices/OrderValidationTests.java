package com.klu.studentservices;

import com.klu.studentservices.controller.OrderController;
import com.klu.studentservices.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class OrderValidationTests {

    @Autowired
    private OrderController orderController;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    @DisplayName("Test Case 1: Valid Order - HTTP 200 with Order Created Successfully")
    void testValidOrder() throws Exception {
        String payload = """
                {
                  "customerName": "John",
                  "productName": "Laptop",
                  "quantity": 2,
                  "price": 50000.0
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Order Created Successfully")))
                .andExpect(jsonPath("$.customerName", is("John")))
                .andExpect(jsonPath("$.productName", is("Laptop")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.price", is(50000.0)));
    }

    @Test
    @DisplayName("Test Case 2: Missing Customer Name - HTTP 400 with customerName error")
    void testMissingCustomerName() throws Exception {
        String payload = """
                {
                  "customerName": "",
                  "productName": "Laptop",
                  "quantity": 2,
                  "price": 50000.0
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.customerName", is("Customer name cannot be blank")));
    }

    @Test
    @DisplayName("Test Case 3: Invalid Quantity - HTTP 400 with quantity error")
    void testInvalidQuantity() throws Exception {
        String payload = """
                {
                  "customerName": "John",
                  "productName": "Laptop",
                  "quantity": -2,
                  "price": 50000.0
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.quantity", is("Quantity must be positive")));
    }

    @Test
    @DisplayName("Test Case 4: Negative Price - HTTP 400 with price error")
    void testNegativePrice() throws Exception {
        String payload = """
                {
                  "customerName": "John",
                  "productName": "Laptop",
                  "quantity": 2,
                  "price": -500.0
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.price", is("Price must be positive")));
    }

    @Test
    @DisplayName("Test Case 5: Missing Product Name - HTTP 400 with productName error")
    void testMissingProductName() throws Exception {
        String payload = """
                {
                  "customerName": "John",
                  "productName": "",
                  "quantity": 2,
                  "price": 50000.0
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.productName", is("Product name cannot be blank")));
    }

    @Test
    @DisplayName("Multiple Validation Errors - HTTP 400 with all relevant error messages")
    void testMultipleValidationErrors() throws Exception {
        String payload = """
                {
                  "customerName": "",
                  "productName": "",
                  "quantity": -1,
                  "price": -100.0
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.customerName", is("Customer name cannot be blank")))
                .andExpect(jsonPath("$.errors.productName", is("Product name cannot be blank")))
                .andExpect(jsonPath("$.errors.quantity", is("Quantity must be positive")))
                .andExpect(jsonPath("$.errors.price", is("Price must be positive")));
    }
}
