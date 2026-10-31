# 安装与 LSPosed 启用

## 当前构建类型

当前基线使用传统 Xposed API 82 入口（`IXposedHookLoadPackage`），不是 libxposed API 102 模块。它适用于支持传统 Xposed 模块加载的 LSPosed/Xposed 环境。不同 LSPosed 分支/版本对传统入口的兼容性可能不同；如果管理器不显示模块或日志没有加载记录，需要针对该框架切换到现代 libxposed API 版本，不能把“APK 安装成功”当作“模块已加载”。

## 下载 APK

1. 打开仓库 Actions 页面：<https://github.com/qingjian0/LSP-Deekseep-/actions>
2. 进入最新的 **Android Build** 工作流运行记录。
3. 只有状态为绿色成功时，才从 Artifacts 下载 `DSRP-debug-apk`。
4. 解压后安装 `app-debug.apk`。

如果 Actions 仍在运行或失败，暂时没有可安装的已验证 APK。

## 在 LSPosed 中启用

1. 安装 APK。
2. 打开 LSPosed Manager → Modules。
3. 找到 **DeepSeek Runtime Probe** 并启用。
4. 打开 Scope，只勾选 `com.deepseek.chat`。
5. 强制停止 DeepSeek，再重新启动。
6. 查看 LSPosed 日志，搜索 `[DSRP]`。

## 预期日志

- `load_package`：目标包进程已进入模块入口。
- `attach_hook`：Application.attach hook 安装完成。
- `host_profile`：记录 ClassLoader 元数据。
- `candidate`：候选类存在/不存在及匹配方法数量。
- `method_enter` / `method_exit`：候选方法被调用时的类型元数据和耗时。

如果只有 `load_package` 而没有 `attach_hook`，说明入口已加载但 attach hook 失败；如果候选类全部 absent，说明当前 DeepSeek 构建的混淆映射与候选不匹配。

## 安全回退

若 DeepSeek 出现崩溃、卡死或异常耗电：在 LSPosed 中关闭模块，强制停止 DeepSeek 后再启动。不要在多个不确定版本上同时启用候选 Hook。
