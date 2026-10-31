# Security and Privacy

The probe is intentionally metadata-only.

It does not persist or log:
- chat text, prompts, completions, reasoning text;
- request or response bodies;
- cookies, bearer tokens, API keys, PoW answers;
- file contents, images, or user identifiers.

It logs method signatures, argument runtime types, result runtime type, exception type, and elapsed time. No host method arguments or return values are changed.

Only enable the module for the intended DeepSeek package. Do not enable it globally. Disable the module if the host crashes, loops, or becomes unstable.

The repository must not contain user credentials, private tokens, session cookies, or proprietary APK binaries. Uploaded APKs are research inputs and are not redistributed by this project.
