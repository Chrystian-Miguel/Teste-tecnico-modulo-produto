package com.chrystian.testetecnicomoduloproduto.contract;

import com.chrystian.testetecnicomoduloproduto.dto.CreateProductDTO;
import com.chrystian.testetecnicomoduloproduto.dto.ProductResponseDTO;
import com.chrystian.testetecnicomoduloproduto.dto.RestockDTO;
import com.chrystian.testetecnicomoduloproduto.dto.SaleDTO;
import com.chrystian.testetecnicomoduloproduto.dto.UpdateProductDTO;

import java.util.List;

public interface IProductService {

    /**
     * Busca todos os produtos
     */
    List<ProductResponseDTO> findAll();

    /**
     * Busca um produto por ID
     */
    ProductResponseDTO findById(String id);

    /**
     * Busca produtos com quantidade maior que 0
     */
    List<ProductResponseDTO> findProductsInStock();

    /**
     * Cria um novo produto
     */
    ProductResponseDTO create(CreateProductDTO dto);

    /**
     * Atualiza um produto existente
     */
    ProductResponseDTO update(String id, UpdateProductDTO dto);

    /**
     * Deleta um produto
     */
    void delete(String id);

    /**
     * Realiza uma venda (reduz a quantidade)
     */
    ProductResponseDTO sale(String id, SaleDTO dto);

    /**
     * Repoem o estoque (aumenta a quantidade)
     */
    ProductResponseDTO restock(String id, RestockDTO dto);
}

