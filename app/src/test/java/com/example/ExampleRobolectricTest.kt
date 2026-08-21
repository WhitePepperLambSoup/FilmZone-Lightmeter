package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.meter.calculator.ExposureCalculator
import com.example.meter.model.FilmDatabase
import com.example.meter.model.ZoneLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FilmZone", appName)
    }

    @Test
    fun testSunny16ExposureCalculation() {
        // Sunny 16 rule: ISO 100, f/16, shutter 1/125s -> EV100 should be ~15.0
        val ev100 = ExposureCalculator.calculateEv100(16.0, 1.0 / 125.0, 100)
        assertTrue(abs(ev100 - 15.0) < 0.2)
    }

    @Test
    fun testZonePlacementCalculation() {
        // Measured EV = 12.0
        // Place on Zone VI (+1 EV tone) -> Effective scene EV target = 12.0 - (+1.0) = 11.0
        val targetEv = ExposureCalculator.calculateZoneTargetEv(12.0, ZoneLevel.ZONE_VI)
        assertEquals(11.0, targetEv, 0.001)

        // Place on Zone III (-2 EV shadow tone) -> Effective scene EV target = 12.0 - (-2.0) = 14.0
        val shadowEv = ExposureCalculator.calculateZoneTargetEv(12.0, ZoneLevel.ZONE_III)
        assertEquals(14.0, shadowEv, 0.001)
    }

    @Test
    fun testReciprocityFailureCompensation() {
        val triX = FilmDatabase.allFilms.first { it.id == "kodak_trix_400" }
        val (compTriXSec, _) = ExposureCalculator.calculateReciprocity(triX, 4.0)
        assertTrue("Compensated 4s for Tri-X should be longer than 4s", compTriXSec > 4.0)

        val acros = FilmDatabase.allFilms.first { it.id == "fuji_acros_ii" }
        val (compAcrosSec, _) = ExposureCalculator.calculateReciprocity(acros, 10.0)
        assertEquals(10.0, compAcrosSec, 0.001) // Acros II requires no reciprocity compensation under 120s!
    }
}
