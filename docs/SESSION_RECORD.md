# Project Session Record

Date basis: 2026-10-31. This file is a structured project record of the decisions and findings available in the working conversation context; it is **not a verbatim export of every chat message**.

## User requirements

- Build an installable Android APK that can be enabled in LSPosed.
- Keep this project independent; do not name or package it as DSH.
- Project name: DeepSeek Runtime Probe (DSRP).
- Package/applicationId: `dev.deepseek.runtimeprobe`.
- Target DeepSeek Android package: `com.deepseek.chat`.
- Use Hook/instrumentation to study runtime request, Flow, SSE, message and file-model boundaries.
- User requested broad Hook coverage; implementation constrains this to an explicit candidate list rather than globally hooking Android/Java framework APIs.
- Repository destination: `https://github.com/qingjian0/LSP-Deekseep-`.

## Reverse-engineering leads

Candidates recorded in the research notes:
- `rx3.g(Lvv1, Long): Lh60` — request/Flow boundary candidate.
- `h60.b(FlowCollector, Continuation)` — Flow collect-shaped candidate.
- `nw1.j(...)` — Flow consumer candidate.
- `nw1.e(int, Lbv3, Continuation)` — event/state dispatch candidate.
- `nw1.a(Loga, Continuation)` — event branch candidate.
- `st1.A(Object)` — ByteReadChannel/parser candidate; not established as the main chat SSE parser and may relate to audio/TTS.

Unresolved:
- actual Flow emitted object type;
- main chat SSE parser and complete finish/close/error/cancel lifecycle;
- nested FileFragment metadata;
- runtime signatures on real devices;
- channel-specific mappings and version compatibility.

## Uploaded research materials

The conversation included:
- `Deekseep.apk`;
- `DeepSeek_2.7.1.apks`;
- `模块.apk.zip`;
- `Deekseep_Nexus_APK_Reverse_Engineering_Report_2026-10-31.md`.

The static report is reproduced in `STATIC_REVERSE_ENGINEERING_REPORT.md`. APK/APKS binaries are not copied into this public repository because they are third-party application packages and may contain proprietary code or assets.

## Credential safety

A GitHub personal access token was pasted into chat during the earlier workflow. It must be revoked immediately. It is deliberately omitted from this repository and must never be used in source, workflow files, logs, or documentation.

## Delivery status

This repository commit contains the initial source baseline and CI build definition. It does not, by itself, prove a successful CI build, APK installation, LSPosed activation, or runtime Hook execution. Those states must be recorded only after the corresponding evidence exists.
