package com.chrystian.testetecnicomoduloproduto.repository;

import com.chrystian.testetecnicomoduloproduto.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByQuantityGreaterThan(Integer quantity);
    Product findByName(String name);
    Optional<Product> findByNameIgnoreCase(String name);
}

