package dev.watchtower.kmp

import android.app.Application
import dev.watchtower.Config
import dev.watchtower.ReplayConfig

private class AndroidBridge(private val app: Application) : NativeBridge {
    override fun start(config: WatchtowerConfig) {
        var c = Config(
            key = config.key,
            service = config.service,
            release = config.release,
            environment = config.environment,
            replay = ReplayConfig(enabled = config.replay, maskText = config.maskText),
        )
        config.endpoint?.let { c = c.copy(endpoint = it) }
        dev.watchtower.Watchtower.init(app, c)
    }

    override fun recordError(t: Throwable, handled: Boolean) =
        dev.watchtower.Watchtower.recordError(t, handled)

    override fun flush() = dev.watchtower.Watchtower.flush()
}

/** Android entry point, from `Application.onCreate`. */
fun Watchtower.startAndroid(app: Application, config: WatchtowerConfig) {
    setBridge(AndroidBridge(app))
    start(config)
}
