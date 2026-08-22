package com.chrystian.testetecnicomoduloproduto.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductDTO {

    @NotBlank(message = "Nome do produto não pode estar vazio")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String name;

    @NotBlank(message = "Descrição não pode estar vazia")
    private String description;

    @NotNull(message = "Preço não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que 0")
    private BigDecimal price;

    @NotNull(message = "Quantidade não pode ser nula")
    @Min(value = 0, message = "Quantidade não pode ser menor que 0")
    private Integer quantity;
}

