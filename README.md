# DeepSeek Runtime Probe (DSRP)

独立 Android LSPosed/Xposed 运行时探针，目标宿主：DeepSeek Android（包名 `com.deepseek.chat`）。

> 当前版本是 **0.1.0-dev / instrumentation baseline**。它用于验证有限、明确的候选方法边界并输出脱敏元数据；不声称已经完整恢复 DeepSeek 的 SSE、Flow 或消息模型，也不包含可绕过认证、PoW、TLS 或服务端访问控制的逻辑。

## 项目身份

- 项目名：DeepSeek Runtime Probe
- applicationId：`dev.deepseek.runtimeprobe`
- Xposed 入口：`dev.deepseek.runtimeprobe.ProbeEntry`
- 目标包：`com.deepseek.chat`
- 最低 Android：API 26
- LSPosed/Xposed API：82（compileOnly）
- 语言：Java；构建：Gradle / Android Gradle Plugin

## 构建

GitHub Actions 在推送到 `main`、推送 `v*` 标签或手动触发时构建 Debug APK。成功后在 Actions 对应运行记录的 Artifacts 中下载 `DSRP-debug-apk`。

本地环境要求：JDK 17、Android SDK Platform 35、Gradle 8.9。

```bash
gradle --no-daemon :app:assembleDebug
```

APK 路径：`app/build/outputs/apk/debug/app-debug.apk`。

## 安装与启用

1. 从 GitHub Actions 下载 `DSRP-debug-apk` 并安装 APK。
2. 打开 LSPosed Manager，在 Modules 中启用 **DeepSeek Runtime Probe**。
3. Scope 只勾选 DeepSeek（`com.deepseek.chat`）。
4. 强制停止并重新启动 DeepSeek。
5. 在 LSPosed 日志中筛选 `[DSRP]`，查看宿主版本、候选类是否存在及候选方法元数据。

模块不提供普通启动器 Activity；这是一个由 LSPosed 加载的模块。

## 当前 Hook 范围

仅对以下静态逆向候选进行**存在性探测和方法元数据 Hook**，类名按 DEX descriptor 的原始简单名称尝试，不擅自补上 `com.deepseek.chat.` 前缀：

- `rx3#g`
- `h60#b`
- `nw1#j`
- `nw1#e`
- `nw1#a`
- `st1#A`

这些符号是待真机验证的候选，不代表已经确认它们就是主聊天 SSE parser 或稳定 API。类/方法不存在时会记录并跳过。不同渠道、版本和混淆映射可能不同。

## 隐私与安全

默认只记录：目标包/版本、类名、方法签名、参数类型、返回类型、结果对象类型、异常类型、耗时和序号。**不记录**聊天正文、prompt、文件内容、Cookie、Authorization、API Key、PoW answer 或其他参数值。不改写参数、返回值或网络流量。

## 文档

- [安装与 LSPosed 启用](docs/INSTALLATION.md)
- [架构](docs/ARCHITECTURE.md)
- [Hook 覆盖与候选清单](docs/HOOK_COVERAGE.md)
- [静态逆向阶段报告](docs/STATIC_REVERSE_ENGINEERING_REPORT.md)
- [宿主兼容性](docs/HOST_COMPATIBILITY.md)
- [构建、安装与验证](docs/BUILD_AND_TEST.md)
- [安全与隐私](docs/SECURITY_AND_PRIVACY.md)
- [对话项目记录](docs/SESSION_RECORD.md)

## 状态声明

当前仓库提交只代表源码和 CI 构建配置已提交。只有 GitHub Actions 绿色且 APK artifact 实际生成后，才能称“构建成功”；只有目标手机启用模块并观察到运行日志后，才能称“真机 Hook 成功”。这两个验证不会互相替代。
