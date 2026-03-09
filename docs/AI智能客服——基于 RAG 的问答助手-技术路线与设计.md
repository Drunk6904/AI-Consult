# AI智能客服系统技术架构设计方案

**设计角色：** 软件架构师  
**文档版本：** v3.2  
**设计日期：** 2025-02-20  
**修订日期：** 2025-03-08

**修订说明：** 本次修订添加了部署模式说明、硬件配置推荐，并根据部署模式差异调整了性能指标。

---

## 零、部署模式与硬件配置

### 1. 大模型部署模式

本系统支持两种大模型部署方式：

| 部署模式 | 说明 | 适用场景 | 响应特点 | 成本模式 |
|---------|------|---------|---------|---------|
| **API模式** | 调用云端大模型API（Dify支持OpenAI、文心一言、通义千问等） | 追求快速响应、网络稳定的环境 | 响应快（2-5秒） | 按Token付费，初期成本低 |
| **本地部署模式** | 本地部署开源大模型（如Llama、Qwen、ChatGLM等） | 数据安全要求高、有GPU资源的环境 | 响应较慢（5-15秒） | 硬件投入一次性成本高，长期使用成本低 |

### 2. 硬件配置推荐（本地部署模式）

| 配置级别 | CPU | 内存 | GPU | 存储 | 适用模型规模 | 预估成本 |
|---------|-----|------|-----|------|-------------|---------|
| 最低配置 | 8核 | 32GB | RTX 3060 12GB | 500GB SSD | 7B参数模型 | ¥8,000-12,000 |
| 推荐配置 | 16核 | 64GB | RTX 4090 24GB | 1TB NVMe SSD | 13B-34B参数模型 | ¥25,000-35,000 |
| 高性能配置 | 32核 | 128GB | A100 80GB | 2TB NVMe SSD | 70B+参数模型 | ¥100,000+ |

### 3. 性能指标分级

| 指标 | API模式 | 本地部署模式（推荐配置） | 本地部署模式（最低配置） |
|------|---------|------------------------|------------------------|
| AI完整回答 | < 5秒 | < 12秒 | < 20秒 |
| 报价计算 | < 3秒 | < 8秒 | < 12秒 |
| 知识库查询 | < 3秒 | < 3秒 | < 3秒 |
| 转人工响应 | < 5秒 | < 5秒 | < 5秒 |

**说明：** 知识库查询和转人工响应不涉及LLM调用，因此不同部署模式性能一致。

---

## 一、技术栈选型清单

| 技术层级 | 技术选型 | 版本/规格 | 选型理由 | 成本估算 |
|---------|---------|----------|---------|---------|
| **前端框架** | Vue 3 | 3.5.25 | Composition API支持复杂状态管理 | 0元（开源） |
| **HTTP客户端** | Axios | 1.13.6 | 可靠的HTTP客户端，支持拦截器和错误处理 | 0元（开源） |
| **构建工具** | Vite | 7.3.1 | 快速的前端构建工具，支持热更新 | 0元（开源） |
| **后端框架** | Spring Boot | 4.0.3 | 生态成熟，易于开发和部署 | 0元（开源） |
| **JDK版本** | OpenJDK | 21 LTS | 虚拟线程支持高并发，ZGC降低GC停顿 | 0元（开源） |
| **数据库** | H2 | 2.1.214 | 内存数据库，适合开发和测试环境 | 0元（开源） |
| **ORM框架** | Spring Data JPA | 4.0.3 | 简化数据库操作，支持对象关系映射 | 0元（开源） |
| **安全框架** | Spring Security | 4.0.3 | 提供完整的认证和授权功能 | 0元（开源） |
| **认证方案** | JWT | 0.12.6 | 无状态认证，便于水平扩展 | 0元（开源） |
| **响应式编程** | Spring WebFlux | 4.0.3 | 非阻塞I/O，适合调用外部API | 0元（开源） |
| **文档解析** | PDFBox + POI | PDFBox 3.0.2 + POI 5.2.5 | 支持PDF、Word、Excel文档解析 | 0元（开源） |
| **AI引擎** | Dify + LLM API | Dify 0.5+ | 快速搭建RAG流程，支持多LLM切换（OpenAI/文心/通义） | Dify开源，LLM按Token付费 |
| **未来规划** | PostgreSQL | 16+ | 生产环境推荐，支持JSONB和向量扩展 | 0元（开源） |
| **未来规划** | Redis | 7.2+ | 用于缓存和会话管理 | 0元（开源） |
| **未来规划** | RabbitMQ | 3.12+ | 用于异步处理和消息队列 | 0元（开源） |

---

## 二、架构模式设计

### **整体架构：单体应用架构**

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          前端应用（Vue 3）                                │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌────────────┐ │
│  │   聊天界面    │   │  知识库管理   │   │  用户管理     │   │  角色管理  │ │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘   └──────┬─────┘ │
│         │                  │                  │                  │       │
└─────────┼──────────────────┼──────────────────┼──────────────────┼───────┘
          │                  │                  │                  │
┌─────────▼──────────────────▼──────────────────▼──────────────────▼────────┐
│                    后端应用（Spring Boot）                                 │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌────────────┐  │
│  │  认证授权     │   │   聊天服务    │   │   知识库服务 │   │  工作流回调 │  │
│  │  - JWT认证   │   │  - Dify调用   │   │  - 文档上传  │   │  - 结果处理 │  │
│  │  - 权限控制   │   │  - 会话管理   │   │  - 文档解析  │   │            │  │
│  └──────────────┘   └──────────────┘   └──────────────┘   └────────────┘  │
└───────────────────────────────────────────────────────────────────────────┘
          │                  │                │                  │
┌─────────▼──────┐  ┌────────▼──────┐  ┌──────▼─────────┐  ┌─────▼─────────┐
│     H2数据库    │  │   Dify平台    │  │  文件存储       │  │  外部API      │
│  - 用户数据     │  │  - RAG流程    │  │  - 文档上传     │  │  - 集成服务    │
│  - 会话数据     │  │  - LLM调用    │  │  - 临时存储     │  │               │
└────────────────┘  └───────────────┘  └────────────────┘  └───────────────┘
```

---

## 三、H2数据库表结构（开发环境）

### 说明：当前开发环境使用H2内存数据库，生产环境计划使用PostgreSQL

### 1. 客户基础信息表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| customer_id | VARCHAR(50) | PRIMARY KEY | 客户唯一ID |
| phone | VARCHAR(20) | UNIQUE, NOT NULL | 手机号（登录凭证） |
| email | VARCHAR(100) | | 邮箱 |
| password_hash | VARCHAR(255) | | 密码哈希（注册用户） |
| name | VARCHAR(100) | NOT NULL | 客户姓名 |
| company_name | VARCHAR(200) | | 公司名称 |
| registration_status | SMALLINT | NOT NULL DEFAULT 0 | 0=未注册 1=已注册 2=VIP |
| customer_level | VARCHAR(20) | DEFAULT '普通' | 客户等级：普通/铜牌/银牌/金牌 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 自动更新 |
| deleted_at | TIMESTAMP | NULL | 软删除标记 |

**索引**：phone, registration_status

---

### 2. 客户扩展档案表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| profile_id | BIGSERIAL | PRIMARY KEY | |
| customer_id | VARCHAR(50) | NOT NULL, REFERENCES customer(customer_id) ON DELETE CASCADE | |
| purchase_history | JSONB | | 购买历史：[{product_id, order_date, amount}] |
| consultation_tags | JSONB | | 咨询标签：["价格敏感","技术导向"] |
| prefer_contact_channel | VARCHAR(20) | DEFAULT '微信' | 偏好渠道 |
| last_order_amount | DECIMAL(12,2) | | 最后订单金额 |
| contract_expiry_date | DATE | | 合同到期日 |

**索引**：customer_id

---

### 3. 产品/服务目录表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| product_id | VARCHAR(50) | PRIMARY KEY | 产品唯一ID |
| product_name | VARCHAR(200) | NOT NULL | 产品名称 |
| version | VARCHAR(50) | NOT NULL | 版本：标准版/旗舰版/企业版 |
| price_monthly | DECIMAL(10,2) | | 月付价格 |
| price_annual | DECIMAL(10,2) | | 年付价格 |
| min_users | INT | | 最小用户数 |
| max_users | INT | | 最大用户数 |
| upgrade_policy | TEXT | | 升级政策文本 |
| discount_rules | JSONB | | 折扣规则：[{level, discount_rate}] |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |

**唯一索引**：product_name, version

---

### 4. 订单表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| order_id | VARCHAR(50) | PRIMARY KEY | 订单唯一ID |
| customer_id | VARCHAR(50) | NOT NULL, REFERENCES customer(customer_id) | |
| product_id | VARCHAR(50) | NOT NULL | 关联product |
| order_date | DATE | NOT NULL | |
| order_amount | DECIMAL(12,2) | NOT NULL | |
| user_count | INT | | 购买用户数 |
| payment_status | VARCHAR(20) | DEFAULT '已支付' | |
| contract_no | VARCHAR(100) | | 合同编号 |

**索引**：customer_id, order_date

---

### 5. 对话会话表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| conversation_id | VARCHAR(50) | PRIMARY KEY | 会话ID |
| customer_id | VARCHAR(50) | NOT NULL | 关联customer |
| channel | VARCHAR(20) | NOT NULL | 渠道：wechat/web/dingtalk |
| status | VARCHAR(20) | DEFAULT 'active' | active/closed/escalated |
| priority | SMALLINT | DEFAULT 0 | 0=普通 1=VIP |
| consultant_id | VARCHAR(50) | | 转人工后关联的顾问ID |
| started_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |
| ended_at | TIMESTAMP | NULL | |
| ai_handled | BOOLEAN | DEFAULT FALSE | 是否AI独立处理 |
| satisfaction_score | SMALLINT | | 满意度1-5 |

**索引**：customer_id, status, started_at

---

### 6. 消息明细表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| message_id | BIGSERIAL | PRIMARY KEY | |
| conversation_id | VARCHAR(50) | NOT NULL, REFERENCES conversation(conversation_id) ON DELETE CASCADE | |
| sender_type | VARCHAR(20) | NOT NULL | customer/ai/consultant |
| sender_id | VARCHAR(50) | | 发送者ID |
| message_type | VARCHAR(20) | DEFAULT 'text' | text/image/file |
| content | TEXT | NOT NULL | 消息内容 |
| metadata | JSONB | | 扩展信息：{confidence, sources, product_quoted} |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |

**索引**：conversation_id, created_at

---

### 7. 知识库文档表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| doc_id | VARCHAR(50) | PRIMARY KEY | 文档ID |
| doc_name | VARCHAR(255) | NOT NULL | 文件名 |
| doc_type | VARCHAR(20) | | pdf/excel/word/ppt |
| upload_path | VARCHAR(500) | | OSS存储路径 |
| file_size | BIGINT | | 文件大小 |
| category | VARCHAR(100) | | 分类：产品手册/报价单/政策 |
| access_level | SMALLINT | DEFAULT 0 | 0=公开 1=内部 2=高权限 |
| uploaded_by | VARCHAR(50) | | 上传人 |
| uploaded_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |
| vector_status | VARCHAR(20) | DEFAULT 'pending' | pending/indexed/failed |

**索引**：category, access_level

---

### 8. 知识库分块表（pgvector向量存储）
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| chunk_id | BIGSERIAL | PRIMARY KEY | |
| doc_id | VARCHAR(50) | NOT NULL, REFERENCES knowledge_base(doc_id) ON DELETE CASCADE | |
| chunk_seq | INT | NOT NULL | 分块序号 |
| content_snippet | TEXT | | 文本片段前200字 |
| embedding | vector(1536) | | **pgvector向量字段，1536维** |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |

**索引**：doc_id  
**向量索引**：embedding vector_cosine_ops（IVFFlat或HNSW）

---

### 9. 系统用户表（RBAC）
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| user_id | VARCHAR(50) | PRIMARY KEY | 用户ID（顾问/管理员） |
| username | VARCHAR(100) | UNIQUE, NOT NULL | |
| password_hash | VARCHAR(255) | NOT NULL | |
| real_name | VARCHAR(100) | NOT NULL | |
| role | VARCHAR(20) | NOT NULL | admin/consultant/supervisor |
| department | VARCHAR(100) | | 部门 |
| status | SMALLINT | DEFAULT 1 | 1=启用 0=禁用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |

---

### 10. 角色权限映射表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | |
| role | VARCHAR(20) | NOT NULL | 角色 |
| resource | VARCHAR(100) | NOT NULL | 资源：knowledge:view/customer:export |
| operation | VARCHAR(20) | NOT NULL | 操作：read/write/delete |

**唯一索引**：role, resource, operation

---

### 11. AI回答审计日志表（核心）
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| log_id | BIGSERIAL | PRIMARY KEY | |
| message_id | BIGINT | NOT NULL | 关联message表 |
| customer_id | VARCHAR(50) | NOT NULL | |
| query_text | TEXT | | 客户问题 |
| ai_response | TEXT | | AI回答 |
| confidence_score | DECIMAL(5,4) | | 置信度0-1 |
| knowledge_sources | JSONB | | 引用来源：[{doc_id, page}] |
| consultant_reviewed | BOOLEAN | DEFAULT FALSE | 是否人工复核 |
| review_result | VARCHAR(20) | | approved/rejected |
| reviewed_by | VARCHAR(50) | | 复核人 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |

**索引**：customer_id, confidence_score, created_at

---

### 12. 渠道配置表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| channel_id | VARCHAR(50) | PRIMARY KEY | 渠道ID |
| channel_type | VARCHAR(20) | NOT NULL | wechat/web/dingtalk |
| channel_name | VARCHAR(100) | NOT NULL | |
| webhook_url | VARCHAR(500) | | 接收消息的Webhook |
| api_key_encrypted | TEXT | | 加密的API密钥 |
| status | SMALLINT | DEFAULT 1 | 1=启用 0=禁用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |

**唯一索引**：channel_type, channel_name

---

### 13. 每日数据洞察表
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| report_id | BIGSERIAL | PRIMARY KEY | |
| report_date | DATE | NOT NULL | |
| total_conversations | INT | | 总会话数 |
| ai_handled_rate | DECIMAL(5,2) | | AI独立处理率% |
| avg_response_time | DECIMAL(10,2) | | 平均响应时长(秒) |
| hot_questions_top10 | JSONB | | TOP10问题 |
| consultant_escalation_top10 | JSONB | | 转人工TOP10问题 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | |

**唯一索引**：report_date

---

## 四、向量管理（Dify平台）

### 说明：当前使用Dify平台内置的向量管理功能，无需单独配置向量数据库

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           Dify平台                                     │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌────────────┐ │
│  │  知识库管理   │   │   向量索引    │   │   LLM调用    │   │  工作流管理  │ │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘   └──────┬─────┘ │
│         │                  │                  │                  │       │
└─────────┼──────────────────┼──────────────────┼──────────────────┼───────┘
          │                  │                  │                  │
┌─────────▼──────┐  ┌────────▼──────┐  ┌──────▼─────────┐  ┌─────▼─────────┐
│  文档上传      │  │  文本分块     │  │  向量生成      │  │  相似度检索   │
│  - PDF/Word    │  │  - 段落分割   │  │  - 嵌入模型    │  │  - TopK查询   │
│  - Excel/PPT   │  │  - 句子分割   │  │  - 维度：1536  │  │  - 阈值过滤   │
└─────────────────┘  └────────────────┘  └────────────────┘  └───────────────┘
```

---

## 五、核心变更点

| 原方案 | 修正后方案 |
|--------|-----------|
| 微服务架构 | **单体应用架构** |
| PostgreSQL 16+ | **H2内存数据库（开发环境）** |
| Milvus独立向量库 | **Dify平台内置向量管理** |
| 复杂分布式架构 | **简化单体架构** |

---

## 六、技术演进规划

### 1. 当前阶段（MVP）

**已完成功能：**
- ✅ 用户认证与授权（JWT）
- ✅ 知识库文档上传与管理
- ✅ Web聊天窗口
- ✅ Dify集成（RAG问答）
- ✅ 会话管理
- ✅ 权限分级控制

**当前技术栈：**
- 前端：Vue 3 + Axios + Vite
- 后端：Spring Boot 4.0.3 + Spring Security + Spring Data JPA
- 数据库：H2内存数据库（开发环境）
- AI引擎：Dify平台

### 2. 近期规划（P1功能）

**待完成功能：**
- ⏳ 微信公众号接入
- ⏳ 用户画像关联
- ⏳ 转人工机制
- ⏳ 数据洞察可视化
- ⏳ 多轮对话上下文记忆

**技术引入计划：**

| 技术 | 引入时机 | 引入原因 | 预期收益 |
|------|---------|---------|---------|
| **Redis** | 用户量>1000时 | 会话缓存、限流计数 | 提升响应速度，支持分布式会话 |
| **PostgreSQL** | 生产环境部署时 | 数据持久化、JSONB支持 | 数据安全，支持复杂查询 |
| **pgvector** | 知识库文档>10000时 | 向量检索优化 | 提升检索精度和速度 |

### 3. 中期规划（P2功能）

**功能扩展：**
- 钉钉/企业微信渠道接入
- 个性化回复
- 实时知识同步
- 话术模板生成

**技术引入计划：**

| 技术 | 引入时机 | 引入原因 | 预期收益 |
|------|---------|---------|---------|
| **RabbitMQ** | 多渠道并发>100时 | 异步消息处理、事件驱动 | 解耦系统组件，提升吞吐量 |
| **Elasticsearch** | 知识库文档>50000时 | 全文检索优化 | 提升搜索体验 |
| **监控体系** | 生产环境部署时 | Prometheus + Grafana | 系统可观测性 |

### 4. 技术选型决策树

```
                    ┌─────────────────────┐
                    │  是否需要数据持久化？ │
                    └──────────┬──────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
              ▼                                 ▼
        ┌─────────┐                      ┌─────────────┐
        │   是    │                      │     否      │
        └────┬────┘                      └──────┬──────┘
             │                                  │
             ▼                                  ▼
    ┌─────────────────┐                ┌─────────────┐
    │ PostgreSQL 16+  │                │ H2内存数据库 │
    └─────────────────┘                └─────────────┘

                    ┌─────────────────────┐
                    │  用户并发量是否>100？ │
                    └──────────┬──────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
              ▼                                 ▼
        ┌─────────┐                      ┌─────────────┐
        │   是    │                      │     否      │
        └────┬────┘                      └──────┬──────┘
             │                                  │
             ▼                                  ▼
    ┌─────────────────┐                ┌─────────────┐
    │ Redis缓存集群   │                │ 本地缓存    │
    └─────────────────┘                └─────────────┘

                    ┌─────────────────────┐
                    │ 是否需要异步处理？   │
                    └──────────┬──────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
              ▼                                 ▼
        ┌─────────┐                      ┌─────────────┐
        │   是    │                      │     否      │
        └────┬────┘                      └──────┬──────┘
             │                                  │
             ▼                                  ▼
    ┌─────────────────┐                ┌─────────────┐
    │ RabbitMQ消息队列 │                │ 同步处理    │
    └─────────────────┘                └─────────────┘
```

### 5. 部署架构演进

**阶段一：单机部署（当前）**
```
┌─────────────────────────────────────┐
│           单台服务器                 │
│  ┌─────────┐  ┌─────────┐  ┌──────┐ │
│  │ Vue前端 │  │ Spring  │  │  H2  │ │
│  │         │  │ Boot    │  │      │ │
│  └─────────┘  └─────────┘  └──────┘ │
│                │                     │
│           ┌────▼────┐               │
│           │  Dify   │               │
│           └─────────┘               │
└─────────────────────────────────────┘
```

**阶段二：生产环境部署**
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Nginx     │────▶│ Spring Boot │────▶│ PostgreSQL  │
│   (前端)    │     │   (后端)    │     │  (数据库)   │
└─────────────┘     └──────┬──────┘     └─────────────┘
                          │
                    ┌─────▼─────┐
                    │   Redis   │
                    │  (缓存)   │
                    └───────────┘
```

**阶段三：高可用部署**
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Nginx     │     │ Spring Boot │     │ PostgreSQL  │
│   (LB)      │────▶│   (集群)    │────▶│   (主从)    │
└─────────────┘     └──────┬──────┘     └─────────────┘
                          │
                    ┌─────▼─────┐
                    │  Redis    │
                    │  (集群)   │
                    └─────┬─────┘
                          │
                    ┌─────▼─────┐
                    │ RabbitMQ  │
                    │ (消息队列) │
                    └───────────┘
```