package com.chrystian.testetecnicomoduloproduto.controller;

import com.chrystian.testetecnicomoduloproduto.dto.CreateProductDTO;
import com.chrystian.testetecnicomoduloproduto.dto.ProductResponseDTO;
import com.chrystian.testetecnicomoduloproduto.dto.UpdateProductDTO;
import com.chrystian.testetecnicomoduloproduto.exception.GlobalExceptionHandler;
import com.chrystian.testetecnicomoduloproduto.exception.InsufficientStockException;
import com.chrystian.testetecnicomoduloproduto.exception.ProductNotFoundException;
import com.chrystian.testetecnicomoduloproduto.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductServiceImpl productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnUpdatedProductAfterSale() throws Exception {
        when(productService.sale(eq("product-1"), any()))
                .thenReturn(productResponse("product-1", 7));

        mockMvc.perform(post("/api/v1/products/product-1/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(7));
    }

    @Test
    void shouldReturnUpdatedProductAfterRestock() throws Exception {
        when(productService.restock(eq("product-1"), any()))
                .thenReturn(productResponse("product-1", 15));

        mockMvc.perform(put("/api/v1/products/product-1/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void shouldReturnAllProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(
                productResponse("product-1", 7),
                productResponse("product-2", 2)));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("product-1"))
                .andExpect(jsonPath("$[1].id").value("product-2"));

        verify(productService).findAll();
    }

    @Test
    void shouldReturnProductById() throws Exception {
        when(productService.findById("product-1")).thenReturn(productResponse("product-1", 7));

        mockMvc.perform(get("/api/v1/products/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(7));
    }

    @Test
    void shouldReturnProductsInStock() throws Exception {
        when(productService.findProductsInStock()).thenReturn(List.of(productResponse("product-1", 7)));

        mockMvc.perform(get("/api/v1/products/in-stock/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("product-1"))
                .andExpect(jsonPath("$[0].quantity").value(7));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        when(productService.create(any(CreateProductDTO.class)))
                .thenReturn(productResponse("product-1", 10));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Teclado\",\"description\":\"Mecanico\",\"price\":299.90,\"quantity\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(10));

        verify(productService).create(any(CreateProductDTO.class));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        when(productService.update(eq("product-1"), any(UpdateProductDTO.class)))
                .thenReturn(productResponse("product-1", 12));

        mockMvc.perform(put("/api/v1/products/product-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Teclado atualizado\",\"description\":\"Mecanico\",\"price\":349.90,\"quantity\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(12));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/v1/products/product-1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService).delete("product-1");
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.findById("missing"))
                .thenThrow(new ProductNotFoundException("Produto não encontrado: missing"));

        mockMvc.perform(get("/api/v1/products/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Produto não encontrado"));
    }

    @Test
    void shouldReturnBadRequestWhenStockIsInsufficient() throws Exception {
        when(productService.sale(eq("product-1"), any()))
                .thenThrow(new InsufficientStockException("Quantidade insuficiente para VENDA. Disponível: 2"));

        mockMvc.perform(post("/api/v1/products/product-1/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Estoque insuficiente"))
                .andExpect(jsonPath("$.details").value("Quantidade insuficiente para VENDA. Disponível: 2"));
    }

    @Test
    void shouldRejectInvalidQuantityBeforeCallingService() throws Exception {
        mockMvc.perform(put("/api/v1/products/product-1/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Erro de validação"));
    }

    private ProductResponseDTO productResponse(String id, int quantity) {
        return ProductResponseDTO.builder()
                .id(id)
                .name("Produto")
                .description("Descricao")
                .price(new BigDecimal("10.00"))
                .quantity(quantity)
                .build();
    }
}
