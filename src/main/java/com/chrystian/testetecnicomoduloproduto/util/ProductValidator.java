package com.chrystian.testetecnicomoduloproduto.util;

import com.chrystian.testetecnicomoduloproduto.dto.CreateProductDTO;
import com.chrystian.testetecnicomoduloproduto.dto.UpdateProductDTO;
import com.chrystian.testetecnicomoduloproduto.exception.InvalidQuantityException;
import com.chrystian.testetecnicomoduloproduto.model.Product;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;


@Slf4j
public class ProductValidator {

    private ProductValidator() {

    }

    /**
     * Valida um DTO de criação de produto
     */
    public static void validateCreateProductDTO(CreateProductDTO dto) {
        log.debug("Validando CreateProductDTO");

        if (dto == null) {
            throw new IllegalArgumentException("DTO não pode ser nulo");
        }

        validateName(dto.getName());
        validateDescription(dto.getDescription());
        validatePrice(dto.getPrice());
        validateQuantity(dto.getQuantity());
    }

    /**
     * Valida um DTO de atualização de produto
     */
    public static void validateUpdateProductDTO(UpdateProductDTO dto) {
        log.debug("Validando UpdateProductDTO");

        if (dto == null) {
            throw new IllegalArgumentException("DTO não pode ser nulo");
        }

        validateName(dto.getName());
        validateDescription(dto.getDescription());
        validatePrice(dto.getPrice());
        validateQuantity(dto.getQuantity());
    }

    /**
     * Valida o nome do produto
     */
    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome não pode estar vazio");
        }
        if (name.length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Nome não pode ter mais de 255 caracteres");
        }

    }

    /**
     * Valida a descrição do produto
     */
    public static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Descrição não pode estar vazia");
        }
    }

    /**
     * Valida o preço do produto
     */
    public static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Preço não pode ser nulo");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que 0");
        }
    }

    /**
     * Valida a quantidade do produto
     */
    public static void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantidade não pode ser nula");
        }
        if (quantity < 0) {
            throw new InvalidQuantityException("Quantidade não pode ser menor que 0");
        }
    }

    /**
     * Valida se a quantidade é válida para venda (deve ser > 0)
     */
    public static void validateSaleQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidQuantityException("Quantidade para venda deve ser maior que 0");
        }
    }

    /**
     * Valida se há quantidade suficiente para venda
     */
    public static void validateSufficientStock(Integer availableQuantity, Integer requestedQuantity) {
        if (requestedQuantity > availableQuantity) {
            throw new IllegalArgumentException(
                    "Quantidade insuficiente. Disponível: " + availableQuantity +
                    ", Solicitado: " + requestedQuantity
            );
        }
    }
}

