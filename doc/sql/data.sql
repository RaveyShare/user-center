-- 插入测试应用数据
INSERT INTO apps (id, app_name, app_id, app_secret, description, status) VALUES 
(1, '测试小程序', 'test-app-id-123', 'test-app-secret-456', '用于测试的微信小程序', 1);

-- 插入测试用户数据
INSERT INTO users (id, nickname, avatar_url, status) VALUES 
(1, '测试用户', 'https://example.com/avatar.jpg', 1);