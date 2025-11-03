-- 创建应用表
CREATE TABLE IF NOT EXISTS apps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_name VARCHAR(100) NOT NULL,
    app_id VARCHAR(100) UNIQUE NOT NULL,
    app_secret VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    status TINYINT DEFAULT 1,
    creator VARCHAR(50),
    creator_id VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50),
    updater_id VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    status TINYINT DEFAULT 1,
    creator VARCHAR(50),
    creator_id VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50),
    updater_id VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建用户应用关联表
CREATE TABLE IF NOT EXISTS user_apps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    app_id BIGINT NOT NULL,
    openid VARCHAR(100) NOT NULL,
    unionid VARCHAR(100),
    status TINYINT DEFAULT 1,
    creator VARCHAR(50),
    creator_id VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50),
    updater_id VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_app (user_id, app_id),
    UNIQUE KEY uk_app_openid (app_id, openid)
);

-- 创建用户会话表
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    app_id BIGINT,
    session_token VARCHAR(255) NOT NULL,
    login_ip VARCHAR(45),
    user_agent VARCHAR(500),
    expire_time DATETIME NOT NULL,
    creator VARCHAR(50),
    creator_id VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(50),
    updater_id VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);