# Architecture

## Runtime sequence

1. LSPosed loads the module entry only for the configured target package.
2. The entry filters strictly on `com.deepseek.chat`.
3. It hooks `Application.attach(Context)` to obtain the host ClassLoader after process initialization.
4. It attempts a small, explicit list of DEX-derived candidate class/method names.
5. For methods that exist, it hooks declared overloads and emits metadata only.

## Non-goals for this baseline

- No global framework-method sweep.
- No request/response body capture.
- No mutation of host arguments, return values, TLS, authentication, or anti-abuse checks.
- No embedded HTTP server or Agent executor yet.
- No assumptions that obfuscated class names have the host package prefix.

## Extension boundary

Future adapters should be added only after runtime signatures are observed on a specific APK hash/version/channel. Keep each host profile explicit and fail closed when signatures do not match.
