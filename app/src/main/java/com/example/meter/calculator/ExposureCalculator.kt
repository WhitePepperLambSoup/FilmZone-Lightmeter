package com.example.meter.calculator

import com.example.meter.model.FilmStock
import com.example.meter.model.PriorityMode
import com.example.meter.model.ZoneLevel
import java.util.Locale
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object ExposureCalculator {

    // Standard Aperture values in 1/3 EV stops
    val APERTURES = doubleArrayOf(
        0.7, 0.8, 0.95, 1.0, 1.1, 1.2, 1.4, 1.6, 1.8, 2.0, 2.2, 2.5, 2.8, 3.2, 3.5, 4.0,
        4.5, 5.0, 5.6, 6.3, 7.1, 8.0, 9.0, 10.0, 11.0, 13.0, 14.0, 16.0, 18.0, 20.0, 22.0,
        25.0, 29.0, 32.0, 36.0, 40.0, 45.0, 51.0, 57.0, 64.0
    )

    // Standard Shutter Speeds in seconds (from 1/8000s to 3600s / 1hr)
    val SHUTTER_SPEEDS = doubleArrayOf(
        1.0 / 8000, 1.0 / 6400, 1.0 / 5000, 1.0 / 4000, 1.0 / 3200, 1.0 / 2500, 1.0 / 2000,
        1.0 / 1600, 1.0 / 1250, 1.0 / 1000, 1.0 / 800, 1.0 / 640, 1.0 / 500, 1.0 / 400,
        1.0 / 320, 1.0 / 250, 1.0 / 200, 1.0 / 160, 1.0 / 125, 1.0 / 100, 1.0 / 80,
        1.0 / 60, 1.0 / 50, 1.0 / 40, 1.0 / 30, 1.0 / 25, 1.0 / 20, 1.0 / 15,
        1.0 / 13, 1.0 / 10, 1.0 / 8, 1.0 / 6, 1.0 / 5, 1.0 / 4, 0.3, 0.4, 0.5,
        0.6, 0.8, 1.0, 1.3, 1.6, 2.0, 2.5, 3.2, 4.0, 5.0, 6.0, 8.0, 10.0,
        13.0, 15.0, 20.0, 25.0, 30.0, 45.0, 60.0, 90.0, 120.0, 180.0, 240.0,
        300.0, 600.0, 900.0, 1200.0, 1800.0, 3600.0
    )

    // Standard ISO values
    val ISO_VALUES = intArrayOf(
        6, 12, 25, 32, 50, 64, 80, 100, 125, 160, 200, 250, 320, 400,
        500, 640, 800, 1000, 1250, 1600, 2000, 2500, 3200, 6400, 12800, 25600
    )

    /**
     * Compute EV100 from Aperture, Shutter Speed, and ISO
     * EV100 = log2(N^2 / t) - log2(S / 100)
     */
    fun calculateEv100(aperture: Double, shutterSec: Double, iso: Int): Double {
        if (aperture <= 0 || shutterSec <= 0 || iso <= 0) return 0.0
        val evCurrent = log2((aperture * aperture) / shutterSec)
        val isoOffset = log2(iso.toDouble() / 100.0)
        return evCurrent - isoOffset
    }

    /**
     * Compute EV at current ISO
     * EV_S = EV100 + log2(S / 100)
     */
    fun calculateEvForIso(ev100: Double, iso: Int): Double {
        if (iso <= 0) return ev100
        return ev100 + log2(iso.toDouble() / 100.0)
    }

    /**
     * Convert EV100 to Approximate Lux (assuming incident light constant K ~ 2.5)
     */
    fun ev100ToLux(ev100: Double): Double {
        return 2.5 * 2.0.pow(ev100)
    }

    /**
     * Convert Lux to Foot-Candles
     */
    fun luxToFootCandles(lux: Double): Double {
        return lux * 0.092903
    }

    /**
     * Calculate target effective EV for camera based on Ansel Adams Zone placement
     * If user places a measured spot into Zone Z:
     *   deltaEv = TargetZone - 5
     *   effectiveCameraEv = measuredEv + deltaEv
     */
    fun calculateZoneTargetEv(measuredEv: Double, targetZone: ZoneLevel): Double {
        val deltaEv = -targetZone.relativeEv
        return measuredEv - deltaEv
    }

    /**
     * Given effective EV and Aperture, find the recommended shutter speed
     * t = N^2 / (2^EV)
     */
    fun calculateShutterForAperture(effectiveEv: Double, aperture: Double): Double {
        val rawTime = (aperture * aperture) / 2.0.pow(effectiveEv)
        return findClosestShutter(rawTime)
    }

    /**
     * Given effective EV and Shutter Speed, find recommended aperture
     * N = sqrt(t * 2^EV)
     */
    fun calculateApertureForShutter(effectiveEv: Double, shutterSec: Double): Double {
        val rawAperture = sqrt(shutterSec * 2.0.pow(effectiveEv))
        return findClosestAperture(rawAperture)
    }

    /**
     * Find the closest standard aperture value
     */
    fun findClosestAperture(rawAperture: Double): Double {
        return APERTURES.minByOrNull { abs(it - rawAperture) } ?: 2.8
    }

    /**
     * Find the closest standard shutter speed
     */
    fun findClosestShutter(rawSec: Double): Double {
        return SHUTTER_SPEEDS.minByOrNull { abs(it - rawSec) } ?: (1.0 / 125)
    }

    /**
     * Find closest standard ISO
     */
    fun findClosestIso(rawIso: Int): Int {
        return ISO_VALUES.minByOrNull { abs(it - rawIso) } ?: 400
    }

    /**
     * Calculate matching pairs of (Aperture, Shutter) for a given EV
     */
    fun getExposurePairs(effectiveEv: Double): List<Pair<Double, Double>> {
        val list = mutableListOf<Pair<Double, Double>>()
        for (aperture in APERTURES) {
            val rawTime = (aperture * aperture) / 2.0.pow(effectiveEv)
            if (rawTime in (1.0 / 8000)..3600.0) {
                val closest = findClosestShutter(rawTime)
                list.add(Pair(aperture, closest))
            }
        }
        return list
    }

    /**
     * Format Aperture to string: f/2.8, f/1.4, etc.
     */
    fun formatAperture(aperture: Double): String {
        return if (aperture == aperture.toInt().toDouble() || aperture >= 10.0 && (aperture * 10) % 10 == 0.0) {
            String.format(Locale.US, "f/%.0f", aperture)
        } else {
            String.format(Locale.US, "f/%.1f", aperture)
        }
    }

    /**
     * Format Shutter Speed to human readable photographic notation:
     * 1/8000s, 1/250s, 0.5s, 4s, 2m 30s, etc.
     */
    fun formatShutter(seconds: Double): String {
        return when {
            seconds <= 0.0 -> "0s"
            seconds < 0.3 -> {
                val denominator = (1.0 / seconds).roundToInt()
                "1/${denominator}s"
            }
            seconds < 1.0 -> {
                String.format(Locale.US, "%.1fs", seconds)
            }
            seconds < 60.0 -> {
                if (seconds == seconds.toInt().toDouble()) {
                    String.format(Locale.US, "%.0fs", seconds)
                } else {
                    String.format(Locale.US, "%.1fs", seconds)
                }
            }
            else -> {
                val totalSec = seconds.roundToInt()
                val minutes = totalSec / 60
                val remSec = totalSec % 60
                if (remSec == 0) {
                    "${minutes}m"
                } else {
                    "${minutes}m ${remSec}s"
                }
            }
        }
    }

    /**
     * Calculate reciprocity-compensated time and difference in stops
     */
    fun calculateReciprocity(film: FilmStock, meteredSec: Double): Pair<Double, Double> {
        val compensatedSec = film.calculateReciprocity(meteredSec)
        val stopsAdded = if (meteredSec > 0 && compensatedSec >= meteredSec) {
            log2(compensatedSec / meteredSec)
        } else 0.0
        return Pair(compensatedSec, stopsAdded)
    }

    /**
     * Calculate contrast range in stops between two EV readings
     */
    fun calculateContrastStops(evA: Double, evB: Double): Double {
        return abs(evA - evB)
    }

    /**
     * Calculate Depth of Field (Near limit, Far limit, Hyperfocal distance)
     */
    data class DepthOfFieldResult(
        val hyperfocalMeters: Float,
        val nearLimitMeters: Float,
        val farLimitMeters: Float?, // null means Infinity (∞)
        val totalDepthMeters: Float? // null means Infinity
    )

    fun calculateDepthOfField(
        focalLengthMm: Float,
        apertureN: Float,
        subjectDistanceMeters: Float,
        circleOfConfusionMm: Float = 0.030f // Standard for 35mm film format
    ): DepthOfFieldResult {
        val fMm = focalLengthMm.coerceAtLeast(10f)
        val fM = fMm / 1000f
        val n = apertureN.coerceAtLeast(0.7f)
        val cocM = (circleOfConfusionMm.coerceAtLeast(0.010f)) / 1000f

        // Hyperfocal Distance H = (f^2) / (N * c) + f (in meters)
        val hyperfocalM = ((fM * fM) / (n * cocM)) + fM

        val d = subjectDistanceMeters.coerceAtLeast(0.1f)
        val dMinusF = (d - fM).coerceAtLeast(0.01f)

        // Near Limit = (H * d) / (H + (d - f))
        val nearM = (hyperfocalM * d) / (hyperfocalM + dMinusF)

        // Far Limit = (H * d) / (H - (d - f)) if d < H, else Infinity
        val denom = hyperfocalM - dMinusF
        val farM = if (denom > 0.05f) {
            (hyperfocalM * d) / denom
        } else {
            null // Infinity
        }

        val totalD = if (farM != null) (farM - nearM).coerceAtLeast(0f) else null

        return DepthOfFieldResult(
            hyperfocalMeters = hyperfocalM,
            nearLimitMeters = nearM,
            farLimitMeters = farM,
            totalDepthMeters = totalD
        )
    }
}
