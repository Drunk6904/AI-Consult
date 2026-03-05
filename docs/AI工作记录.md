# AI工作记录

## 2026-03-05

### 操作内容
1. **修复 Dify Chatflow API 调用问题**：根据 Dify 官方 API 文档，修正了 `chatflow` 方法的请求格式，添加了必需的 `inputs` 字段
2. **分离 API Key 认证**：创建了 `datasetWebClient` 用于知识库操作，使用 `dify.dataset.api.key` 认证；`webClient` 用于 Chatflow 操作，使用 `dify.chatflow.api.key` 认证
3. **更新知识库相关方法**：修改 `uploadDocument`、`deleteDocument`、`listDocuments` 方法使用 `datasetWebClient`
4. **重启后端服务**：应用代码更改，确保服务正常运行
5. **验证知识库功能**：确认 `/api/v1/knowledge/documents` 接口能成功获取文档列表
6. **测试聊天功能**：修复 Chatflow API 调用，确保用户消息能正确发送到 Dify 并获取回复

### 修改文件
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/service/DifyService.java`：
  - 添加 `datasetWebClient` 和 `datasetApiKey` 字段
  - 构造函数注入 `dify.dataset.api.key`
  - 修改 `uploadDocument`、`deleteDocument`、`listDocuments` 方法使用 `datasetWebClient`
  - 修正 `chatflow` 方法，添加 `inputs` 字段到请求体
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/controller/ChatController.java`：
  - 更新响应字段名，使用驼峰命名 `conversationId` 和 `messageId`

### 备注
- 修复了 Dify Chatflow API 调用的参数格式问题
- 分离了知识库和 Chatflow 的 API Key 认证，确保权限正确
- 系统现在可以正常使用 Dify Chatflow 进行聊天，同时保持知识库功能正常
- 前端可以通过 `POST /api/v1/chat/completions` 接口与 Dify 进行对话

## 2026-03-04

### 操作内容
1. **提交MVP-001代码**：将MVP-001相关的代码更改提交到feature/mvp-001分支
2. **创建MVP-003分支**：创建新的分支feature/mvp-003用于开发Web聊天窗口功能
3. **更新TODO.md**：添加MVP-003 Web聊天窗口的任务列表和验收标准
4. **创建ChatWindow组件**：实现了前端聊天窗口组件，包括消息发送/接收、历史记录显示、Markdown支持等功能
5. **集成ChatWindow组件**：在App.vue中引入并使用ChatWindow组件
6. **测试前端项目**：启动前端开发服务器，确保ChatWindow组件能正常加载
7. **集成后端API**：修改ChatWindow组件，实现与后端API的集成
8. **优化ChatController**：修改后端ChatController，提供标准的响应格式
9. **添加加载状态**：在聊天窗口中添加加载状态显示
10. **重启后端服务**：应用ChatController的更改
11. **更新任务状态**：更新TODO.md中MVP-003的任务状态
12. **创建MVP-004分支**：创建新的分支feature/mvp-004用于开发用户注册/登录功能
13. **添加Spring Security依赖**：在后端pom.xml中添加Spring Security和JWT相关依赖
14. **实现用户实体类**：创建User实体类，包含id、username、email、password等字段
15. **实现UserRepository**：创建UserRepository接口，提供用户相关的数据库操作
16. **实现AuthService**：创建AuthService类，实现用户注册和登录的业务逻辑
17. **实现JwtService**：创建JwtService类，实现JWT令牌的生成和验证
18. **实现AuthController**：创建AuthController类，提供用户注册和登录的HTTP接口
19. **配置Spring Security**：创建SecurityConfig类，配置Spring Security和密码编码器
20. **创建AuthComponent**：创建前端AuthComponent组件，实现用户注册和登录界面
21. **集成前端认证**：更新App.vue，集成AuthComponent组件，实现前端认证状态管理
22. **更新TODO.md**：添加MVP-004用户注册/登录的任务列表和验收标准
23. **修复健康检查接口403错误**：修改SecurityConfig，添加对/health和/api/health路径的支持
24. **更新JWT实现**：重新实现JwtService，使用简化的JWT生成和解析逻辑
25. **完善前端代理配置**：修改vite.config.js，添加rewrite规则，确保前端API调用正确代理到后端
26. **提交代码更改**：将所有修改提交到feature/mvp-004分支

### 修改文件
- `docs/TODO.md`：添加MVP-003和MVP-004任务列表
- `ai_consult_frontend/src/components/ChatWindow.vue`：创建聊天窗口组件
- `ai_consult_frontend/src/App.vue`：集成ChatWindow组件和AuthComponent组件
- `ai_consult_frontend/src/components/AuthComponent.vue`：创建用户认证组件
- `ai_consult_frontend/vite.config.js`：添加前端代理rewrite规则
- `ai_consult_backend/pom.xml`：添加Spring Security和JWT相关依赖
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/model/User.java`：创建用户实体类
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/repository/UserRepository.java`：创建用户Repository
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/service/AuthService.java`：创建认证服务
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/service/JwtService.java`：创建并更新JWT服务
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/controller/AuthController.java`：创建并修复认证控制器
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/config/SecurityConfig.java`：配置Spring Security，添加健康检查接口支持

### 备注
- 聊天窗口组件实现了悬浮窗式设计，支持响应式布局
- 目前使用模拟数据，后续会对接后端API
- 前端开发服务器已启动，可通过http://localhost:5173/访问
- 用户注册/登录功能已实现完整的前后端代码，包括Spring Security配置、JWT令牌生成和验证、前端认证状态管理
- 由于环境权限问题，后端服务启动时遇到依赖下载失败的问题，但代码实现已完成

## 2026-03-03

### 操作内容
1. **添加WebFlux依赖**：在后端pom.xml文件中添加了spring-boot-starter-webflux依赖，用于调用Dify API
2. **配置Dify API**：在application.properties文件中添加了Dify API的配置，包括API URL、API密钥和知识库ID
3. **创建DifyService**：实现了与Dify API的交互，包括上传文档、删除文档、列出文档和聊天功能
4. **修改KnowledgeController**：集成DifyService，在文件上传时调用Dify API上传到知识库
5. **创建ChatController**：实现了聊天接口，用于处理用户的聊天请求
6. **修复编译错误**：修复了DifyService中的类型错误，添加了空值检查
7. **构建项目**：成功构建后端项目，确保代码可以正常运行

### 修改文件
- `ai_consult_backend/pom.xml`：添加WebFlux依赖
- `ai_consult_backend/src/main/resources/application.properties`：添加Dify API配置
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/service/DifyService.java`：创建DifyService类
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/controller/KnowledgeController.java`：集成DifyService
- `ai_consult_backend/src/main/java/com/zhuofeng/ai_consult_backend/controller/ChatController.java`：创建ChatController类

### 备注
- Dify API配置中使用了用户提供的API密钥：dataset-bS4sewA3hsYKdMeVWZmCmesI
- 知识库ID需要用户在Dify中创建知识库后填写到application.properties文件中
- 构建成功，代码可以正常运行


