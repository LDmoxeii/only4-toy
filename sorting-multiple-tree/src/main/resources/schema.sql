-- UI资源表_1
CREATE TABLE IF NOT EXISTS ui_resource_1
    (
    id            VARCHAR(36) PRIMARY KEY,
    parent_id     VARCHAR(36),
    node_path     VARCHAR(255),
    sort          BIGINT,
    title         VARCHAR(100),
    en_title      VARCHAR(100),
    show_status   BOOLEAN DEFAULT TRUE,
    active_status BOOLEAN DEFAULT TRUE
    );

-- UI资源表_2
CREATE TABLE IF NOT EXISTS ui_resource_2
    (
    id            VARCHAR(36) PRIMARY KEY,
    parent_id     VARCHAR(36),
    node_path     VARCHAR(255),
    sort          BIGINT,
    title         VARCHAR(100),
    en_title      VARCHAR(100),
    show_status   BOOLEAN DEFAULT TRUE,
    active_status BOOLEAN DEFAULT TRUE
    );

-- API资源表_1
CREATE TABLE IF NOT EXISTS api_resource_1
    (
    id            VARCHAR(36) PRIMARY KEY,
    parent_id     VARCHAR(36),
    node_path     VARCHAR(255),
    sort          BIGINT,
    title         VARCHAR(100),
    en_title      VARCHAR(100),
    show_status   BOOLEAN DEFAULT TRUE,
    active_status BOOLEAN DEFAULT TRUE
    );

-- API资源表_2
CREATE TABLE IF NOT EXISTS api_resource_2
    (
    id            VARCHAR(36) PRIMARY KEY,
    parent_id     VARCHAR(36),
    node_path     VARCHAR(255),
    sort          BIGINT,
    title         VARCHAR(100),
    en_title      VARCHAR(100),
    show_status   BOOLEAN DEFAULT TRUE,
    active_status BOOLEAN DEFAULT TRUE
    );
