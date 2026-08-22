package com.chrystian.testetecnicomoduloproduto.service;

import com.chrystian.testetecnicomoduloproduto.dto.CreateProductDTO;
import com.chrystian.testetecnicomoduloproduto.repository.ProductRepository;
import com.chrystian.testetecnicomoduloproduto.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductCreationConcurrencyTest {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ProductRepository productRepository;

    private String productName;

    @AfterEach
    void cleanUp() {
        if (productName != null) {
            productRepository.findByNameIgnoreCase(productName).ifPresent(productRepository::delete);
        }
    }

    @Test
    void shouldAllowOnlyOneOfThreeSimultaneousRegistrationsWithSameName() throws Exception {
        productName = "Produto concorrente " + UUID.randomUUID();
        CreateProductDTO input = CreateProductDTO.builder()
                .name(productName)
                .description("Teste de cadastro concorrente")
                .price(new BigDecimal("10.00"))
                .quantity(5)
                .build();

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            List<Future<Boolean>> registrations = new ArrayList<>();
            for (int index = 0; index < 3; index++) {
                registrations.add(executor.submit(() -> {
                    start.await();
                    try {
                        productService.create(input);
                        return true;
                    } catch (RuntimeException exception) {
                        return false;
                    }
                }));
            }
            start.countDown();

            long successfulRegistrations = 0;
            for (Future<Boolean> registration : registrations) {
                if (registration.get()) {
                    successfulRegistrations++;
                }
            }

            assertThat(successfulRegistrations).isEqualTo(1);
            assertThat(productRepository.findByNameIgnoreCase(productName)).isPresent();
            assertThat(productRepository.findAll().stream()
                    .filter(product -> productName.equals(product.getName())))
                    .hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }
}