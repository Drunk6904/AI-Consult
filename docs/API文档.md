# AI智能客服系统 - API 文档

## 目录

1. [概述](#概述)
2. [认证方式](#认证方式)
3. [环境变量配置](#环境变量配置)
4. [API 端点](#api-端点)
   - [认证相关](#认证相关)
   - [用户管理](#用户管理)
   - [角色权限](#角色权限)
   - [知识库管理](#知识库管理)
   - [聊天问答](#聊天问答)
   - [Webhook](#webhook)
5. [错误码说明](#错误码说明)

---

## 概述

本文档详细描述了 AI 智能客服系统的所有 API 接口，包括请求格式、响应格式、认证方式和错误处理。

### 基础信息

- **基础 URL**: `http://localhost:8080`
- **API 版本**: `v1`
- **内容类型**: `application/json`
- **字符编码**: `UTF-8`

### 请求格式

所有请求（除文件上传外）应使用 JSON 格式：

```http
Content-Type: application/json
```

### 响应格式

标准响应格式：

```json
{
  "success": true,
  "data": { ... },
  "message": "操作成功"
}
```

错误响应格式：

```json
{
  "success": false,
  "message": "错误描述信息"
}
```

---

## 认证方式

系统使用 JWT（JSON Web Token）进行身份认证。

### 获取 Token

通过登录接口获取 JWT Token：

```http
POST /api/v1/auth/login
```

### 使用 Token

在请求头中添加 Authorization：

```http
Authorization: Bearer <your-jwt-token>
```

### Token 有效期

默认有效期为 2 小时（7200000 毫秒），可在环境变量中配置。

---

## 环境变量配置

### 必需环境变量

| 变量名 | 用途 | 示例值 |
|--------|------|--------|
| `DIFY_API_URL` | Dify API 基础 URL | `http://localhost/v1` |
| `DIFY_WORKFLOW_API_KEY` | Workflow API 密钥 | `app-xxx` |
| `DIFY_CHATFLOW_API_KEY` | Chatflow API 密钥 | `app-xxx` |
| `DIFY_DATASET_API_KEY` | Dataset API 密钥 | `dataset-xxx` |
| `DIFY_KNOWLEDGE_BASE_ID` | 公开知识库 ID | `uuid` |
| `DIFY_KNOWLEDGE_PRIVATE_BASE_ID` | 私有知识库 ID | `uuid` |
| `JWT_SECRET_KEY` | JWT 签名密钥（≥32字符） | `your-secret-key...` |

### 可选环境变量

| 变量名 | 用途 | 默认值 |
|--------|------|--------|
| `DIFY_WORKFLOW_ID` | 工作流 ID | - |
| `DIFY_WEBHOOK_URL` | Webhook 正式 URL | - |
| `DIFY_WEBHOOK_DEBUG_URL` | Webhook 调试 URL | - |
| `FEATURE_FLAG_WORKFLOW_TYPE` | 功能切换标志 | `workflow` |
| `JWT_EXPIRATION_TIME` | Token 过期时间(毫秒) | `7200000` |
| `DB_URL` | 数据库连接 URL | `jdbc:h2:mem:testdb` |
| `DB_USERNAME` | 数据库用户名 | `sa` |
| `DB_PASSWORD` | 数据库密码 | - |
| `MAX_FILE_SIZE` | 最大上传文件大小 | `100MB` |

### 功能切换配置

通过 `FEATURE_FLAG_WORKFLOW_TYPE` 环境变量控制使用 Dify Workflow 或 Chatflow：

- **`workflow`**（默认）：使用 Dify Workflow API
- **`chatflow`**：使用 Dify Chatflow API

---

## API 端点

### 认证相关

#### 1. 用户注册

```http
POST /api/v1/auth/register
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名（3-20字符） |
| email | string | 是 | 邮箱地址 |
| password | string | 是 | 密码（至少6位） |

**请求示例：**

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456"
}
```

**响应示例：**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "roles": ["USER"]
  },
  "message": "注册成功"
}
```

---

#### 2. 用户登录

```http
POST /api/v1/auth/login
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**请求示例：**

```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例：**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "roles": ["USER"],
      "permissions": ["chat:use", "knowledge:view"]
    }
  },
  "message": "登录成功"
}
```

---

### 用户管理

#### 3. 获取当前用户信息

```http
GET /api/v1/users/me
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "roles": ["USER"],
    "permissions": ["chat:use", "knowledge:view"]
  }
}
```

---

#### 4. 获取所有用户（管理员）

```http
GET /api/v1/users
Authorization: Bearer <token>
```

**权限要求：** `user:manage`

**响应示例：**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ADMIN"]
    }
  ]
}
```

---

#### 5. 更新用户信息

```http
PUT /api/v1/users/{id}
Authorization: Bearer <token>
```

**权限要求：** `user:manage` 或本人

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 否 | 用户名 |
| email | string | 否 | 邮箱 |
| password | string | 否 | 新密码 |

---

#### 6. 删除用户（管理员）

```http
DELETE /api/v1/users/{id}
Authorization: Bearer <token>
```

**权限要求：** `user:manage`

---

### 角色权限

#### 7. 获取所有角色

```http
GET /api/v1/roles
Authorization: Bearer <token>
```

**权限要求：** `role:manage`

**响应示例：**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "roleCode": "ADMIN",
      "roleName": "管理员",
      "permissions": ["user:manage", "role:manage", "knowledge:manage"]
    },
    {
      "id": 2,
      "roleCode": "USER",
      "roleName": "普通用户",
      "permissions": ["chat:use", "knowledge:view"]
    }
  ]
}
```

---

#### 8. 创建角色（管理员）

```http
POST /api/v1/roles
Authorization: Bearer <token>
```

**权限要求：** `role:manage`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleCode | string | 是 | 角色代码（大写） |
| roleName | string | 是 | 角色名称 |
| permissionIds | array | 否 | 权限ID列表 |

---

#### 9. 为用户分配角色（管理员）

```http
POST /api/v1/users/{userId}/roles
Authorization: Bearer <token>
```

**权限要求：** `role:manage`

**请求参数：**

```json
{
  "roleIds": [1, 2]
}
```

---

### 知识库管理

#### 10. 上传文档到知识库

```http
POST /api/v1/knowledge/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**权限要求：** `knowledge:manage`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 文档文件（PDF/Word/Excel） |
| isPublic | boolean | 否 | 是否上传到公开知识库（默认true） |

**响应示例：**

```json
{
  "success": true,
  "data": {
    "documentId": "doc-xxx",
    "fileName": "document.pdf",
    "status": "uploaded"
  },
  "message": "文档上传成功"
}
```

---

#### 11. 获取知识库文档列表

```http
GET /api/v1/knowledge/documents?isPublic=true
Authorization: Bearer <token>
```

**权限要求：** `knowledge:view`

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| isPublic | boolean | 否 | 是否查询公开知识库（默认true） |

**响应示例：**

```json
{
  "success": true,
  "data": {
    "documents": [
      {
        "id": "doc-xxx",
        "name": "document.pdf",
        "size": 1024000,
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 1
  }
}
```

---

#### 12. 删除知识库文档

```http
DELETE /api/v1/knowledge/documents/{documentId}?isPublic=true
Authorization: Bearer <token>
```

**权限要求：** `knowledge:manage`

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| isPublic | boolean | 否 | 是否从公开知识库删除（默认true） |

---

### 聊天问答

#### 13. 发送聊天消息

```http
POST /api/v1/chat/completions
Authorization: Bearer <token>（可选）
```

**权限要求：** 无（支持匿名访问）

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| query | string | 是 | 用户问题 |
| user | string | 否 | 用户ID（默认anonymous） |
| session_id | string | 否 | 会话ID |
| conversation_id | string | 否 | Dify 会话ID（UUID格式） |

**请求示例：**

```json
{
  "query": "你好，请问如何重置密码？",
  "user": "user123",
  "session_id": "session-abc",
  "conversation_id": "conv-uuid"
}
```

**响应示例：**

```json
{
  "success": true,
  "data": {
    "answer": "您可以通过以下步骤重置密码：...",
    "conversationId": "conv-uuid",
    "messageId": "msg-xxx",
    "session_id": "session-abc",
    "is_registered": true,
    "workflow_type": "workflow"
  }
}
```

**说明：**
- 系统根据 `FEATURE_FLAG_WORKFLOW_TYPE` 环境变量自动选择使用 Workflow 或 Chatflow
- 返回的 `workflow_type` 字段指示当前使用的模式

---

#### 14. 获取聊天配置

```http
POST /api/v1/chat/config
```

**响应示例：**

```json
{
  "success": true,
  "data": {
    "workflow_type": "workflow",
    "supported_types": ["workflow", "chatflow"]
  }
}
```

---

### Webhook

#### 15. 接收工作流回调

```http
POST /api/v1/webhook/workflow
```

**请求参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| query | string | 用户查询 |
| answer | string | AI 回答 |
| user_id | string | 用户ID |
| is_registered | boolean | 是否注册用户 |
| knowledge_base | string | 使用的知识库 |
| timestamp | number | 时间戳 |

---

#### 16. 发送 Webhook 请求

```http
POST /api/v1/webhook/send
Authorization: Bearer <token>
```

**权限要求：** `webhook:send`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| payload | object | 是 | Webhook 数据 |
| isDebug | boolean | 否 | 是否使用调试URL（默认false） |

---

## 错误码说明

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| JWT 配置错误：jwt.secret.key 未设置 | JWT 密钥未配置 | 在 .env 文件中设置 JWT_SECRET_KEY |
| JWT 配置错误：jwt.secret.key 长度不足 | JWT 密钥太短 | 使用至少32个字符的密钥 |
| DIFY_API_URL 未设置 | Dify API URL 未配置 | 在 .env 文件中设置 DIFY_API_URL |
| FEATURE_FLAG_WORKFLOW_TYPE 配置无效 | 功能切换标志无效 | 设置为 workflow 或 chatflow |
| Invalid username or password | 用户名或密码错误 | 检查登录凭据 |
| Token has expired | Token 已过期 | 重新登录获取新 Token |
| Access denied | 权限不足 | 检查用户角色权限 |

---

## 附录

### 预定义角色

| 角色代码 | 角色名称 | 权限 |
|----------|----------|------|
| ADMIN | 管理员 | 所有权限 |
| USER | 普通用户 | chat:use, knowledge:view |
| KNOWLEDGE_ADMIN | 知识库管理员 | knowledge:manage, knowledge:view |
| GUEST | 访客 | chat:use（公开知识库） |

### 预定义权限

| 权限代码 | 说明 |
|----------|------|
| user:manage | 用户管理 |
| role:manage | 角色管理 |
| permission:manage | 权限管理 |
| knowledge:manage | 知识库管理 |
| knowledge:view | 查看知识库 |
| chat:use | 使用聊天功能 |
| webhook:send | 发送 Webhook |
| system:config | 系统配置 |

---

**文档版本**: 1.0  
**更新日期**: 2026-03-10  
**维护者**: AI智能客服开发团队
