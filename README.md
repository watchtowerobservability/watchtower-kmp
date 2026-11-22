# watchtower-kmp

Kotlin Multiplatform common API for Watchtower. Shared code calls one API; Android delegates to
`watchtower-android`, iOS to `watchtower-ios` through a small Swift bridge. No product logic lives here.

## Install

```kotlin
// build.gradle.kts, commonMain (the Android SDK comes in transitively on androidMain)
implementation("io.github.watchtowerobservability:watchtower-kmp:0.1.1")
```

Artifacts are published to Maven Central (`io.github.watchtowerobservability:watchtower-kmp`) and GitHub Packages.
Add the `watchtower-ios` Swift package to the iOS app.

## Quick start

```kotlin
val cfg = WatchtowerConfig(key = "<publishable key>", service = "shop", release = "1.0.0")
Watchtower.startAndroid(app, cfg)          // androidMain, Application.onCreate
Watchtower.startIos(SwiftBridge(), cfg)    // iosMain / Swift, app launch
Watchtower.recordError(t)                  // anywhere in shared code
```

iOS bridge (Swift, in the app target):

```swift
import Watchtower
import shared // your Kotlin framework

final class SwiftBridge: IosNativeBridge {
    func start(key: String, service: String, endpoint: String?, release: String?, environment: String?, replay: Bool, maskText: Bool, options: [String: Any]) {
        var c = Config(key: key, service: service)
        if let endpoint { c.endpoint = endpoint }
        c.release = release; c.environment = environment
        c.replay.enabled = replay; c.replay.maskText = maskText
        if let v = options["trackViews"] as? Bool { c.trackViews = v }
        if let v = options["hangThresholdMs"] as? Int, v > 0 { c.hangThreshold = Double(v) / 1000 }
        if let v = options["propagateTo"] as? [String] { c.propagateTo = v }
        if let v = options["profileLaunch"] as? Bool { c.profileLaunch = v }
        c.buildId = options["buildId"] as? String
        Watchtower.start(c)
    }
    func recordError(name: String, message: String, stack: String, handled: Bool) {
        Watchtower.shared.recordError(NSError(domain: name, code: 0, userInfo: [NSLocalizedDescriptionKey: message, "stack": stack]), handled: handled)
    }
    func flush() { Watchtower.shared.flush() }
}
```

## Integrations

| Integration | Where |
| --- | --- |
| Crash, ANR, app start, frames, replay (Android) | `watchtower-android`, started by `startAndroid` |
| Crash, hang, app start, frames, replay (iOS) | `watchtower-ios`, started through the Swift bridge |
| Shared error capture | `Watchtower.recordError` |

## Configuration

| Option | Meaning |
| --- | --- |
| `key` | publishable ingest key; masked in `toString`, never logged |
| `service`, `release`, `environment` | passed to the native SDK |
| `endpoint` | edge URL; unset uses the native default |
| `replay`, `maskText` | masked screenshot replay |
| `trackViews`, `hangThresholdMs`, `propagateTo`, `profileLaunch`, `buildId` | screen views, ANR / hang threshold, trace propagation prefixes, launch profiling, debug-file build id |

## Provenance

Every release artifact carries a GitHub build attestation. Verify one with
`gh attestation verify <file> --repo watchtowerobservability/watchtower-kmp`.

## License

Apache-2.0. Source of truth is the Watchtower monorepo; this repository is a published mirror.
