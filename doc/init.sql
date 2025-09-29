-- 多应用用户中心数据库初始化脚本
-- @author Ravey

-- 应用表
CREATE TABLE apps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '应用ID',
    app_name VARCHAR(100) NOT NULL COMMENT '应用名称',
    app_id VARCHAR(100) UNIQUE NOT NULL COMMENT '微信小程序AppId',
    app_secret VARCHAR(200) NOT NULL COMMENT '微信小程序AppSecret',
    description VARCHAR(500) COMMENT '应用描述',
    status TINYINT DEFAULT 1 COMMENT '应用状态：1-正常，0-禁用',
    creator VARCHAR(50) COMMENT '创建人',
    creator_id VARCHAR(50) COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(50) COMMENT '更新人',
    updater_id VARCHAR(50) COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

-- 用户表（多应用版本）
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '用户状态：1-正常，0-禁用',
    creator VARCHAR(50) COMMENT '创建人',
    creator_id VARCHAR(50) COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(50) COMMENT '更新人',
    updater_id VARCHAR(50) COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户应用关联表
CREATE TABLE user_apps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    openid VARCHAR(100) NOT NULL COMMENT '微信小程序openId',
    unionid VARCHAR(100) COMMENT '微信unionId',
    status TINYINT DEFAULT 1 COMMENT '关联状态：1-正常，0-禁用',
    creator VARCHAR(50) COMMENT '创建人',
    creator_id VARCHAR(50) COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(50) COMMENT '更新人',
    updater_id VARCHAR(50) COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_app (user_id, app_id),
    UNIQUE KEY uk_app_openid (app_id, openid),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户应用关联表';

-- 二维码登录记录表（多应用版本）
CREATE TABLE qr_login_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    qrcode_id VARCHAR(64) UNIQUE NOT NULL COMMENT '二维码唯一标识',
    app_id BIGINT COMMENT '应用ID（扫码时填入）',
    user_id BIGINT COMMENT '用户ID（扫码确认后填入）',
    openid VARCHAR(100) COMMENT '微信小程序openId',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待扫码，1-已扫码待确认，2-已确认，3-已过期',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    creator VARCHAR(50) COMMENT '创建人',
    creator_id VARCHAR(50) COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(50) COMMENT '更新人',
    updater_id VARCHAR(50) COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二维码登录记录表';

-- 用户会话表（多应用版本）
CREATE TABLE user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_id BIGINT COMMENT '应用ID',
    session_token VARCHAR(255) NOT NULL COMMENT '会话令牌',
    login_ip VARCHAR(45) COMMENT '登录IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    creator VARCHAR(50) COMMENT '创建人',
    creator_id VARCHAR(50) COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(50) COMMENT '更新人',
    updater_id VARCHAR(50) COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';

-- 创建索引
-- 应用表索引
CREATE INDEX idx_apps_app_id ON apps(app_id);
CREATE INDEX idx_apps_status ON apps(status);
CREATE INDEX idx_apps_create_time ON apps(create_time);

-- 用户表索引
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_create_time ON users(create_time);

-- 用户应用关联表索引
CREATE INDEX idx_user_apps_user_id ON user_apps(user_id);
CREATE INDEX idx_user_apps_app_id ON user_apps(app_id);
CREATE INDEX idx_user_apps_openid ON user_apps(openid);
CREATE INDEX idx_user_apps_unionid ON user_apps(unionid);

-- 二维码登录记录表索引
CREATE INDEX idx_qr_login_qrcode_id ON qr_login_records(qrcode_id);
CREATE INDEX idx_qr_login_app_id ON qr_login_records(app_id);
CREATE INDEX idx_qr_login_status ON qr_login_records(status);
CREATE INDEX idx_qr_login_expire_time ON qr_login_records(expire_time);

-- 用户会话表索引
CREATE INDEX idx_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_sessions_app_id ON user_sessions(app_id);
CREATE INDEX idx_sessions_expire_time ON user_sessions(expire_time);

-- 初始化数据
-- 插入示例应用数据
INSERT INTO apps (app_name, app_id, app_secret, description, creator) VALUES
('商城小程序', 'wx1234567890abcdef', 'secret1234567890abcdef', '电商购物小程序', 'Ravey'),
('服务小程序', 'wx0987654321fedcba', 'secret0987654321fedcba', '客户服务小程序', 'Ravey');

-- 提交事务
COMMIT;
