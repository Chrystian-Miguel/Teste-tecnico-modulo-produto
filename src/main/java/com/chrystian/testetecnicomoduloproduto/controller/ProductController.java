package com.chrystian.testetecnicomoduloproduto.controller;

import com.chrystian.testetecnicomoduloproduto.dto.CreateProductDTO;
import com.chrystian.testetecnicomoduloproduto.dto.ProductResponseDTO;
import com.chrystian.testetecnicomoduloproduto.dto.RestockDTO;
import com.chrystian.testetecnicomoduloproduto.dto.SaleDTO;
import com.chrystian.testetecnicomoduloproduto.dto.UpdateProductDTO;
import com.chrystian.testetecnicomoduloproduto.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Produtos", description = "API para gerenciar produtos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Busca todos os produtos
     */
    @GetMapping
    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista com todos os produtos cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        return ResponseEntity.ok(productService.findAll());
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
        return ResponseEntity.ok(productService.findById(id));
    }

    /**
     * Busca produtos com quantidade maior que 0
     */
    @GetMapping("/in-stock/available")
    @Operation(summary = "Listar produtos em estoque", description = "Retorna uma lista de produtos com quantidade maior que 0")
    @ApiResponse(responseCode = "200", description = "Lista de produtos em estoque retornada com sucesso")
    public ResponseEntity<List<ProductResponseDTO>> findInStock() {
        return ResponseEntity.ok(productService.findProductsInStock());
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
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
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
        return ResponseEntity.ok(productService.update(id, dto));
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
        productService.delete(id);
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
        return ResponseEntity.ok(productService.sale(id, dto));
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
        return ResponseEntity.ok(productService.restock(id, dto));
    }
}

