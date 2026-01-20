package com.distributed.rate_limiter.controller;

import com.distributed.rate_limiter.annotations.RateLimit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    @RateLimit
    public ResponseEntity<String> ping() {
        return new ResponseEntity<>("\"pong\"", HttpStatus.OK);
    }

    @GetMapping("/health")
    @RateLimit(capacity = 2, refillRate = 0.2)
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("\"healthy\"", HttpStatus.OK);
    }
}
