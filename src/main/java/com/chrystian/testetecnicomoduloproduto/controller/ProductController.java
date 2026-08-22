package com.chrystian.testetecnicomoduloproduto.controller;

import com.chrystian.testetecnicomoduloproduto.dto.CreateProductDTO;
import com.chrystian.testetecnicomoduloproduto.dto.ProductResponseDTO;
import com.chrystian.testetecnicomoduloproduto.dto.RestockDTO;
import com.chrystian.testetecnicomoduloproduto.dto.SaleDTO;
import com.chrystian.testetecnicomoduloproduto.dto.UpdateProductDTO;
import com.chrystian.testetecnicomoduloproduto.service.impl.ProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Produtos", description = "API para gerenciar produtos")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductServiceImpl productService;



    /**
     * Busca todos os produtos
     */
    @GetMapping
    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista com todos os produtos cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        log.info("GET /api/v1/products - recebida requisicao para listar produtos");
        List<ProductResponseDTO> products = productService.findAll();
        log.info("GET /api/v1/products - resposta 200 com {} produtos", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * Busca um produto específico por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico através do seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable String id) {
        log.info("GET /api/v1/products/{} - recebida requisicao", id);
        ProductResponseDTO product = productService.findById(id);
        log.info("GET /api/v1/products/{} - resposta 200: {}", id, product);
        return ResponseEntity.ok(product);
    }

    /**
     * Busca produtos com quantidade maior que 0
     */
    @GetMapping("/in-stock/available")
    @Operation(summary = "Listar produtos em estoque", description = "Retorna uma lista de produtos com quantidade maior que 0")
    @ApiResponse(responseCode = "200", description = "Lista de produtos em estoque retornada com sucesso")
    public ResponseEntity<List<ProductResponseDTO>> findInStock() {
        log.info("GET /api/v1/products/in-stock/available - recebida requisicao");
        List<ProductResponseDTO> products = productService.findProductsInStock();
        log.info("GET /api/v1/products/in-stock/available - resposta 200 com {} produtos", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * Cria um novo produto
     */
    @PostMapping
    @Operation(summary = "Criar novo produto", description = "Cadastra um novo produto no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody CreateProductDTO dto) {
        log.info("POST /api/v1/products - requisicao recebida: {}", dto);
        ProductResponseDTO product = productService.create(dto);
        log.info("POST /api/v1/products - resposta 201: {}", product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Atualiza um produto existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza todas as informações de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductDTO dto) {
        log.info("PUT /api/v1/products/{} - requisicao recebida: {}", id, dto);
        ProductResponseDTO product = productService.update(id, dto);
        log.info("PUT /api/v1/products/{} - resposta 200: {}", id, product);
        return ResponseEntity.ok(product);
    }

    /**
     * Deleta um produto
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto", description = "Remove um produto do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/v1/products/{} - requisicao recebida", id);
        productService.delete(id);
        log.info("DELETE /api/v1/products/{} - resposta 204", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Realiza uma venda (reduz quantidade)
     */
    @PostMapping("/{id}/sale")
    @Operation(summary = "Realizar venda", description = "Reduz a quantidade do produto após uma venda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venda realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Estoque insuficiente ou dados inválidos")
    })
    public ResponseEntity<ProductResponseDTO> sale(
            @PathVariable String id,
            @Valid @RequestBody SaleDTO dto) {
        log.info("POST /api/v1/products/{}/sale - requisicao recebida: {}", id, dto);
        ProductResponseDTO product = productService.sale(id, dto);
        log.info("POST /api/v1/products/{}/sale - resposta 200: {}", id, product);
        return ResponseEntity.ok(product);
    }

    /**
     * Repoem o estoque (aumenta quantidade)
     */
    @PutMapping("/{id}/restock")
    @Operation(summary = "Repor estoque", description = "Aumenta a quantidade do produto quando chega um novo lote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reposição realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ProductResponseDTO> restock(
            @PathVariable String id,
            @Valid @RequestBody RestockDTO dto) {
        log.info("PUT /api/v1/products/{}/restock - requisicao recebida: {}", id, dto);
        ProductResponseDTO product = productService.restock(id, dto);
        log.info("PUT /api/v1/products/{}/restock - resposta 200: {}", id, product);
        return ResponseEntity.ok(product);
    }
}

