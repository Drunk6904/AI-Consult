## 一、MVP任务清单（按优先级排序）

**文档版本：** v3.1  
**修订日期：** 2025-03-08  
**修订说明：** 本次修订根据大模型部署方式差异调整了验收标准，并明确了已完成与待完成任务的边界。

---

### 部署模式说明

验收标准根据大模型部署方式有所差异：
- **API模式**：调用云端大模型API，响应快（2-5秒）
- **本地部署模式**：本地部署开源大模型，响应较慢（5-15秒，视硬件配置）

---

### 🔴 高优先级（P0 - Must Have）

| 任务ID | 任务名称 | 功能描述 | 输入 | 输出 | 涉及模块 | 验收标准 | 状态 |
|--------|----------|----------|------|------|----------|----------|------|
| **MVP-001** | 知识库基础搭建 | 支持PDF/Excel/Word上传，自动解析文本内容，查看知识库文档列表 | 多格式文档文件 | 结构化文本块+向量索引+文档列表 | 后端(解析引擎/向量DB)+前端 | 支持PDF/Excel/Word；解析成功率>90%；支持100页以上文档；提供文档列表接口 | ✅ 已完成 |
| **MVP-002** | RAG检索增强生成 | 基于知识库检索相关片段，结合LLM生成回答（通过Dify平台实现） | 用户query+知识库索引 | answer | 后端(Dify集成) | API模式响应<5秒；本地部署<12秒；返回引用来源；准确率>95% | ✅ 已完成 |
| **MVP-003** | Web聊天窗口 | 网页端悬浮窗，支持发送/接收消息 | 用户输入文本 | AI回复+引用来源展示 | 前端(Web SDK) | 加载<1秒；支持Markdown；移动端适配 | ✅ 已完成 |
| **MVP-004** | 用户注册/登录 | JWT鉴权，区分注册/未注册用户 | 手机号/密码或游客ID | token+用户状态 | 后端(认证服务)+DB | 登录响应<2秒；token有效期24h | ✅ 已完成 |
| **MVP-005** | 权限分级控制 | 未注册用户限制访问范围(仅30%公开知识) | 用户token+query | 过滤后的回答/提示注册 | 后端(权限中间件) | 未登录用户无法访问敏感信息；自动提示注册 | ✅ 已完成 |
| **MVP-006** | 问答日志记录 | 记录每次query/answer/confidence/sources | 问答完整数据 | 持久化日志记录 | 后端(日志服务)+DB | 记录完整率100%；支持检索 | ⏳ 待完成 |
| **MVP-007** | 数据洞察统计 | 总咨询量、TOP10问题、低置信度占比统计 | 日志数据 | 统计报表接口 | 后端(分析服务)+DB | 至少3个统计维度；数据实时性<5分钟 | ⏳ 待完成 |

---

### 🟡 中优先级（P1 - Should Have）

| 任务ID | 任务名称 | 功能描述 | 输入 | 输出 | 涉及模块 | 验收标准 | 状态 |
|--------|----------|----------|------|------|----------|----------|------|
| **MVP-008** | 微信公众号接入 | 对接微信API，实现菜单栏/对话窗智能回复 | 微信消息XML | AI回复(富文本/图文) | 后端(微信适配器) | API模式响应<3秒；本地部署<8秒；支持图文卡片；消息加解密 | ⏳ 待完成 |
| **MVP-009** | 用户画像关联 | 登录后加载用户Profile(订单历史/咨询记录) | user_id | 用户画像数据 | 后端(用户服务)+DB | 数据加载<3秒；支持手动补充 | ⏳ 待完成 |
| **MVP-010** | 转人工机制 | 置信度<95%或关键词触发转人工 | query+confidence+关键词匹配 | 转接信号+上下文包 | 后端(决策引擎) | 转接准确率>98%；耗时<2秒 | ⏳ 待完成 |
| **MVP-011** | 转人工信息打包 | 打包客户画像+对话历史+AI结果+置信度 | 当前会话全部数据 | 结构化转接信息 | 后端(会话服务) | 完整率100%；JSON结构化 | ⏳ 待完成 |
| **MVP-012** | 洞察数据可视化 | 前端展示统计图表(TOP10/趋势/占比) | 统计接口数据 | 可视化图表页面 | 前端(数据看板) | 页面加载<2秒；支持时间筛选 | ⏳ 待完成 |
| **MVP-013** | 对话历史连续 | 支持多轮对话，上下文记忆 | 多轮对话记录 | 关联回答 | 后端(会话管理) | 上下文记忆>5轮；API模式响应<5秒，本地部署<12秒 | ⏳ 待完成 |

---

### 🟢 低优先级（P2 - Nice to Have）

| 任务ID | 任务名称 | 功能描述 | 输入 | 输出 | 涉及模块 | 验收标准 | 状态 |
|--------|----------|----------|------|------|----------|----------|------|
| **MVP-014** | 渠道扩展设计 | 预留钉钉/企微等渠道接口结构 | 渠道配置参数 | 标准化接入协议 | 后端(渠道网关) | 新渠道接入<5人天；配置化接入 | 📋 规划中 |
| **MVP-015** | 个性化回复 | 基于用户等级/订单信息拼接个性化内容 | 用户画像+query | 差异化回答 | 后端(个性化引擎) | 同问题不同用户回答差异明显 | 📋 规划中 |
| **MVP-016** | 实时知识同步 | 文档更新后增量学习，实时同步 | 更新后的文档 | 增量索引更新 | 后端(索引服务) | 同步延迟<15分钟；支持版本对比 | 📋 规划中 |
| **MVP-017** | 话术模板生成 | 基于高频问题自动生成话术模板 | 历史问答数据 | 模板库 | 后端(NLP分析) | 模板覆盖率>70%；支持收藏 | 📋 规划中 |

---

## 二、关键接口列表

### 1. 知识库管理接口

```yaml
# 上传文档
POST /api/v1/knowledge/upload
Headers: Authorization: Bearer {token}
Content-Type: multipart/form-data

Request:
{
  "file": File,           # PDF/Excel/Word
  "category": "public",   # public/private
  "description": "产品价格表2025Q1"
}

Response:
{
  "code": 200,
  "data": {
    "doc_id": "doc_001",
    "status": "processing",  # processing/completed/failed
    "total_pages": 50,
    "parsed_chunks": 0
  }
}

# 查询文档解析状态
GET /api/v1/knowledge/doc/{doc_id}/status
Response:
{
  "code": 200,
  "data": {
    "doc_id": "doc_001",
    "status": "completed",
    "parsed_chunks": 120,
    "indexed_at": "2025-03-02T10:30:00Z"
  }
}

# 获取知识库文档列表
GET /api/v1/knowledge/documents
Headers: Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "documents": [
      {
        "id": "doc_001",
        "name": "产品价格表2025Q1.pdf",
        "size": 1024000,
        "status": "completed",
        "created_at": "2025-03-02T10:00:00Z",
        "parsed_chunks": 120
      },
      {
        "id": "doc_002",
        "name": "用户手册.pdf",
        "size": 2048000,
        "status": "completed",
        "created_at": "2025-03-01T15:30:00Z",
        "parsed_chunks": 250
      }
    ],
    "total": 2,
    "page": 1,
    "page_size": 10
  }
}
```

### 2. AI问答核心接口

```yaml
# 统一问答接口
POST /api/v1/ask
Headers: Authorization: Bearer {token}  # 未登录传临时token

Request:
{
  "query": "你们的企业版多少钱？",
  "session_id": "sess_001",     # 可选，用于多轮对话
  "channel": "web",             # web/wechat/app
  "user_context": {             # 登录用户自动填充
    "user_id": "u_123",
    "level": "vip",
    "order_history": ["prod_001"]
  }
}

Response:
{
  "code": 200,
  "data": {
    "answer": "企业版根据人数定价，10人以下¥999/月...",
    "confidence": 0.97,
    "sources": [
      {
        "doc_id": "doc_001",
        "doc_name": "产品价格表2025Q1.pdf",
        "page": 5,
        "snippet": "企业版定价：10人以下¥999/月，10-50人¥2999/月...",
        "relevance": 0.95
      }
    ],
    "session_id": "sess_001",
    "suggested_questions": ["如何购买？", "有折扣吗？"],
    "transfer_to_human": false    # 是否触发转人工
  }
}

# 流式回答接口（SSE）
POST /api/v1/ask/stream
Headers: Authorization: Bearer {token}
Accept: text/event-stream

Response: 
event: start
data: {"session_id": "sess_001"}

event: chunk  
data: {"content": "企业版", "index": 0}

event: source
data: {"doc_name": "产品价格表2025Q1.pdf", "page": 5}

event: end
data: {"confidence": 0.97, "complete": true}
```

### 3. 用户认证接口

```yaml
# 游客登录（未注册）
POST /api/v1/auth/guest
Request: {}
Response:
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "token_type": "Bearer",
    "expires_in": 86400,
    "user_type": "guest",
    "permissions": ["public_kb_read", "basic_ask"]
  }
}

# 注册用户登录
POST /api/v1/auth/login
Request:
{
  "phone": "13800138000",
  "password": "******",
  # 或微信code
  "wx_code": "xxx"
}

Response:
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user_type": "registered",
    "permissions": ["public_kb_read", "private_kb_read", "history_view", "profile_edit"],
    "profile": {
      "user_id": "u_123",
      "nickname": "张三",
      "level": "standard",
      "register_date": "2025-01-15",
      "consult_count": 15
    }
  }
}
```

### 4. 会话管理接口

```yaml
# 获取对话历史
GET /api/v1/chat/history?session_id=sess_001&limit=20
Response:
{
  "code": 200,
  "data": {
    "session_id": "sess_001",
    "messages": [
      {
        "role": "user",
        "content": "企业版多少钱？",
        "timestamp": "2025-03-02T10:00:00Z"
      },
      {
        "role": "assistant", 
        "content": "企业版根据人数定价...",
        "sources": [...],
        "confidence": 0.97,
        "timestamp": "2025-03-02T10:00:03Z"
      }
    ]
  }
}

# 转人工提交
POST /api/v1/chat/transfer
Request:
{
  "session_id": "sess_001",
  "reason": "low_confidence",  # low_confidence/keyword/user_request
  "context": {
    "current_query": "能打折吗？",
    "dialogue_history": [...],
    "user_profile": {...},
    "ai_results": {...},
    "confidence": 0.82
  }
}

Response:
{
  "code": 200,
  "data": {
    "transfer_id": "trans_001",
    "queue_position": 3,
    "estimated_wait": 120,  # 秒
    "agent_name": "客服小李",
    "status": "queued"      # queued/connected/closed
  }
}
```

### 5. 数据统计接口

```yaml
# 获取统计指标
GET /api/v1/analytics/dashboard?start_date=2025-03-01&end_date=2025-03-02
Headers: Authorization: Bearer {admin_token}

Response:
{
  "code": 200,
  "data": {
    "overview": {
      "total_queries": 1523,
      "unique_users": 456,
      "avg_response_time": 2.1,
      "human_transfer_rate": 0.08
    },
    "top_questions": [
      {"query": "企业版价格", "count": 128, "trend": "up"},
      {"query": "如何开通", "count": 95, "trend": "stable"}
    ],
    "low_confidence_queries": [
      {"query": "定制化开发", "confidence": 0.72, "count": 23}
    ],
    "hourly_distribution": [...]
  }
}

# 导出报表
POST /api/v1/analytics/export
Request:
{
  "report_type": "daily_summary",
  "date_range": ["2025-03-01", "2025-03-02"],
  "format": "excel"  # excel/pdf
}
Response: File download
```

### 6. 微信渠道接口（内部回调）

```yaml
# 微信消息接收（微信服务器→我方）
POST /webhook/wechat/message
Content-Type: application/xml

Request Body:
<xml>
  <ToUserName><![CDATA[gh_xxx]]></ToUserName>
  <FromUserName><![CDATA[openid_xxx]]></FromUserName>
  <CreateTime>123456789</CreateTime>
  <MsgType><![CDATA[text]]></MsgType>
  <Content><![CDATA[企业版多少钱？]]></Content>
  <MsgId>1234567890123456</MsgId>
</xml>

Response:
<xml>
  <ToUserName><![CDATA[openid_xxx]]></ToUserName>
  <FromUserName><![CDATA[gh_xxx]]></FromUserName>
  <CreateTime>123456789</CreateTime>
  <MsgType><![CDATA[news]]></MsgType>
  <ArticleCount>1</ArticleCount>
  <Articles>
    <item>
      <Title><![CDATA[企业版定价方案]]></Title>
      <Description><![CDATA[根据您的人数，推荐以下方案...]]></Description>
      <PicUrl><![CDATA[https://xxx/cover.jpg]]></PicUrl>
      <Url><![CDATA[https://xxx/detail]]></Url>
    </item>
  </Articles>
</xml>
```

---

## 三、MVP闭环流程图

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  文档上传    │────▶│  知识库解析  │────▶│  向量索引   │
│ (PDF/Excel) │     │  (文本分块)  │     │  (Embedding)│
└─────────────┘     └─────────────┘     └──────┬──────┘
                                                │
┌─────────────┐     ┌─────────────┐            │
│  数据洞察    │◀────│  日志记录    │◀───────────┘
│ (TOP10/统计)│     │(query/answer)│
└──────┬──────┘     └─────────────┘
       ▲
       │
┌──────┴──────┐     ┌─────────────┐     ┌─────────────┐
│  用户权限    │◀────│  Web/微信   │◀────│   RAG问答   │
│ (注册/未注册)│     │   聊天窗口   │     │(检索+生成)  │
└─────────────┘     └─────────────┘     └─────────────┘
```

---

## 四、Dify集成说明

由于我们使用了Dify平台来实现RAG功能，以下是集成的关键要点：

1. **Dify平台配置**：
   - 在Dify平台创建知识库和Chatflow
   - 获取Chatflow API Key和Dataset API Key
   - 配置知识库ID

2. **后端集成**：
   - 使用WebClient调用Dify API
   - 分离Chatflow和Dataset的API Key认证
   - 实现文档上传、删除、列表查询等功能
   - 实现基于Dify Chatflow的问答功能

3. **前端集成**：
   - 实现知识库文档列表展示
   - 实现Web聊天窗口
   - 集成用户认证功能

4. **接口适配**：
   - 提供符合规范的API接口
   - 确保响应格式符合要求
   - 支持会话管理和多轮对话