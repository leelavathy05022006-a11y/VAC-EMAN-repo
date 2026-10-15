CREATE TABLE medicine_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    generic_name VARCHAR(120) NOT NULL,
    strength VARCHAR(40) NOT NULL,
    dosage_form VARCHAR(20) NOT NULL,
    manufacturer VARCHAR(120) NOT NULL,
    requires_prescription TINYINT(1) NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicine_category FOREIGN KEY (category_id) REFERENCES medicine_categories(id),
    CONSTRAINT uk_medicine_name_strength_form UNIQUE (name, strength, dosage_form)
);
