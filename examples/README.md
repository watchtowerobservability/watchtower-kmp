# Minimal app

Shared code (`commonMain`):

```kotlin
fun config() = WatchtowerConfig(key = "<publishable key>", service = "shop", release = "1.0.0")
fun report(t: Throwable) = Watchtower.recordError(t)
```

Android, `Application.onCreate`: `Watchtower.startAndroid(this, config())`.

iOS, `AppDelegate`: implement `IosNativeBridge` (see the package README) and call
`Watchtower.shared.startIos(bridge: WatchtowerSwiftBridge(), config: ...)`.
