package dev.watchtower.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

// Fake key only.
private const val KEY = "wtk_example_not_a_real_key"

private class Fake : NativeBridge {
    var started: WatchtowerConfig? = null
    val errors = mutableListOf<Pair<Throwable, Boolean>>()
    override fun start(config: WatchtowerConfig) { started = config }
    override fun recordError(t: Throwable, handled: Boolean) { errors += t to handled }
    override fun flush() {}
}

class WatchtowerTest {
    @Test fun toStringMasksKey() {
        assertFalse(WatchtowerConfig(KEY, "svc").toString().contains(KEY))
    }

    @Test fun delegatesToBridge() {
        val f = Fake()
        Watchtower.setBridge(f)
        Watchtower.start(WatchtowerConfig(KEY, "svc", release = "1.0"))
        assertEquals("1.0", f.started?.release)
        val boom: Throwable = IllegalStateException("boom")
        Watchtower.recordError(boom, handled = false)
        assertEquals(listOf<Pair<Throwable, Boolean>>(boom to false), f.errors.toList())
    }

    @Test fun rumOptionsReachTheBridgeMap() {
        val o = WatchtowerConfig(KEY, "svc", trackViews = false, hangThresholdMs = 3000, profileLaunch = true, buildId = "b1").rumOptions()
        assertEquals(false, o["trackViews"])
        assertEquals(3000L, o["hangThresholdMs"])
        assertEquals(true, o["profileLaunch"])
        assertEquals("b1", o["buildId"])
    }

    @Test fun blankKeyRejected() {
        Watchtower.setBridge(Fake())
        assertFailsWith<IllegalArgumentException> { Watchtower.start(WatchtowerConfig(" ", "svc")) }
    }
}
