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
        log.debug("Iniciando validacao de CreateProductDTO");

        if (dto == null) {
            throw new IllegalArgumentException("DTO não pode ser nulo");
        }

        validateName(dto.getName());
        validateDescription(dto.getDescription());
        validatePrice(dto.getPrice());
        validateQuantity(dto.getQuantity());
        log.debug("CreateProductDTO validado com sucesso");
    }

    /**
     * Valida um DTO de atualização de produto
     */
    public static void validateUpdateProductDTO(UpdateProductDTO dto) {
        log.debug("Iniciando validacao de UpdateProductDTO");

        if (dto == null) {
            throw new IllegalArgumentException("DTO não pode ser nulo");
        }

        validateName(dto.getName());
        validateDescription(dto.getDescription());
        validatePrice(dto.getPrice());
        validateQuantity(dto.getQuantity());
        log.debug("UpdateProductDTO validado com sucesso");
    }

    /**
     * Valida o nome do produto
     */
    public static void validateName(String name) {
        log.debug("Validando nome do produto");
        if (name == null || name.isBlank()) {
            log.debug("Nome do produto é nulo ou vazio");
            throw new IllegalArgumentException("Nome não pode estar vazio");
        }
        if (name.length() < 3) {
            log.debug("Nome do produto tem menos de 3 caracteres");
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres");
        }
        if (name.length() > 255) {
            log.debug("Nome do produto tem mais de 255 caracteres");
            throw new IllegalArgumentException("Nome não pode ter mais de 255 caracteres");
        }
        log.debug("Nome do produto validado com sucesso");
    }

    /**
     * Valida a descrição do produto
     */
    public static void validateDescription(String description) {
        log.debug("Validando descrição do produto");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Descrição não pode estar vazia");
        }
        log.debug("Descrição do produto validada com sucesso");
    }

    /**
     * Valida o preço do produto
     */
    public static void validatePrice(BigDecimal price) {
        log.debug("Validando preço do produto");
        if (price == null) {
            throw new IllegalArgumentException("Preço não pode ser nulo");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que 0");
        }
        log.debug("Preço do produto validado com sucesso");
    }

    /**
     * Valida a quantidade do produto
     */
    public static void validateQuantity(Integer quantity) {
        log.debug("Validando quantidade do produto: {}", quantity);
        if (quantity == null) {
            throw new IllegalArgumentException("Quantidade não pode ser nula");
        }
        if (quantity < 0) {
            throw new InvalidQuantityException("Quantidade não pode ser menor que 0");
        }
        log.debug("Quantidade do produto validada com sucesso: {}", quantity);
    }

    /**
     * Valida se a quantidade é válida para venda (deve ser > 0)
     */
    public static void validateSaleQuantity(Integer quantity) {
        log.debug("Validando quantidade para venda: {}", quantity);
        if (quantity == null || quantity <= 0) {
            throw new InvalidQuantityException("Quantidade para venda deve ser maior que 0");
        }
        log.debug("Quantidade para venda validada com sucesso: {}", quantity);
    }

    /**
     * Valida se há quantidade suficiente para venda
     */
    public static void validateSufficientStock(Integer availableQuantity, Integer requestedQuantity) {
        log.debug("Validando estoque: disponivel={}, solicitado={}", availableQuantity, requestedQuantity);
        if (requestedQuantity > availableQuantity) {
            log.warn("Estoque insuficiente: disponivel={}, solicitado={}", availableQuantity, requestedQuantity);
            throw new IllegalArgumentException(
                    "Quantidade insuficiente. Disponível: " + availableQuantity +
                    ", Solicitado: " + requestedQuantity
            );
        }
                log.debug("Estoque suficiente para a quantidade solicitada");
    }
}

