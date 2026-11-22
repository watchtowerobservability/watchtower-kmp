## 0.1.1

- Sync with the monorepo: native RUM options.

- `WatchtowerConfig` carries the native RUM options (`trackViews`, `hangThresholdMs`, `propagateTo`, `profileLaunch`, `buildId`). The iOS bridge method `start` gains an `options` map (see the README Swift bridge).

## 0.1.0

- Common `Watchtower` API with Android delegation and an iOS Swift bridge.
