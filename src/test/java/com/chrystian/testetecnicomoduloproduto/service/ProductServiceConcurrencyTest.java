package com.chrystian.testetecnicomoduloproduto.service;

import com.chrystian.testetecnicomoduloproduto.dto.SaleDTO;
import com.chrystian.testetecnicomoduloproduto.exception.InsufficientStockException;
import com.chrystian.testetecnicomoduloproduto.model.Product;
import com.chrystian.testetecnicomoduloproduto.repository.ProductRepository;
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
class ProductServiceConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private final List<String> createdProductIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        productRepository.deleteAllById(createdProductIds);
    }

    @Test
    void shouldAllowOnlyOneOfThreeSimultaneousPurchasesForOneUnit() throws Exception {
        String productId = UUID.randomUUID().toString();
        createdProductIds.add(productId);
        productRepository.save(Product.builder()
                .id(productId)
                .name("Produto concorrente " + productId)
                .description("Teste de concorrencia")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .build());

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            List<Future<Boolean>> purchases = new ArrayList<>();
            for (int index = 0; index < 3; index++) {
                purchases.add(executor.submit(() -> {
                    start.await();
                    try {
                        productService.sale(productId, SaleDTO.builder().quantity(1).build());
                        return true;
                    } catch (InsufficientStockException exception) {
                        return false;
                    }
                }));
            }
            start.countDown();

            long successfulPurchases = 0;
            for (Future<Boolean> purchase : purchases) {
                if (purchase.get()) {
                    successfulPurchases++;
                }
            }

            assertThat(successfulPurchases).isEqualTo(1);
            assertThat(productRepository.findById(productId)).get()
                    .extracting(Product::getQuantity)
                    .isEqualTo(0);
        } finally {
            executor.shutdownNow();
        }
    }
}