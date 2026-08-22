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
        boolean inStock = product != null && product.getQuantity() > 0;
        log.debug("Verificando estoque do produto: resultado={}", inStock);
        return inStock;
    }

    /**
     * Verifica se há quantidade suficiente
     */
    public static boolean hasSufficientStock(Product product, Integer requestedQuantity) {
         boolean sufficient = product != null &&
               product.getQuantity() != null &&
               product.getQuantity() >= requestedQuantity;
         log.debug("Verificando estoque suficiente: solicitado={}, resultado={}", requestedQuantity, sufficient);
         return sufficient;
    }

    /**
     * Calcula o preço total de uma venda
     */
    public static BigDecimal calculateTotalPrice(Product product, Integer quantity) {
        if (product == null || product.getPrice() == null || quantity == null) {
            log.debug("Calculando preço total: dados insuficientes, retornando zero");
            return BigDecimal.ZERO;
        }
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        log.debug("Preço total calculado: {}", totalPrice);
        return totalPrice;
    }

    /**
     * Formata informações do produto para log
     */
    public static String formatProductInfo(Product product) {
        if (product == null) {
            log.debug("Formatando informações do produto nulo");
            return "Product(null)";
        }
        String productInfo = String.format(
                "Product(id=%s, name=%s, qty=%d, price=%.2f)",
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice()
        );
            log.debug("Informações do produto formatadas: {}", productInfo);
            return productInfo;
    }

    /**
     * Formata informações de quantidade para log
     */
    public static String formatQuantityInfo(Integer current, Integer previous) {
        String quantityInfo = String.format("Quantidade: %d → %d", previous, current);
        log.debug("Informações de quantidade formatadas: {}", quantityInfo);
        return quantityInfo;
    }

    /**
     * Verifica se dois produtos são iguais pelo ID
     */
    public static boolean isSameProduct(Product product1, Product product2) {
        if (product1 == null || product2 == null) {
            log.debug("Comparando produtos: um ou ambos são nulos");
            return false;
        }
        boolean sameProduct = product1.getId().equals(product2.getId());
        log.debug("Comparando produtos por ID: resultado={}", sameProduct);
        return sameProduct;
    }

    /**
     * Verifica se o ID é válido (não nulo e não vazio)
     */
    public static boolean isValidId(String id) {
        boolean validId = id != null && !id.isBlank() && id.length() == 36;
        log.debug("Validando ID do produto: resultado={}", validId);
        return validId;
    }

    /**
     * Formata um valor monetário
     */
    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            log.debug("Formatando valor monetário nulo");
            return "R$ 0,00";
        }
        String formattedValue = String.format("R$ %.2f", value);
        log.debug("Valor monetário formatado: {}", formattedValue);
        return formattedValue;
    }

    /**
     * Retorna a descrição abreviada de um produto
     */
    public static String getProductSummary(Product product) {
        if (product == null) {
            log.debug("Obtendo resumo de produto nulo");
            return "Produto não disponível";
        }
        String summary = String.format("%s (ID: %s)", product.getName(), product.getId().substring(0, 8) + "...");
        log.debug("Resumo do produto obtido: {}", summary);
        return summary;
    }
}

