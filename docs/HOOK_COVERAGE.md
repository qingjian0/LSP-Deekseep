# Hook Coverage and Evidence

| Candidate | Static lead | Current runtime behavior | Confidence |
|---|---|---|---|
| `rx3#g` | Request/Flow boundary candidate | Probe class and overload presence; metadata hook if found | Unverified |
| `h60#b` | Flow collect-shaped candidate | Probe class and overload presence; metadata hook if found | Unverified |
| `nw1#j` | Flow consumer candidate | Probe class and overload presence; metadata hook if found | Unverified |
| `nw1#e` | Event/state dispatch candidate | Probe class and overload presence; metadata hook if found | Unverified |
| `nw1#a` | Event branch candidate | Probe class and overload presence; metadata hook if found | Unverified |
| `st1#A` | ByteReadChannel/parser candidate; may relate to audio/TTS | Probe class and overload presence; metadata hook if found | Not established as main chat SSE parser |

The names above are raw simple class names inferred from DEX descriptors. A descriptor such as `Lrx3;` does not justify inventing `com.deepseek.chat.rx3`.

The probe records parameter types and return types, not parameter values. A method being found does not prove its semantic role. A method being hooked does not prove it was invoked during a chat request.
