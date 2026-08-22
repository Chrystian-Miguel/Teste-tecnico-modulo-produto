package com.chrystian.testetecnicomoduloproduto.util;

import com.chrystian.testetecnicomoduloproduto.model.Product;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;


@Slf4j
public class ProductUtils {

    private ProductUtils() {

    }

    /**
     * Verifica se um produto tem estoque disponível
     */
    public static boolean isInStock(Product product) {
        return product != null && product.getQuantity() > 0;
    }

    /**
     * Verifica se há quantidade suficiente
     */
    public static boolean hasSufficientStock(Product product, Integer requestedQuantity) {
        return product != null &&
               product.getQuantity() != null &&
               product.getQuantity() >= requestedQuantity;
    }

    /**
     * Calcula o preço total de uma venda
     */
    public static BigDecimal calculateTotalPrice(Product product, Integer quantity) {
        if (product == null || product.getPrice() == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Formata informações do produto para log
     */
    public static String formatProductInfo(Product product) {
        if (product == null) {
            return "Product(null)";
        }
        return String.format(
                "Product(id=%s, name=%s, qty=%d, price=%.2f)",
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice()
        );
    }

    /**
     * Formata informações de quantidade para log
     */
    public static String formatQuantityInfo(Integer current, Integer previous) {
        return String.format("Quantidade: %d → %d", previous, current);
    }

    /**
     * Verifica se dois produtos são iguais pelo ID
     */
    public static boolean isSameProduct(Product product1, Product product2) {
        if (product1 == null || product2 == null) {
            return false;
        }
        return product1.getId().equals(product2.getId());
    }

    /**
     * Verifica se o ID é válido (não nulo e não vazio)
     */
    public static boolean isValidId(String id) {
        return id != null && !id.isBlank() && id.length() == 36;
    }

    /**
     * Formata um valor monetário
     */
    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "R$ 0,00";
        }
        return String.format("R$ %.2f", value);
    }

    /**
     * Retorna a descrição abreviada de um produto
     */
    public static String getProductSummary(Product product) {
        if (product == null) {
            return "Produto não disponível";
        }
        return String.format("%s (ID: %s)", product.getName(), product.getId().substring(0, 8) + "...");
    }
}

