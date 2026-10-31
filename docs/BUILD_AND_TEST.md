# Build, Install, and Test

## CI build

Workflow: `.github/workflows/android.yml`.

Expected artifact after a green run: `DSRP-debug-apk`, containing `app-debug.apk`.

## Install

1. Download the artifact ZIP from the successful Actions run.
2. Extract and install `app-debug.apk`.
3. Enable the module in LSPosed.
4. Select only `com.deepseek.chat` in Scope.
5. Force-stop and relaunch DeepSeek.

## Verification checklist

- [ ] GitHub Actions build is green.
- [ ] APK signature verifies with `apksigner`.
- [ ] LSPosed lists the module and allows enabling it.
- [ ] DeepSeek process starts without crash/ANR.
- [ ] `Application.attach(Context)` hook appears in LSPosed logs.
- [ ] Candidate class presence is recorded.
- [ ] Candidate method signatures are recorded.
- [ ] At least one candidate method is observed executing during a controlled chat.
- [ ] Repeat on each supported distribution channel and APK hash.

A CI build does not establish device installation or runtime hook success.
