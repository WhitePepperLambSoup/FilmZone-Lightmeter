package com.example

import com.example.meter.calculator.ExposureCalculator
import com.example.meter.model.FilmDatabase
import com.example.meter.model.ZoneLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {
    @Test
    fun testApexCalculations() {
        // Standard Sunny 16 rule: EV100 = 15 at ISO 100
        val ev = ExposureCalculator.calculateEv100(16.0, 1.0 / 125.0, 100)
        assertTrue(abs(ev - 15.0) < 0.1)

        // Shutter calculation test at ISO 100, f/16, EV100 = 15 -> Shutter ≈ 1/125s
        val shutter = ExposureCalculator.calculateShutterForAperture(15.0, 16.0)
        assertTrue(abs(shutter - 1.0 / 125.0) < 0.002)

        // Aperture calculation test at ISO 100, 1/125s, EV100 = 15 -> Aperture ≈ 16.0
        val aperture = ExposureCalculator.calculateApertureForShutter(15.0, 1.0 / 125.0)
        assertTrue(abs(aperture - 16.0) < 0.5)
    }

    @Test
    fun testIsoScaling() {
        val ev100 = 12.0
        // At ISO 400 (+2 EV higher sensitivity), effective EV should be 14.0
        val ev400 = ExposureCalculator.calculateEvForIso(ev100, 400)
        assertEquals(14.0, ev400, 0.001)

        // At ISO 50 (-1 EV lower sensitivity), effective EV should be 11.0
        val ev50 = ExposureCalculator.calculateEvForIso(ev100, 50)
        assertEquals(11.0, ev50, 0.001)
    }

    @Test
    fun testReciprocityFailure() {
        val triX = FilmDatabase.allFilms.first { it.id == "kodak_trix_400" }
        // For sub-second exposure, no reciprocity failure
        val (shortCompSec, shortStops) = ExposureCalculator.calculateReciprocity(triX, 0.5)
        assertEquals(0.5, shortCompSec, 0.001)
        assertEquals(0.0, shortStops, 0.001)

        // For long exposure (10s), reciprocity compensation kicks in
        val (longCompSec, longStops) = ExposureCalculator.calculateReciprocity(triX, 10.0)
        assertTrue(longCompSec > 10.0)
        assertTrue(longStops > 0.0)
    }

    @Test
    fun testZoneSystemOffsets() {
        assertEquals(0.0, ZoneLevel.ZONE_V.relativeEv, 0.001)
        assertEquals(-2.0, ZoneLevel.ZONE_III.relativeEv, 0.001)
        assertEquals(+2.0, ZoneLevel.ZONE_VII.relativeEv, 0.001)
        assertEquals(-5.0, ZoneLevel.ZONE_0.relativeEv, 0.001)
        assertEquals(+5.0, ZoneLevel.ZONE_X.relativeEv, 0.001)
    }
}
