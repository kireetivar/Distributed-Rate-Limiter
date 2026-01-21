package com.distributed.rate_limiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class DistributedRateLimiterApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedRateLimiterApplication.class, args);
	}

}
