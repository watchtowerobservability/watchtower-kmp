package dev.watchtower.kmp

/** Options common to both platforms. Unset fields use the native SDK's defaults. */
data class WatchtowerConfig(
    /** Publishable ingest key (sent as a bearer token by the native SDK). */
    val key: String,
    val service: String,
    val endpoint: String? = null,
    val release: String? = null,
    val environment: String? = null,
    val replay: Boolean = false,
    /** Mask every text view and input in replay (default). */
    val maskText: Boolean = true,
    /** One RUM `view` per resumed screen (activity / view controller). */
    val trackViews: Boolean = true,
    /** The main thread counts as hung (ANR / app hang) after this many ms. */
    val hangThresholdMs: Long = 5000,
    /** URL prefixes that get a W3C `traceparent` header. */
    val propagateTo: List<String> = emptyList(),
    /** Sample the main thread until the first frame of a cold launch (`app_launch_profile`). */
    val profileLaunch: Boolean = false,
    /** The debug-file build id (dSYM UUID, ProGuard mapping id) added to every error. */
    val buildId: String? = null,
) {
    // The key must never reach logs.
    override fun toString() = "WatchtowerConfig(service=$service, endpoint=$endpoint, key=***)"

    /** The RUM options as the map the iOS bridge reads (keys as in the native `Config`). */
    fun rumOptions(): Map<String, Any?> = mapOf(
        "trackViews" to trackViews, "hangThresholdMs" to hangThresholdMs, "propagateTo" to propagateTo,
        "profileLaunch" to profileLaunch, "buildId" to buildId,
    )
}

/** What a platform must provide; implemented for Android here and for iOS by a Swift bridge. */
interface NativeBridge {
    fun start(config: WatchtowerConfig)
    fun recordError(t: Throwable, handled: Boolean)
    fun flush()
}

/** `Watchtower.start(config)` once at launch, then `recordError` anywhere in shared code. */
object Watchtower {
    internal var bridge: NativeBridge? = null

    /** Android: call [startAndroid][dev.watchtower.kmp.startAndroid] instead; iOS: after [setBridge]. */
    fun start(config: WatchtowerConfig) {
        require(config.key.isNotBlank()) { "Watchtower key is required" }
        (bridge ?: error("Watchtower: no native bridge set (see README)")).start(config)
    }

    fun setBridge(b: NativeBridge) { bridge = b }

    fun recordError(t: Throwable, handled: Boolean = true) { bridge?.recordError(t, handled) }

    fun flush() { bridge?.flush() }
}
