package dev.watchtower.kmp

/**
 * Implemented in Swift (see README) by forwarding to `Watchtower.start(Config(...))` and
 * `Watchtower.shared.recordError` of the watchtower-ios package, so the Kotlin framework needs no cinterop.
 */
interface IosNativeBridge {
    fun start(
        key: String, service: String, endpoint: String?, release: String?, environment: String?,
        replay: Boolean, maskText: Boolean,
    )

    fun recordError(name: String, message: String, stack: String, handled: Boolean)
    fun flush()
}

/** iOS entry point, from `application(_:didFinishLaunchingWithOptions:)`. */
fun Watchtower.startIos(bridge: IosNativeBridge, config: WatchtowerConfig) {
    setBridge(object : NativeBridge {
        override fun start(config: WatchtowerConfig) = bridge.start(
            config.key, config.service, config.endpoint, config.release, config.environment,
            config.replay, config.maskText,
        )

        override fun recordError(t: Throwable, handled: Boolean) =
            bridge.recordError(t::class.simpleName ?: "Error", t.message ?: "", t.stackTraceToString().take(8000), handled)

        override fun flush() = bridge.flush()
    })
    start(config)
}
