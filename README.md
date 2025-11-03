# User Center 用户中心

## 📖 项目简介

User Center 是一个基于 Spring Boot 的多应用用户中心系统，为多个小程序和应用提供统一的用户认证、授权和管理服务。支持微信小程序快速登录、二维码登录等多种认证方式。

## ✨ 功能特性

### 🔐 认证功能
- **微信小程序登录**：支持微信小程序一键授权登录
- **二维码登录**：支持扫码登录功能
- **JWT Token 管理**：安全的令牌生成和验证
- **多应用支持**：统一管理多个小程序应用

### 👥 用户管理
- **用户信息管理**：用户基本信息的增删改查
- **多应用关联**：用户可关联多个应用，支持 OpenID 和 UnionID
- **会话管理**：用户登录会话的管理和监控

### 🛠 管理功能
- **应用管理**：小程序应用的注册和配置管理
- **用户管理**：后台用户信息查看和管理
- **会话监控**：实时查看用户登录状态

## 🏗 技术栈

### 后端技术
- **框架**：Spring Boot 3.2.6
- **数据库**：MySQL 8.0
- **缓存**：Redis 6.0
- **ORM**：MyBatis Plus 3.5
- **认证**：JWT + 自定义过滤器
- **文档**：Swagger 3 (OpenAPI)
- **服务发现**：Nacos

### 开发工具
- **构建工具**：Maven 3.8+
- **JDK 版本**：Java 17
- **代码生成**：Lombok
- **JSON 处理**：FastJSON

## 📁 项目结构

```
user-center/
├── user-center-api/          # API 接口定义层
│   ├── dto/                  # 数据传输对象
│   ├── model/                # 请求响应模型
│   └── service/              # 服务接口定义
├── user-center-service/      # 业务逻辑实现层
│   ├── dao/                  # 数据访问层
│   ├── impl/                 # 服务实现
│   └── context/              # 上下文管理
├── user-center-web/          # Web 控制层
│   ├── controller/           # 控制器
│   └── filter/               # 过滤器
├── user-center-start/        # 启动模块
│   ├── config/               # 配置类
│   └── UserCenterApplication.java
└── doc/                      # 项目文档
    ├── api/                  # API 文档
    ├── architecture/         # 架构文档
    ├── sql/                  # 数据库脚本
    └── requirements/         # 需求文档
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/RaveyShare/user-center.git
cd user-center
```

2. **数据库初始化**
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE user_center DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始化脚本
mysql -u root -p user_center < doc/sql/init.sql
```

3. **配置文件**
```bash
# 复制配置文件模板
cp user-center-start/src/main/resources/application-dev.yml.template user-center-start/src/main/resources/application-dev.yml

# 修改数据库和 Redis 连接配置
vim user-center-start/src/main/resources/application-dev.yml
```

4. **编译运行**
```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run -pl user-center-start
```

5. **验证安装**
- 访问 Swagger 文档：http://localhost:8081/swagger-ui.html
- 健康检查：http://localhost:8081/actuator/health

## 📚 文档链接

- [API 接口文档](doc/api/README.md)
- [架构设计文档](doc/architecture/多应用架构技术设计文档.md)
- [数据库设计](doc/sql/README.md)
- [部署指南](doc/deployment/README.md)

## 🔧 开发指南

### 代码规范
- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一使用 `HttpResult` 作为响应格式
- API 层直接使用 Req/Resp 对象，避免不必要的转换

### 分支管理
- `main`：主分支，用于生产环境
- `develop`：开发分支，用于功能集成
- `feature/*`：功能分支，用于新功能开发
- `hotfix/*`：热修复分支，用于紧急修复

### 提交规范
```
feat: 新功能
fix: 修复问题
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👨‍💻 作者

**Ravey** - *项目维护者*

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue：[GitHub Issues](https://github.com/RaveyShare/user-center/issues)
- 邮箱：ravey@example.com

---

⭐ 如果这个项目对您有帮助，请给我们一个 Star！
