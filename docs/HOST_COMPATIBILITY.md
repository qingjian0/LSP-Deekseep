# Host Compatibility

Target package: `com.deepseek.chat`.

Do not identify Play and domestic builds by versionCode alone. Record at minimum:
- package name;
- versionName and versionCode;
- APK SHA-256;
- installer/source channel when available;
- signing-certificate digest;
- Android version and ABI;
- candidate class/method signatures observed at runtime.

Current candidate symbols are research leads, not a compatibility guarantee. Unknown versions are logged as unclassified. The module does not patch host behavior when a candidate is absent.
