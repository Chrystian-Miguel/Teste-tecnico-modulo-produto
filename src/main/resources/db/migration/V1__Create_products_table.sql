CREATE TABLE products (
    id CHAR(36) PRIMARY KEY COMMENT 'UUID do produto',
    name VARCHAR(255) NOT NULL COMMENT 'Nome do produto',
    description LONGTEXT COMMENT 'Descrição do produto',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Preço do produto',
    quantity INT NOT NULL DEFAULT 0 COMMENT 'Quantidade em estoque',
    CONSTRAINT chk_quantity CHECK (quantity >= 0),   -- Validação: quantidade não pode ser menor que 0
    CONSTRAINT chk_price CHECK (price > 0),  -- Validação: preço deve ser maior que 0
    INDEX idx_quantity (quantity),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

