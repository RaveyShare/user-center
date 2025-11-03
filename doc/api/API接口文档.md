# API 接口文档

## 概述

本文档基于真实的 Controller 代码和请求/响应模型，描述了用户中心系统的 REST API 接口，包括认证、应用管理等功能模块。

## 基础信息

- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`

## 通用响应格式

所有 API 接口都使用统一的 HttpResult 响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1640995200000
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应状态码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据，具体结构见各接口说明 |
| timestamp | Long | 响应时间戳 |

## 认证方式

### JWT Token 认证

系统使用 JWT Token 进行用户认证，需要在请求头中携带：

```
Authorization: Bearer <token>
```

## API 接口列表

### 1. 认证模块 (/auth)

#### 1.1 微信小程序登录

**接口地址**: `POST /auth/wxMiniAppLogin`

**接口描述**: 通过微信小程序登录凭证进行用户登录

**Controller**: `AuthController.wxMiniAppLogin()`

**请求参数** (`MiniProgramLoginReq`):

```json
{
  "appId": "wx1234567890abcdef",
  "code": "0123456789abcdef",
  "userInfo": {
    "nickname": "用户昵称",
    "avatarUrl": "https://example.com/avatar.jpg"
  }
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 验证规则 | 说明 |
|--------|------|------|----------|------|
| appId | String | 是 | @NotBlank | 小程序AppId |
| code | String | 是 | @NotBlank | 微信授权码 |
| userInfo | Object | 否 | | 用户信息（可选） |
| userInfo.nickname | String | 否 | | 昵称 |
| userInfo.avatarUrl | String | 否 | | 头像URL |

**响应示例** (`MiniProgramLoginRes`):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "nickname": "用户昵称",
      "avatarUrl": "https://example.com/avatar.jpg"
    }
  },
  "timestamp": 1640995200000
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| token | String | JWT令牌 |
| expiresIn | Long | 令牌过期时间（秒），固定为86400（24小时） |
| userInfo | Object | 用户信息 |
| userInfo.id | Long | 用户ID |
| userInfo.nickname | String | 昵称 |
| userInfo.avatarUrl | String | 头像URL |

### 2. 应用管理模块 (/admin/apps)

#### 2.1 创建应用

**接口地址**: `POST /admin/apps/createApp`

**接口描述**: 创建新的应用

**Controller**: `AppsController.createApp()`

**请求参数** (`AppCreateReq`):

```json
{
  "appName": "测试小程序",
  "appId": "test-app-id-123",
  "appSecret": "test-app-secret-456",
  "description": "用于测试的微信小程序"
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 验证规则 | 说明 |
|--------|------|------|----------|------|
| appName | String | 是 | @NotBlank, @Size(max=100) | 应用名称 |
| appId | String | 是 | @NotBlank, @Size(max=100) | 微信小程序AppId |
| appSecret | String | 是 | @NotBlank, @Size(max=255) | 微信小程序AppSecret |
| description | String | 否 | @Size(max=500) | 应用描述 |

**响应示例** (`AppRes`):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "appName": "测试小程序",
    "appId": "test-app-id-123",
    "description": "用于测试的微信小程序",
    "status": 1,
    "createTime": "2023-01-01T12:00:00",
    "updateTime": "2023-01-01T12:00:00"
  },
  "timestamp": 1640995200000
}
```

#### 2.2 获取应用列表

**接口地址**: `GET /admin/apps/getAppList`

**接口描述**: 获取所有应用的列表

**Controller**: `AppsController.getAppList()`

**请求参数**: 无

**响应示例** (`AppListRes`):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "appName": "测试小程序",
        "appId": "test-app-id-123",
        "description": "用于测试的微信小程序",
        "status": 1,
        "createTime": "2023-01-01T12:00:00",
        "updateTime": "2023-01-01T12:00:00"
      }
    ],
    "total": 1
  },
  "timestamp": 1640995200000
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| list | Array | 应用列表 |
| total | Long | 总数 |

**AppRes 字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 应用ID |
| appName | String | 应用名称 |
| appId | String | 微信小程序AppId |
| description | String | 应用描述 |
| status | Integer | 状态：1-启用，0-禁用 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |



## 错误码说明

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，需要登录 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 参数校验失败 |
| 1002 | 应用不存在 |
| 1003 | 用户不存在 |
| 1004 | 登录凭证无效 |
| 1005 | Token 已过期 |
| 1006 | 应用已存在 |

## API 调用示例

### JavaScript 示例

```javascript
// 微信小程序登录
const loginData = {
  appId: 'test-app-id-123',
  code: '0123456789abcdef',
  userInfo: {
    nickname: '测试用户',
    avatarUrl: 'https://example.com/avatar.jpg'
  }
};

fetch('/auth/wxMiniAppLogin', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(loginData)
})
.then(response => response.json())
.then(data => {
  console.log('登录成功:', data);
  // 保存 token
  localStorage.setItem('token', data.data.token);
});

// 创建应用
const appData = {
  appName: '新应用',
  appId: 'new-app-id',
  appSecret: 'new-app-secret',
  description: '新创建的应用'
};

fetch('/admin/apps/createApp', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  },
  body: JSON.stringify(appData)
})
.then(response => response.json())
.then(data => {
  console.log('应用创建成功:', data);
});

// 获取应用列表
fetch('/admin/apps/getAppList', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
.then(response => response.json())
.then(data => {
  console.log('应用列表:', data);
});
```

### Java 示例

```java
// 使用 RestTemplate 调用 API
RestTemplate restTemplate = new RestTemplate();

// 微信小程序登录
MiniProgramLoginReq loginReq = new MiniProgramLoginReq();
loginReq.setAppId("test-app-id-123");
loginReq.setCode("0123456789abcdef");

MiniProgramLoginReq.UserInfo userInfo = new MiniProgramLoginReq.UserInfo();
userInfo.setNickname("测试用户");
userInfo.setAvatarUrl("https://example.com/avatar.jpg");
loginReq.setUserInfo(userInfo);

HttpResult<MiniProgramLoginRes> loginResult = restTemplate.postForObject(
    "/auth/wxMiniAppLogin", loginReq, HttpResult.class);

// 创建应用
AppCreateReq createReq = new AppCreateReq();
createReq.setAppName("新应用");
createReq.setAppId("new-app-id");
createReq.setAppSecret("new-app-secret");
createReq.setDescription("新创建的应用");

HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + token);
HttpEntity<AppCreateReq> entity = new HttpEntity<>(createReq, headers);

ResponseEntity<HttpResult> createResult = restTemplate.exchange(
    "/admin/apps/createApp", HttpMethod.POST, entity, HttpResult.class);

// 获取应用列表
HttpEntity<String> getEntity = new HttpEntity<>(headers);
ResponseEntity<HttpResult> appsResult = restTemplate.exchange(
    "/admin/apps/getAppList", HttpMethod.GET, getEntity, HttpResult.class);
```

### cURL 示例

```bash
# 微信小程序登录
curl -X POST http://localhost:8080/auth/wxMiniAppLogin \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "test-app-id-123",
    "code": "0123456789abcdef",
    "userInfo": {
      "nickname": "测试用户",
      "avatarUrl": "https://example.com/avatar.jpg"
    }
  }'

# 创建应用
curl -X POST http://localhost:8080/admin/apps/createApp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "appName": "新应用",
    "appId": "new-app-id",
    "appSecret": "new-app-secret",
    "description": "新创建的应用"
  }'

# 获取应用列表
curl -X GET http://localhost:8080/admin/apps/getAppList \
  -H "Authorization: Bearer <token>"
```

## 技术实现说明

### 项目结构
- **Controller 层**: 位于 `user-center-web` 模块
  - `AuthController`: 认证相关接口
  - `AppsController`: 应用管理接口
- **API 模型**: 位于 `user-center-api` 模块
  - 请求模型: `req` 包下
  - 响应模型: `res` 包下
- **统一响应**: 使用 `HttpResult` 包装所有响应

### 验证注解
- `@NotBlank`: 字符串不能为空
- `@Size`: 字符串长度限制
- `@Valid`: 启用参数验证

## 版本信息

- **当前版本**: v1.0.0
- **更新时间**: 基于真实代码生成
- **维护人员**: Ravey

## 联系方式

如有问题，请联系开发团队。

---

**作者：** Ravey  
**版本：** 1.0.0  
**更新时间：** 基于真实 Controller 和模型类生成