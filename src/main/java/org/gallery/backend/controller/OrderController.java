package org.gallery.backend.controller;

import jakarta.transaction.Transactional;
import org.gallery.backend.dto.OrderDto;
import org.gallery.backend.entity.Cart;
import org.gallery.backend.entity.Order;
import org.gallery.backend.repository.CartRepository;
import org.gallery.backend.repository.OrderRepository;
import org.gallery.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    JwtService jwtService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CartRepository cartRepository;

    @GetMapping("/api/orders")
    public ResponseEntity getOrder(
            @CookieValue(value = "token", required = false) String token
    ) {

        if (!jwtService.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        List<Order> orders = orderRepository.findByMemberIdOrderByIdDesc(jwtService.getId(token));
        return new ResponseEntity<>(orders, HttpStatus.OK);

    }

    @Transactional
    @PostMapping("/api/orders")
    public ResponseEntity pushOrder(
            @RequestBody OrderDto dto,
            @CookieValue(value = "token", required = false) String token
    ) {

        if (!jwtService.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        int memberId = jwtService.getId(token);

        Order order = new Order();
        order.setMemberId(memberId);
        order.setName(dto.getName());
        order.setAddress(dto.getAddress());
        order.setPayment(dto.getPayment());
        if(StringUtils.hasText(dto.getCardNumber())) {
            order.setCardNumber(dto.getCardNumber());
        }
        order.setItems(dto.getItems());

        orderRepository.save(order);

        cartRepository.deleteByMemberId(memberId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
