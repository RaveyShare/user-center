# 用户中心 API 文档

**作者**: Ravey  
**版本**: 1.0.0  
**更新时间**: 2024-12-30

## 1. 认证管理 (Auth)

Base Path: `/front/auth`

### 1.1 微信小程序登录

通过微信小程序登录凭证进行用户登录，获取 JWT Token。

- **接口地址**: `/wxMiniAppLogin`
- **请求方式**: `POST`
- **描述**: 微信小程序登录

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| appId | string | 是 | 小程序AppId |
| code | string | 是 | 微信授权码 (登录凭证) |
| encryptedData | string | 否 | 加密数据（用于获取UnionID等敏感信息） |
| iv | string | 否 | 加密算法的初始向量 |
| userInfo | object | 否 | 用户信息 |
| userInfo.nickname | string | 否 | 昵称 |
| userInfo.avatarUrl | string | 否 | 头像URL |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| token | string | JWT令牌 |
| expiresIn | long | 令牌过期时间（秒） |
| userInfo | object | 用户信息 |
| userInfo.id | long | 用户ID |
| userInfo.nickname | string | 昵称 |
| userInfo.avatarUrl | string | 头像URL |

### 1.2 生成扫码登录二维码

网页端生成二维码，用于小程序扫码登录。

- **接口地址**: `/qr/generate`
- **请求方式**: `POST`
- **描述**: 生成扫码登录二维码

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| appId | string | 是 | 应用ID |
| scene | string | 否 | 场景值 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| qrcodeId | string | 二维码ID (UUID) |
| expireAt | long | 过期时间戳 |
| qrContent | string | 二维码内容 |

### 1.3 查询二维码状态

网页端轮询二维码状态，确认后返回token。

- **接口地址**: `/qr/check`
- **请求方式**: `POST`
- **描述**: 查询二维码状态

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| qrcodeId | string | 是 | 二维码ID |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| status | integer | 状态码 |
| token | string | JWT令牌 (登录成功时返回) |
| userInfo | object | 用户信息 (登录成功时返回) |

### 1.4 小程序扫码上报

小程序扫码后上报二维码状态。

- **接口地址**: `/qr/scan`
- **请求方式**: `POST`
- **描述**: 小程序扫码上报

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| qrcodeId | string | 是 | 二维码ID |

#### 响应参数

无 (Void)

### 1.5 小程序确认登录

小程序在用户确认后提交登录确认。

- **接口地址**: `/qr/confirm`
- **请求方式**: `POST`
- **描述**: 小程序确认登录

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| qrcodeId | string | 是 | 二维码ID |

#### 响应参数

无 (Void)

### 1.6 生成小程序码

生成携带登录场景值的小程序码。

- **接口地址**: `/qr/wxacode`
- **请求方式**: `POST`
- **描述**: 生成小程序码

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| appId | string | 否 | 小程序AppId |
| qrcodeId | string | 否 | 二维码ID |
| page | string | 否 | 跳转页面 |
| width | integer | 否 | 宽度 |
| envVersion | string | 否 | 环境版本 |
| checkPath | boolean | 否 | 检查路径 |
| hyaline | boolean | 否 | 是否透明 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| qrcodeId | string | 二维码ID |
| expireAt | long | 过期时间戳 |
| imageBase64 | string | 图片Base64编码 |

### 1.7 发送邮箱验证码

向指定邮箱发送验证码。

- **接口地址**: `/email/sendCode`
- **请求方式**: `POST`
- **描述**: 发送邮箱验证码

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| email | string | 是 | 邮箱地址 |
| scene | integer | 是 | 场景：1-注册，2-登录，3-重置密码 |

#### 响应参数

无 (Void)

### 1.8 邮箱注册

通过邮箱和验证码注册新用户。

- **接口地址**: `/email/register`
- **请求方式**: `POST`
- **描述**: 邮箱注册

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| email | string | 是 | 邮箱地址 |
| password | string | 是 | 密码 |
| code | string | 是 | 验证码 |
| nickname | string | 否 | 昵称 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| token | string | JWT令牌 |
| expiresIn | long | 令牌过期时间（秒） |
| userInfo | object | 用户信息 |

### 1.9 邮箱登录

支持密码登录或验证码登录。

- **接口地址**: `/email/login`
- **请求方式**: `POST`
- **描述**: 邮箱登录

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| email | string | 是 | 邮箱地址 |
| password | string | 否 | 密码（密码登录时必填） |
| code | string | 否 | 验证码（验证码登录时必填） |
| loginType | integer | 是 | 登录方式：1-密码登录，2-验证码登录 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| token | string | JWT令牌 |
| expiresIn | long | 令牌过期时间（秒） |
| userInfo | object | 用户信息 |

### 1.10 重置密码

通过验证码验证后重置密码。

- **接口地址**: `/email/resetPassword`
- **请求方式**: `POST`
- **描述**: 重置密码

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| email | string | 是 | 邮箱地址 |
| newPassword | string | 是 | 新密码 |
| code | string | 是 | 验证码 |

#### 响应参数

无 (Void)

---

## 2. 用户管理 (Users)

Base Path: `/front/users`

### 2.1 获取当前用户信息

获取当前登录用户的详细信息。需携带 Token。

- **接口地址**: `/me`
- **请求方式**: `GET`
- **描述**: 获取当前用户信息

#### 请求参数

无

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | long | 用户ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| phone | string | 手机号 |
| email | string | 邮箱 |
| status | integer | 状态：1-正常，0-禁用 |

### 2.2 更新用户信息

更新当前用户的昵称和头像。

- **接口地址**: `/update`
- **请求方式**: `POST`
- **描述**: 更新用户信息

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| nickname | string | 否 | 昵称 |
| avatarUrl | string | 否 | 头像URL |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | long | 用户ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| phone | string | 手机号 |
| email | string | 邮箱 |
| status | integer | 状态：1-正常，0-禁用 |

### 2.3 上传头像

上传用户头像图片到OSS。

- **接口地址**: `/avatar/upload`
- **请求方式**: `POST`
- **描述**: 上传头像
- **Content-Type**: `multipart/form-data`

#### 请求参数

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| file | file | 是 | 图片文件 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| data | string | 完整的图片访问URL |

---

## 3. 应用管理 (Apps)

Base Path: `/admin/apps`

### 3.1 创建应用

创建新的应用。

- **接口地址**: `/createApp`
- **请求方式**: `POST`
- **描述**: 创建应用

#### 请求参数 (Body)

| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| appName | string | 是 | 应用名称 (max 100) |
| appId | string | 是 | 微信小程序AppId (max 100) |
| appSecret | string | 是 | 微信小程序AppSecret (max 255) |
| description | string | 否 | 应用描述 (max 500) |

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | long | 应用ID |
| appName | string | 应用名称 |
| appId | string | 微信小程序AppId |
| description | string | 应用描述 |
| status | integer | 状态：1-启用，0-禁用 |
| createTime | string | 创建时间 |
| updateTime | string | 更新时间 |

### 3.2 获取应用列表

获取所有应用的列表。

- **接口地址**: `/getAppList`
- **请求方式**: `GET`
- **描述**: 获取应用列表

#### 请求参数

无

#### 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| list | array | 应用列表 (参见创建应用响应) |
| total | long | 总数 |
