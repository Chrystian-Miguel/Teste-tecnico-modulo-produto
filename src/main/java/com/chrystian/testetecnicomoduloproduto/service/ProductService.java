package com.chrystian.testetecnicomoduloproduto.service;

import com.chrystian.testetecnicomoduloproduto.contract.IProductService;
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
import com.chrystian.testetecnicomoduloproduto.util.ProductValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;


    /**
     * Busca todos os produtos
     */
    public List<ProductResponseDTO> findAll() {
        log.info("Buscando todos os produtos");
        return productRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca um produto por ID
     */
    public ProductResponseDTO findById(String id) {
        log.info("Buscando produto com ID: {}", id);
        Product product = getProductOrThrow(id);
        return convertToDTO(product);
    }

    /**
     * Busca produtos com quantidade maior que 0
     */
    public List<ProductResponseDTO> findProductsInStock() {
        log.info("Buscando produtos com estoque disponível");

        List<Product> productsInStock = productRepository.findByQuantityGreaterThan(0);
        productsNotFound(productsInStock);
        return productsInStock
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cria um novo produto
     */
    public ProductResponseDTO create(CreateProductDTO dto) {
        log.info("Criando novo produto: {}", dto.getName());

        // Usar ProductValidator para validações centralizadas
        ProductValidator.validateCreateProductDTO(dto);
        // Verifica se o produto já existe
        productAlredyExists(dto.getName());

        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .build();

        Product savedProduct = productRepository.save(product);
        logSuccess("CRIAR", savedProduct.getId(), formatProductInfo(savedProduct));

        return convertToDTO(savedProduct);
    }

    /**
     * Atualiza um produto existente
     */
    public ProductResponseDTO update(String id, UpdateProductDTO dto) {
        log.info("Atualizando produto com ID: {}", id);

        // Usar ProductValidator para validações centralizadas
        ProductValidator.validateUpdateProductDTO(dto);
        // Verifica se o produto existe
        Product product = getProductOrThrow(id);

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());

        Product updatedProduct = productRepository.save(product);
        logSuccess("ATUALIZAR", id, formatProductInfo(updatedProduct));

        return convertToDTO(updatedProduct);
    }

    /**
     * Deleta um produto
     */
    public void delete(String id) {
        log.info("Deletando produto com ID: {}", id);
        // Verifica se o produto existe
        Product product = getProductForUpdateOrThrow(id);
        productRepository.delete(product);

        logSuccess("DELETAR", id, "Produto removido com sucesso");
    }

    /**
     * Realiza uma venda (reduz a quantidade do produto)
     */
    public ProductResponseDTO sale(String id, SaleDTO dto) {
        log.info("Processando venda do produto ID: {} - Quantidade: {}", id, dto.getQuantity());

        // 1. Validar entrada
        validateSaleQuantity(dto.getQuantity());

        // 2. Buscar produto
        Product product = getProductForUpdateOrThrow(id);

        // 3. Verificar estoque
        checkStockAvailability(product);

        // 4. Verificar quantidade suficiente
        checkSufficientQuantity(product, dto.getQuantity(), "VENDA");

        // 5. Calcular nova quantidade
        Integer newQuantity = calculateNewQuantityAfterSale(product, dto.getQuantity());

        // 6. Atualizar com log estruturado
        Product updatedProduct = updateProductQuantity(product, newQuantity, "VENDA");

        logSuccess("VENDA", id, String.format("Qtd vendida: %d, Restante: %d",
                dto.getQuantity(), updatedProduct.getQuantity()));

        return convertToDTO(updatedProduct);
    }

    /**
     * Repoem o estoque (aumenta a quantidade do produto)
     */
    public ProductResponseDTO restock(String id, RestockDTO dto) {
        log.info("Reposição de estoque do produto ID: {} - Quantidade: {}", id, dto.getQuantity());

        // 1. Validar entrada
        validateRestockQuantity(dto.getQuantity());

        // 2. Buscar produto
        Product product = getProductForUpdateOrThrow(id);

        // 3. Calcular nova quantidade
        Integer newQuantity = calculateNewQuantityAfterRestock(product, dto.getQuantity());

        // 4. Atualizar com log estruturado
        Product updatedProduct = updateProductQuantity(product, newQuantity, "REPOSIÇÃO");

        logSuccess("REPOSIÇÃO", id, String.format("Qtd adicionada: %d, Novo total: %d",
                dto.getQuantity(), updatedProduct.getQuantity()));

        return convertToDTO(updatedProduct);
    }


    /**
     * Obtém um produto pelo ID ou lança exceção
     */
    private Product getProductOrThrow(String id) {
        log.debug("Buscando produto com ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Produto não encontrado com ID: {}", id);
                    return new ProductNotFoundException("Produto não encontrado com ID: " + id);
                });
    }

    private Product getProductForUpdateOrThrow(String id) {
        log.debug("Buscando produto com lock para atualização: {}", id);
        return productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> {
                    log.error("Produto não encontrado com ID: {}", id);
                    return new ProductNotFoundException("Produto não encontrado com ID: " + id);
                });
    }

    /**
     * Verifica se um produto tem estoque disponível
     */

    private void checkStockAvailability(Product product) {
        if (product.getQuantity() <= 0) {
            log.warn("Produto sem estoque: {}", product.getId());
            throw new InsufficientStockException("Produto sem estoque disponível");
        }
    }

    /**
     * Verifica se a lista de produtos não está vazia, caso contrário lança exceção
     */
    private void productsNotFound(List<Product> products) {
        if (products.isEmpty()) {
            log.warn("Nenhum produto encontrado com estoque disponível");
            throw new ProductNotFoundException("Nenhum produto encontrado com estoque disponível");
        }

    }

    /**
     * Verifica se um produto com o mesmo nome não existe, caso contrário lança exceção
     */
    private void productAlredyExists(String name) {
        if (productRepository.findByNameIgnoreCase(name).isPresent()) {
            log.error("Produto com nome '{}' já existe", name);
            throw new ProductAlreadyExistsException("Produto com nome '" + name + "' já existe");
        }
    }

    /**
     * Verifica se há quantidade suficiente para operação
     */

    private void checkSufficientQuantity(Product product, Integer requestedQuantity, String operation) {
        if (requestedQuantity > product.getQuantity()) {
            log.error(
                    "Quantidade insuficiente para {}. Disponível: {} Solicitado: {}",
                    operation,
                    product.getQuantity(),
                    requestedQuantity
            );
            throw new InsufficientStockException(
                    "Quantidade insuficiente para " + operation +
                            ". Disponível: " + product.getQuantity()
            );
        }
    }

    /**
     * Atualiza a quantidade de um produto
     */
    private Product updateProductQuantity(Product product, Integer newQuantity, String operation) {
        Integer oldQuantity = product.getQuantity();
        product.setQuantity(newQuantity);
        Product updated = productRepository.save(product);

        log.info(
                "Quantidade atualizada para {}: {} → {} ({})",
                product.getId(),
                oldQuantity,
                newQuantity,
                operation
        );

        return updated;
    }

    /**
     * Calcula a nova quantidade após venda
     */
    private Integer calculateNewQuantityAfterSale(Product product, Integer saleQuantity) {
        return product.getQuantity() - saleQuantity;
    }

    /**
     * Calcula a nova quantidade após reposição
     */
    private Integer calculateNewQuantityAfterRestock(Product product, Integer restockQuantity) {
        return product.getQuantity() + restockQuantity;
    }

    /**
     * Valida se o ID do produto é válido
     */
    private void validateProductId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID do produto não pode estar vazio");
        }
    }

    /**
     * Valida a quantidade de venda
     */
    private void validateSaleQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            log.error("Quantidade de venda inválida: {}", quantity);
            throw new InvalidQuantityException("Quantidade deve ser maior que 0");
        }
    }

    /**
     * Valida a quantidade de reposiçao
     */
    private void validateRestockQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            log.error("Quantidade de reposição inválida: {}", quantity);
            throw new InvalidQuantityException("Quantidade deve ser maior que 0");
        }
    }

    /**
     * Log estruturado de operação bem-sucedida
     */
    private void logSuccess(String operation, String productId, String details) {
        log.info("{} realizado com sucesso. Produto: {}, Detalhes: {}", operation, productId, details);
    }

    /**
     * Log estruturado de operação com erro
     */
    private void logError(String operation, String productId, String errorMessage) {
        log.error("{} falhou para Produto: {}, Erro: {}", operation, productId, errorMessage);
    }

    /**
     * Formata informações do produto para log
     */
    private String formatProductInfo(Product product) {
        return String.format(
                "ID=%s, Nome=%s, Qtd=%d, Preço=%.2f",
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice()
        );
    }

    /**
     * Converte Product em ProductResponseDTO
     */
    private ProductResponseDTO convertToDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}

