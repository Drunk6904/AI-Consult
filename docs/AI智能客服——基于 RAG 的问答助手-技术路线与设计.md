# AI智能客服系统技术架构设计方案

**设计角色：** 软件架构师  
**文档版本：** v1.1  
**设计日期：** 2025-02-20

---

## 一、技术栈选型清单

| 技术层级 | 技术选型 | 版本/规格 | 选型理由 | 成本估算 |
|---------|---------|----------|---------|---------|
| **前端框架** | Vue 3 + TypeScript | Vue 3.4 + TS 5.3 | Composition API支持复杂状态管理，TS保障类型安全 | 0元（开源） |
| **UI组件库** | Element Plus | 2.4+ | 企业级组件丰富，支持暗黑模式，与Vue3生态完美整合 | 0元（开源） |
| **状态管理** | Pinia | 2.1+ | 替代Vuex，更轻量、更直观的响应式管理 | 0元（开源） |
| **后端框架** | Spring Boot | 3.2+ | 微服务首选，生态成熟，AI集成库丰富（LangChain4j） | 0元（开源） |
| **JDK版本** | OpenJDK | 21 LTS | 虚拟线程支持高并发，ZGC降低GC停顿 | 0元（开源） |
| **数据库** | **PostgreSQL** | **16+** | **支持JSONB字段、全文检索、向量扩展（pgvector），AI场景更优** | 0元（开源） |
| **缓存层** | Redis | 7.2+ | 多级缓存：热点知识库、会话状态、限流计数 | 0元（开源） |
| **向量数据库** | **pgvector** | **0.6+** | **PostgreSQL插件，无需独立部署，RAG检索一体化** | 0元（开源） |
| **消息队列** | RabbitMQ | 3.12+ | 异步处理AI生成、日志写入、CRM同步，削峰填谷 | 0元（开源） |
| **API网关** | Spring Cloud Gateway | 4.1+ | 统一入口、路由、限流、鉴权 | 0元（开源） |
| **服务注册** | Nacos | 2.3+ | 服务发现与配置中心，阿里开源，中文文档友好 | 0元（开源） |
| **AI引擎** | Dify + LLM API | Dify 0.5+ | 快速搭建RAG流程，支持多LLM切换（OpenAI/文心/通义） | Dify开源，LLM按Token付费 |
| **云服务** | 阿里云/腾讯云 | ECS 4C8G | 生产环境推荐云主机，含SLB负载均衡、OSS存储 | 约5000-8000元/月 |
| **监控告警** | Prometheus + Grafana | 2.45+ | 全链路监控，AI响应时长、准确率实时看板 | 0元（开源） |

---

## 二、架构模式设计

### **整体架构：微服务 + 事件驱动**

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          API Gateway (Spring Cloud)                      │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌────────────┐ │
│  │   微信渠道    │   │   网页渠道   │   │   钉钉渠道    │   │  管理后台  │ │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘   └──────┬─────┘ │
│         │                  │                  │                  │       │
└─────────┼──────────────────┼──────────────────┼──────────────────┼───────┘
          │                  │                  │                  │
┌─────────▼──────────────────▼──────────────────▼──────────────────▼────────┐
│                    服务编排层（Spring Cloud Gateway）                      │
│  统一鉴权 · 负载均衡 · 限流熔断 · 日志追踪 · 协议转换                        │
└─────────┬──────────────────┬──────────────────┬──────────────────┬────────┘
          │                  │                  │                  │
┌─────────▼──────┐  ┌────────▼──────┐  ┌──────▼─────────┐  ┌─────▼─────────┐
│  会话服务       │  │   AI核心服务    │  │   知识库服务    │  │  CRM集成服务   │
│  - 会话管理     │  │  - RAG检索      │  │  - 文档上传     │  │  - 客户查询    │
│  - 消息持久化   │  │  - LLM调用      │  │  - 向量索引     │  │  - 订单同步    │
│  - 转人工策略   │  │  - 置信度评估   │  │  - 权限控制     │  │  - 回调通知    │
└─────────┬───────┘  └────────┬──────┘  └────────┬───────┘  └──────┬────────┘
          │                   │                   │                  │
┌─────────▼──────┐  ┌────────▼──────┐  ┌──────▼─────────┐  ┌─────▼─────────┐
│  PostgreSQL    │  │   Redis集群    │  │  pgvector      │  │  RabbitMQ     │
│  - 客户数据     │  │  - 会话缓存    │  │  - 向量存储     │  │  - 异步队列   │
│  - 订单数据     │  │  - 限流计数    │  │  - 相似度检索   │  │  - 事件总线   │
└─────────────────┘  └────────────────┘  └────────────────┘  └───────────────┘
```

---

## 三、PostgreSQL数据库表结构

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

## 四、pgvector扩展配置

```sql
-- 启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 知识库分块表创建向量索引（HNSW算法，更快但占用更多内存）
CREATE INDEX idx_knowledge_chunk_embedding ON knowledge_chunk 
USING hnsw (embedding vector_cosine_ops) 
WITH (m = 16, ef_construction = 64);

-- 向量相似度检索示例（RAG核心查询）
SELECT chunk_id, content_snippet, 
       1 - (embedding <=> query_embedding) AS similarity
FROM knowledge_chunk
WHERE doc_id IN (SELECT doc_id FROM knowledge_base WHERE access_level <= user_level)
ORDER BY embedding <=> query_embedding
LIMIT 5;
```

---

## 五、核心变更点

| 原方案 | 修正后方案 |
|--------|-----------|
| MySQL 8.0 | **PostgreSQL 16+** |
| Milvus独立向量库 | **pgvector插件（一体化）** |
| JSON字段 | **JSONB字段（支持索引、全文检索）** |
| 向量检索跨服务 | **同一数据库内完成RAG检索** |