package com.chrystian.testetecnicomoduloproduto.service;

import com.chrystian.testetecnicomoduloproduto.dto.CreateProductDTO;
import com.chrystian.testetecnicomoduloproduto.dto.ProductResponseDTO;
import com.chrystian.testetecnicomoduloproduto.dto.RestockDTO;
import com.chrystian.testetecnicomoduloproduto.dto.SaleDTO;
import com.chrystian.testetecnicomoduloproduto.dto.UpdateProductDTO;
import com.chrystian.testetecnicomoduloproduto.exception.InsufficientStockException;
import com.chrystian.testetecnicomoduloproduto.exception.InvalidQuantityException;
import com.chrystian.testetecnicomoduloproduto.exception.ProductAlreadyExistsException;
import com.chrystian.testetecnicomoduloproduto.exception.ProductNotFoundException;
import com.chrystian.testetecnicomoduloproduto.model.Product;
import com.chrystian.testetecnicomoduloproduto.repository.ProductRepository;
import com.chrystian.testetecnicomoduloproduto.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldCreateProductWithGeneratedId() {
        CreateProductDTO input = CreateProductDTO.builder()
                .name("Teclado mecanico")
                .description("Teclado para escritorio")
                .price(new BigDecimal("299.90"))
                .quantity(10)
                .build();
        when(productRepository.findByNameIgnoreCase(input.getName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO result = productService.create(input);

        assertThat(result.getId()).isNotBlank();
        assertThat(result.getName()).isEqualTo(input.getName());
        assertThat(result.getPrice()).isEqualByComparingTo("299.90");
        assertThat(result.getQuantity()).isEqualTo(10);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldRejectProductWithDuplicateName() {
        CreateProductDTO input = CreateProductDTO.builder()
                .name("Teclado mecanico")
                .description("Outro teclado")
                .price(new BigDecimal("199.90"))
                .quantity(2)
                .build();
        when(productRepository.findByNameIgnoreCase(input.getName())).thenReturn(Optional.of(product("1", 5)));

        assertThatThrownBy(() -> productService.create(input))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining("Teclado mecanico");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldUpdateProductData() {
        Product existing = product("1", 4);
        UpdateProductDTO input = UpdateProductDTO.builder()
                .name("Mouse profissional")
                .description("Mouse atualizado")
                .price(new BigDecimal("149.90"))
                .quantity(8)
                .build();
        when(productRepository.findById("1")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        ProductResponseDTO result = productService.update("1", input);

        assertThat(result.getName()).isEqualTo("Mouse profissional");
        assertThat(result.getDescription()).isEqualTo("Mouse atualizado");
        assertThat(result.getPrice()).isEqualByComparingTo("149.90");
        assertThat(result.getQuantity()).isEqualTo(8);
        verify(productRepository).save(existing);
    }

    @Test
    void shouldDecreaseStockWhenSelling() {
        Product existing = product("1", 10);
        when(productRepository.findByIdForUpdate("1")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        ProductResponseDTO result = productService.sale("1", SaleDTO.builder().quantity(3).build());

        assertThat(result.getQuantity()).isEqualTo(7);
        assertThat(existing.getQuantity()).isEqualTo(7);
        verify(productRepository).save(existing);
    }

    @Test
    void shouldIncreaseStockWhenRestocking() {
        Product existing = product("1", 10);
        when(productRepository.findByIdForUpdate("1")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        ProductResponseDTO result = productService.restock("1", RestockDTO.builder().quantity(5).build());

        assertThat(result.getQuantity()).isEqualTo(15);
        assertThat(existing.getQuantity()).isEqualTo(15);
        verify(productRepository).save(existing);
    }

    @Test
    void shouldRejectSaleWhenQuantityExceedsStock() {
        Product existing = product("1", 2);
        when(productRepository.findByIdForUpdate("1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productService.sale("1", SaleDTO.builder().quantity(3).build()))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Disponível: 2");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectSaleFromEmptyStock() {
        Product existing = product("1", 0);
        when(productRepository.findByIdForUpdate("1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productService.sale("1", SaleDTO.builder().quantity(1).build()))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("sem estoque");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectInvalidSaleQuantity() {
        assertThatThrownBy(() -> productService.sale("1", SaleDTO.builder().quantity(0).build()))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessageContaining("maior que 0");
        verify(productRepository, never()).findByIdForUpdate(any(String.class));
    }

    @Test
    void shouldRejectInvalidRestockQuantity() {
        assertThatThrownBy(() -> productService.restock("1", RestockDTO.builder().quantity(-1).build()))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessageContaining("maior que 0");
        verify(productRepository, never()).findByIdForUpdate(any(String.class));
    }

    @Test
    void shouldRejectUnknownProduct() {
        when(productRepository.findByIdForUpdate("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.restock("missing", RestockDTO.builder().quantity(1).build()))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("missing");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldReturnOnlyProductsWithStock() {
        Product available = product("1", 3);
        when(productRepository.findByQuantityGreaterThan(0)).thenReturn(List.of(available));

        List<ProductResponseDTO> result = productService.findProductsInStock();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
    }

    private Product product(String id, int quantity) {
        return Product.builder()
                .id(id)
                .name("Produto " + id)
                .description("Descricao")
                .price(new BigDecimal("10.00"))
                .quantity(quantity)
                .build();
    }
}
