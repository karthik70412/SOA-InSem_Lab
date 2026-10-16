package com.klu.studentservices.controller;

import com.klu.studentservices.dto.OrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Order Created Successfully");
        response.put("customerName", orderRequest.getCustomerName());
        response.put("productName", orderRequest.getProductName());
        response.put("quantity", orderRequest.getQuantity());
        response.put("price", orderRequest.getPrice());

        return ResponseEntity.ok(response);
    }
}
