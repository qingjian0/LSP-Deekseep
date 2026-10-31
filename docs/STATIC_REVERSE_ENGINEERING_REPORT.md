# Android Xposed 模块静态逆向阶段报告

日期：2026-10-31  
分析对象：用户上传的 `模块.apk.zip`  
分析方式：ZIP/DEX/ELF/AXML 静态检查 + 对照作者公开的 Deekseep v1.7.5 源码与文档。未在设备上运行 APK。

## 1. 文件身份与版本

| 项目 | 已确认结果 |
|---|---|
| APK 包名 | `com.dsmod.probe` |
| Manifest `versionName` | `1.7.6` |
| Manifest `versionCode` | `36` |
| Manifest 应用标签 | `Nexus` |
| Xposed 入口 | `com.dsmod.probe.Main`（`assets/xposed_init`） |
| Xposed 最低接口 | 82 |
| 目标应用 | `com.deepseek.chat` |
| minSdk / targetSdk | 24 / 34 |
| DEX | `classes.dex` 约 4.7 MB；`classes2.dex` 约 8.3 MB |

**名称存在差异：** APK Manifest 的产品标签为 `Nexus`，DEX 中也有 `NexusUi`、`Nexus-MCP` 等标识；但包内 `LETTER_TO_DEVELOPERS.txt` 明确署名 Deekseep、作者 `@lllucccian`，并指向 `https://github.com/lllucccian/Deekseep`。这说明 APK 至少包含 Deekseep 的代码/版权声明，但仅凭当前证据不能断定它与公开仓库的分支、打包者或品牌关系。

该 Letter 自称 GPL-3.0，并要求保留署名和许可证。公开仓库 README 也说明公开版为 v1.7.5，Local API 与闭源保护载荷不在公开源码中。上传 APK 的 Manifest 是 v1.7.6；DEX 字符串还出现 `1.7.6 Closed`、DeepSeek `2.5.3/code275`。因此它不是公开 v1.7.5 Open APK 的同一构建。

## 2. Android 注入与组件结构

- `assets/xposed_init` 指向 `com.dsmod.probe.Main`。
- Manifest 标记 `xposedmodule=true`、`xposedminversion=82`，并将 `com.deepseek.chat` 纳入目标范围。
- `Main` 实现传统 Xposed `IXposedHookLoadPackage`，并将 `com.deepseek.chat` 作为目标包。
- 模块自身另有设置 Activity、进程管理 Activity、Agent 延时 Activity/Receiver、前台服务及本地语音控制组件。
- 前台服务组件负责维持用户主动启用的本机 API/SSE 可用性；Manifest 的 FGS subtype 描述也明确提及 DeepSeek localhost API。

这不是把 DeepSeek APK 改造成独立客户端，而是将模块代码注入 DeepSeek 进程，再由模块控制宿主内部的请求、消息和工具流程。

## 3. Local API：可确认的协议面

### 3.1 地址和认证

公开源码中的本地 API 地址回退值为：

`http://127.0.0.1:8765/v1`

其中 `8765` 是源码中用于状态未返回端口时的默认显示/回退端口；实际端口可配置。APK 字符串还明确区分“仅本机回环”和“本机 + 局域网”监听模式。局域网模式同样要求 API Key。

公开源码 `z3.java` 对混淆字符串的解码结果给出以下路径/认证标识：

| 用途 | 路径或字段 |
|---|---|
| OpenAI Chat Completions | `POST /v1/chat/completions` |
| OpenAI Responses | `POST /v1/responses` |
| Anthropic Messages | `POST /v1/messages` |
| Anthropic Token Count | `POST /v1/messages/count_tokens` |
| Models | `GET /v1/models` |
| Models 子路径 | `/v1/models/` |
| User Balance | `/v1/user/balance` |
| OpenAI 风格认证头 | `Authorization: Bearer <key>` |
| Anthropic 风格认证头 | `x-api-key` |

协议选择是显式的：OpenAI 模式提供 `/models`、`/chat/completions`、`/responses`；Anthropic 模式提供 `/v1/messages`、`/v1/messages/count_tokens`，并支持 JSON、SSE、`tool_use` / `tool_result` 和 thinking 参数。协议不匹配会返回错误，而不是将两套请求格式混用。

### 3.2 主 DEX 与闭源载荷的边界

公开源码中的 `z2` 定义了 Local API 与载荷之间的 ABI 合约：

- `LOCAL_API_PAYLOAD_ABI = 4`
- `Backend.isReady()` / `readinessDetail()` / `complete(...)`
- `CompletionRequest` 包含模型、prompt、reasoning、search、max output tokens、图片/文件、工具计划、Responses continuation、会话 scope、原生 conversation/message ID 等字段。
- `DeltaSink` 将文本和 reasoning 增量流式回传，并处理取消、已满足条件和已发布文本快照。

源码注释明确说明：HTTP Server 位于独立编译、受保护的 Local API payload 中；主 DEX 的 `Main` 实现 `Backend` 并驱动 DeepSeek 原生 transport。公开版 `z15` 是空 stub；闭源版才加载 native `shi` 核心并提供受保护字符串/载荷能力。

**因此：** 已确认的是 API 路由和主 DEX/载荷接口契约；尚未确认的是闭源 HTTP Server 的每个 handler、完整 JSON schema、所有状态码、请求队列细节及其 native 实现。

## 4. Agent 执行链

公开源码可确认 Agent 不是简单的 prompt 模板，而是一条由模型输出控制块、Hook 解析、工具执行和结果回注组成的执行管线。

### 4.1 模型到工具

模型回复中的内部控制协议使用以下边界标记：

- `[[DEEKSEEP_LOCAL_TOOLS_V1]]` / `[[/DEEKSEEP_LOCAL_TOOLS_V1]]`
- `[[DEEKSEEP_LOCAL_TOOL_RESULT_V1]]` / `[[/DEEKSEEP_LOCAL_TOOL_RESULT_V1]]`
- `[[DEEKSEEP_ANONYMOUS_HEARTBEAT_EVENT_V1]]` / `[[/DEEKSEEP_ANONYMOUS_HEARTBEAT_EVENT_V1]]`

`HeartbeatToolProtocol` 在回复流中识别并验证这些结构，隐藏内部控制内容，只有完整且参数合法的 JSON 调用才进入执行器。单次响应最多接受 4 个调用，并有路径、内容长度、Shell 命令、网络请求、超时等限制。

### 4.2 已公开的内置工具类别

- 时间/计划：`get_current_time`、`schedule_once`、`set_plan`、`clear_plan`、`set_interval`、`bind_chat`、`cancel_heartbeat`、`delay`
- 屏幕/UI：`capture_screen`、`tap_screen`、`swipe_screen`、`press_back`、`screen_power`、`ask_user`、`render_rich_panel`
- 文件：`read_file`、`write_file`、`delete_file`、`transfer_file`
- 网络/搜索：`network_request`、`search_web`
- 系统/应用：`shell`、`open_app`、`list_apps`、`app_info`、`uninstall_app`
- 多媒体/扩展：`music`、`mcp`，以及 Java 插件提供的动态 Agent 工具

### 4.3 执行后端与持久化

`AgentToolConfig` 定义三种后端：`in_app`、`root`、`shizuku`。`AgentDeviceBridge` 负责设备、文件、截图和 Shell 操作；受保护构建还包含工作区/Termux 环境路径。`AgentRunStore` 与 outbox 负责持久化执行状态和结果回传；源码明确避免在进程重启后重复执行可能产生副作用的操作。

## 5. MCP：它是客户端，不等于本地 MCP Server

公开源码中的 `AgentMcpManager` 明确标注为“in-chat Agent 使用的独立 MCP Streamable-HTTP client”。

- 工具名统一使用 `mcp__` 前缀。
- 支持 `streamable_http` 与传统 `sse`。
- 配置保存在 DeepSeek 私有目录 `deekseep_agent/mcp.json`。
- 会连接外部 MCP 服务、发现工具、维护会话/请求 ID，并将工具同步给当前 Agent。
- Streamable HTTP 请求使用 `Accept: application/json, text/event-stream` 和 `MCP-Protocol-Version: 2025-03-26`。

因此不能把它误判成“APK 自己启动一个 MCP Server”。它的公开实现主要是 Agent 到外部 MCP 服务的客户端连接层。

## 6. 独立语音 API

APK 字符串还出现以下路径：

- `/v1/audio/formats`
- `/v1/audio/speech`
- `/v1/audio/message-speech`
- `/v1/audio/voices`

界面文案明确表示该语音服务只支持 DeepSeek 2.5.3/code275，拥有独立的服务开关、API Key 和监听端口，默认监听 `127.0.0.1`，与 Local API 分别启停、分别配置。不要把语音服务的 API Key/端口与主 Local API 混用。

## 7. 原生库初步清点

ARM64 包含：

- `libnesec.so`（约 3.6 KB，strip）
- `libshi.so`（约 319 KB，strip）
- `libnexus_cxx.so`（约 1.5 MB，strip）
- `libnexus_audio.so`（约 260 KB，含 JNI 导出 `Java_com_dsmod_probe_V253BundledAudioEncoder_nativeEncodeAudio`）
- `libsmspatch.so`（约 7.3 KB，strip）
- `libandroidx.graphics.path.so`

当前只完成 ELF 元信息和符号初查，尚未做 ARM64 反汇编、JNI 调用点映射或载荷解密。`libshi.so` 与 `z15` 的关系在公开源码中有明确线索，但不能仅凭文件名推断其所有功能。

## 8. 当前确认 / 推断 / 未完成

### 已确认

1. APK 是 `com.dsmod.probe` 的 Xposed 模块，入口 `com.dsmod.probe.Main`，目标 `com.deepseek.chat`。
2. Manifest 为 v1.7.6/versionCode 36，界面标签 Nexus；包内版权信署名 Deekseep。
3. Local API 的 OpenAI/Anthropic 路由、认证头、默认地址回退值和主 DEX/payload ABI 可从公开源码交叉确认。
4. Agent 的控制标记、工具类别、Root/Shizuku/in-app 后端和持久化回传设计可从公开源码确认。
5. MCP 实现是外部 MCP 服务客户端，支持 Streamable HTTP/SSE。
6. 语音 API 是与主 Local API 分离的服务。

### 合理推断（尚未由完整反汇编证明）

1. `libshi.so` / `libnesec.so` 与 Closed 版保护载荷、字符串解码或 Local API payload 的加载/保护链相关。
2. APK 中出现的 `deekseep-1.7.5-closed-cloud-v1`、Nexus 命名与 Deekseep 版权信，可能反映同一代码谱系中的品牌/构建差异；目前不能确认其仓库分支和打包来源。

### 尚未完成

1. 完整 DEX 反编译到 Java/Kotlin/Smali，以及 Hook 类/方法的逐项调用图。
2. 闭源 Local API payload 的 handler、完整请求/响应 schema、SSE 事件细节。
3. `libshi.so`、`libnesec.so` 的 JNI/ARM64 反汇编与载荷解包。
4. 运行时验证实际监听端口、局域网开关、API Key 验证、请求队列和错误码。
5. 未进行安装、Hook 或在线请求测试。

当前环境没有预装 JADX/apktool，且容器无法访问外网下载反编译工具。因此本报告不是完整 JADX 反编译报告；后续需要在可用的 JADX/MT 管理器环境继续拆解 DEX 和 native payload。

## 9. 面向独立实现的建议

建议复刻架构和公开协议，不直接复制闭源 payload：

1. **Android Host Adapter**：仅负责经授权的 DeepSeek 进程 Hook、版本适配、会话/请求上下文桥接。
2. **Local API Gateway**：独立 HTTP 服务，先实现 OpenAI Chat Completions 与 Anthropic Messages；随后补 Responses、SSE、tool calls、models。
3. **Native Transport Adapter**：把标准 API 请求转换成宿主当前会话/模型请求，再将原生流式文本与 reasoning 转回标准协议。
4. **Agent Runtime**：工具注册表、JSON Schema 校验、权限策略、执行超时、调用去重、持久化 outbox。
5. **Device Backend**：`in_app` 为默认，Root/Shizuku 显式授权；文件工作区隔离，危险操作二次确认。
6. **MCP Client**：独立支持 Streamable HTTP/SSE；不要把 MCP 客户端和本地 HTTP API 混成一个模块。
7. **Audio API**：单独模块/服务，避免复用主 Local API 的端口、Key 或生命周期状态。

如果采用 Deekseep 的 GPL 代码或衍生代码，必须保留作者署名和 GPL-3.0 许可，并遵守相应源码公开义务。独立重写时应避免复制未公开的闭源实现。

## 10. 参考来源

- APK 内 `assets/LETTER_TO_DEVELOPERS.txt`
- APK 内 `assets/xposed_init`
- APK `AndroidManifest.xml`
- APK `classes.dex` / `classes2.dex` 字符串与 DEX class/type 表
- APK ARM64 ELF 库元信息
- 作者公开仓库：<https://github.com/lllucccian/Deekseep>
- 公开版 Hook 入口：<https://github.com/lllucccian/Deekseep/blob/main/module/src/com/dsmod/probe/Main.java>
- 公开版 Local API 合约：<https://github.com/lllucccian/Deekseep/blob/main/module/src/com/dsmod/probe/z2.java>
- 公开版 API 路由/认证标识：<https://github.com/lllucccian/Deekseep/blob/main/module/src/com/dsmod/probe/z3.java>
- 公开版 MCP 客户端：<https://github.com/lllucccian/Deekseep/blob/main/module/src/com/dsmod/probe/AgentMcpManager.java>
- 公开版 Agent 工具协议：<https://github.com/lllucccian/Deekseep/blob/main/module/src/com/dsmod/probe/HeartbeatToolProtocol.java>
