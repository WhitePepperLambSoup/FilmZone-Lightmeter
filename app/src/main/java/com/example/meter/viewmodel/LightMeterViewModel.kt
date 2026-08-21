package com.example.meter.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meter.calculator.ExposureCalculator
import com.example.meter.camera.LuminanceAnalysisResult
import com.example.meter.model.AppLanguage
import com.example.meter.model.COMMON_FOCAL_PRESETS
import com.example.meter.model.CameraLensType
import com.example.meter.model.CameraOpticsInfo
import com.example.meter.model.ColorFilterMode
import com.example.meter.model.CompositionGridStyle
import com.example.meter.model.FilmAspectRatio
import com.example.meter.model.FilmDatabase
import com.example.meter.model.FilmStock
import com.example.meter.model.FocalLengthChoice
import com.example.meter.model.MeteringLog
import com.example.meter.model.MeteringMode
import com.example.meter.model.PhysicalLensInfo
import com.example.meter.model.PriorityMode
import com.example.meter.model.RangefinderEngineMode
import com.example.meter.model.SpotMarker
import com.example.meter.model.ZoneLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

data class LightMeterUiState(
    // Live metering readings
    val liveSpotEv100: Double = 10.0,
    val liveFrameEv100: Double = 10.0,
    val isSpotMeteringActive: Boolean = false, // false = Frameline Center-Weighted Average, true = Spot Metering
    val isZoneSystemEnabled: Boolean = false, // false = Standard Photography Metering, true = Ansel Adams Zone System
    val lockedEv100: Double? = null,
    val isHoldingReading: Boolean = false,
    val spotLuminance: Float = 118f,
    val frameLuminance: Float = 118f,
    val zoneHistogram: IntArray = IntArray(11),
    val sceneDynamicRangeStops: Double = 5.0,

    // Exposure parameters
    val currentFilm: FilmStock = FilmDatabase.allFilms[0], // Default Generic Color Film
    val iso: Int = 400,
    val selectedAperture: Double = 2.8,
    val selectedShutterSec: Double = 1.0 / 125,
    val priorityMode: PriorityMode = PriorityMode.APERTURE,
    val exposureCompensationEv: Double = 0.0,
    val ndFilterStops: Double = 0.0,
    val calibrationOffsetEv: Double = 0.0,

    // Zone System & Metering Point
    val selectedTargetZone: ZoneLevel = ZoneLevel.ZONE_V, // Zone V (18% Middle Gray) default
    val spotNormX: Float = 0.5f,
    val spotNormY: Float = 0.5f,
    val multiSpots: List<SpotMarker> = emptyList(),
    val meteringMode: MeteringMode = MeteringMode.MATRIX,
    val isFalseColorEnabled: Boolean = false,

    // Rangefinder / Focus Distance Measurement
    val isDistanceModeEnabled: Boolean = false,
    val isDistanceSpotLinkEnabled: Boolean = false, // When true, distance tap also performs spot metering
    val rangefinderEngineMode: RangefinderEngineMode = RangefinderEngineMode.INCLINOMETER,
    val userHeightMeters: Float = 1.50f, // Eye-level height from ground for inclinometer trigonometry
    val afDistanceCalibrationScale: Float = 1.0f, // Scale multiplier for Camera2 AF motor diopters
    val measuredDistanceMeters: Float? = null, // Measured distance in meters (from AF or Stadiametric)
    val isDistanceInfinity: Boolean = false,
    val isDistanceLowContrast: Boolean = false,
    val isDistanceMeasuring: Boolean = false,
    val distanceFocusPointNormX: Float = 0.5f,
    val distanceFocusPointNormY: Float = 0.5f,
    val inclinometerPitchDeg: Float = 0f,
    val inclinometerRollDeg: Float = 0f,
    val inclinometerHorizontalDistMeters: Float? = null,
    val inclinometerDirectDistMeters: Float? = null,
    val isPhoneLevel: Boolean = false,

    // Framing, Aspect Ratio & Composition
    val selectedAspectRatio: FilmAspectRatio = FilmAspectRatio.FORMAT_135,
    val focalLengthMm: Int = 50, // User-customizable focal length in mm (default 50mm)
    val selectedFocalLength: FocalLengthChoice = FocalLengthChoice.F50,
    val isDigitalCropZoomEnabled: Boolean = true, // Digital crop zoom to fill viewfinder vs rangefinder brightlines
    val gridStyle: CompositionGridStyle = CompositionGridStyle.RULE_OF_THIRDS,
    val selectedColorFilter: ColorFilterMode = ColorFilterMode.NONE,
    val isTorchOn: Boolean = false,
    val isExperimentalMultiCameraEnabled: Boolean = false,
    val selectedCameraLens: CameraLensType = CameraLensType.MAIN_WIDE,
    val selectedCameraId: String? = null,
    val opticsInfo: CameraOpticsInfo = com.example.meter.camera.CameraOpticsDetector.createFallbackOptics(),
    val manualLandscapeBaseFocalMm: Float? = null,
    val appLanguage: AppLanguage = AppLanguage.DEFAULT,
    val framelineNormLeft: Float = 0.05f,
    val framelineNormTop: Float = 0.05f,
    val framelineNormRight: Float = 0.95f,
    val framelineNormBottom: Float = 0.95f,
    val hardwareZoomRatio: Float = 1.0f,
    val hardwareMinZoomRatio: Float = 0.5f,
    val hardwareMaxZoomRatio: Float = 10.0f,

    // Reciprocity & Long Exposure Timer
    val isTimerRunning: Boolean = false,
    val timerTotalSec: Double = 0.0,
    val timerRemainingSec: Double = 0.0,
    val isTimerCompleted: Boolean = false,

    // Shot Logging
    val shotLogs: List<MeteringLog> = emptyList(),
    val activeTabSheet: ActiveSheet? = null,
    val lastRecordedMessage: String? = null,
    val showFirstLaunchCalibrationPrompt: Boolean = false
) {
    // Target 35mm horizontal equivalent focal length for chosen film format
    val targetEquiv135Mm: Float
        get() = focalLengthMm.toFloat() * (36.0f / selectedAspectRatio.gateWidthMm)

    // Base Portrait 35mm equivalent focal length of phone's main 1.0x sensor
    val effectivePortraitBaseFocal: Float
        get() = if (opticsInfo.portraitEquivFocalMm > 0f) opticsInfo.portraitEquivFocalMm else 32.0f

    // Dynamic zoom ratio to send to CameraControl.setZoomRatio()
    val dynamicZoomRatio: Float
        get() {
            if (!isDigitalCropZoomEnabled) return 1.0f
            val base = effectivePortraitBaseFocal
            return if (base > 0f) (targetEquiv135Mm / base) else 1.0f
        }

    // Indicates whether the chosen focal length exceeds the phone's minimum hardware zoom FOV
    val isFocalExceedingPortraitSensor: Boolean
        get() = dynamicZoomRatio < hardwareMinZoomRatio

    // Focus Trigger Point for Camera2 AF / FocusMeteringAction
    val focusTriggerPoint: Pair<Float, Float>?
        get() = if (isDistanceMeasuring) Pair(distanceFocusPointNormX, distanceFocusPointNormY) else null

    val distanceNormX: Float
        get() = distanceFocusPointNormX

    val distanceNormY: Float
        get() = distanceFocusPointNormY

    // Current live EV100 based on active metering mode
    val currentLiveEv100: Double
        get() = if (isSpotMeteringActive) liveSpotEv100 else liveFrameEv100

    // Current active EV100 taking into account hold state
    val effectiveEv100: Double
        get() = lockedEv100 ?: currentLiveEv100

    // Evaluative Scene Analysis: Current spot's natural EV delta from scene middle gray baseline
    val naturalSpotDeltaEv: Double
        get() = (effectiveEv100 - liveFrameEv100)

    // Evaluative Scene Analysis: Natural zone that this spot intrinsically belongs to
    val naturalSpotZone: ZoneLevel
        get() = ZoneLevel.fromEvDifference(naturalSpotDeltaEv)

    // Evaluative Scene Analysis: Exposure deviation between user's target zone placement and natural state
    // When user places the spot to its natural zone (e.g. +2 EV cloud placed at Zone VII), placement shift is 0.0 EV (no preview change!)
    val targetZonePlacementShiftEv: Double
        get() = if (isZoneSystemEnabled && isSpotMeteringActive) {
            naturalSpotDeltaEv - selectedTargetZone.relativeEv
        } else 0.0

    // Target Camera EV for ISO calculations
    // To render a spot of luminance EV_spot at targetZone (which has relative offset relativeEv from Zone V):
    // The camera exposure must be set to EV_cam = EV_spot - relativeEv.
    val targetCameraEv100: Double
        get() = if (isZoneSystemEnabled && isSpotMeteringActive) {
            effectiveEv100 - selectedTargetZone.relativeEv
        } else {
            effectiveEv100
        }

    // Effective EV at current ISO, adjusted by Zone placement, exposure compensation, and ND filter
    val effectiveEvForIso: Double get() {
        val baseEv = ExposureCalculator.calculateEvForIso(targetCameraEv100, iso)
        val targetEv = baseEv - exposureCompensationEv + ndFilterStops
        return targetEv
    }

    // Recommended Shutter speed based on Priority Mode & Zone placement
    val recommendedShutterSec: Double get() {
        return ExposureCalculator.calculateShutterForAperture(effectiveEvForIso, selectedAperture)
    }

    // Recommended Aperture based on Priority Mode & Zone placement
    val recommendedAperture: Double get() {
        return ExposureCalculator.calculateApertureForShutter(effectiveEvForIso, selectedShutterSec)
    }

    // Effective active Aperture taking priority mode into account
    val effectiveAperture: Double
        get() = if (priorityMode == PriorityMode.SHUTTER) recommendedAperture else selectedAperture

    // Simulated real-time exposure shift stops for live preview shader
    // Delta from standard scene evaluative matrix preview: (EV_frame - EV_cam)
    // If user's placement matches the spot's natural brightness, preview remains identical to natural scene.
    // If user pulls a bright spot down to Zone V or pushes a shadow up, preview brightens/darkens dynamically!
    val simulatedExposureShiftStops: Double get() {
        val zonePreviewShift = if (isZoneSystemEnabled && isSpotMeteringActive) {
            -selectedTargetZone.relativeEv
        } else 0.0
        return zonePreviewShift + exposureCompensationEv - ndFilterStops
    }

    // Reciprocity compensated shutter speed for long exposures
    val reciprocityCompensatedShutterSec: Double get() {
        val targetShutter = if (priorityMode == PriorityMode.SHUTTER) selectedShutterSec else recommendedShutterSec
        return currentFilm.calculateReciprocity(targetShutter)
    }

    val reciprocityStopsAdded: Double get() {
        val targetShutter = if (priorityMode == PriorityMode.SHUTTER) selectedShutterSec else recommendedShutterSec
        val comp = reciprocityCompensatedShutterSec
        return if (targetShutter > 0 && comp >= targetShutter) {
            kotlin.math.log2(comp / targetShutter)
        } else 0.0
    }

    // Lux estimate
    val lux: Double get() = ExposureCalculator.ev100ToLux(effectiveEv100)
    val footCandles: Double get() = ExposureCalculator.luxToFootCandles(lux)
}

enum class ActiveSheet {
    FILM_RECIPROCITY,
    ZONE_GUIDE,
    SHOT_LOG,
    SETTINGS,
    FRAMELINE_SETTINGS,
    SECONDARY_MENU
}

class LightMeterViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LightMeterUiState())
    val uiState: StateFlow<LightMeterUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val prefs = application.getSharedPreferences("film_meter_prefs", Context.MODE_PRIVATE)

    init {
        // Load saved preferences
        val savedFilmId = prefs.getString("last_film_id", "default_color_film")
        val initialFilm = FilmDatabase.allFilms.firstOrNull { it.id == savedFilmId } ?: FilmDatabase.allFilms[0]
        val savedIso = prefs.getInt("last_iso", initialFilm.defaultIso)
        val savedCal = prefs.getFloat("last_calibration_ev", 0.0f).toDouble()
        val savedFocalMm = prefs.getInt("last_focal_mm", 50)
        val savedCropZoom = prefs.getBoolean("is_digital_crop_zoom_enabled", true)
        val savedAspectName = prefs.getString("last_aspect_name", FilmAspectRatio.FORMAT_135.name)
        val savedAspect = try {
            FilmAspectRatio.valueOf(savedAspectName ?: FilmAspectRatio.FORMAT_135.name)
        } catch (_: Exception) {
            FilmAspectRatio.FORMAT_135
        }
        val savedGridName = prefs.getString("last_grid_name", CompositionGridStyle.RULE_OF_THIRDS.name)
        val savedGrid = try {
            CompositionGridStyle.valueOf(savedGridName ?: CompositionGridStyle.RULE_OF_THIRDS.name)
        } catch (_: Exception) {
            CompositionGridStyle.RULE_OF_THIRDS
        }
        val savedMultiCam = prefs.getBoolean("is_multi_cam_enabled", false)
        val savedLensName = prefs.getString("camera_lens_name", CameraLensType.MAIN_WIDE.name)
        val savedLens = try {
            CameraLensType.valueOf(savedLensName ?: CameraLensType.MAIN_WIDE.name)
        } catch (_: Exception) {
            CameraLensType.MAIN_WIDE
        }
        val savedCameraId = prefs.getString("last_selected_camera_id", null)
        val savedLangCode = prefs.getString("last_app_language_code", AppLanguage.DEFAULT.code)
        val initialLang = AppLanguage.fromCode(savedLangCode)

        val savedManualBaseFocal = prefs.getFloat("manual_landscape_base_focal", 0.0f).takeIf { it > 0f }
        val detectedOptics = com.example.meter.camera.CameraOpticsDetector.detectBackCameraOptics(application, savedManualBaseFocal, savedCameraId)

        // Auto B&W filter if film is B&W
        val initialFilter = if (initialFilm.isBlackAndWhite) ColorFilterMode.BW_STANDARD else ColorFilterMode.NONE
        val focalChoice = FocalLengthChoice.fromFocal(savedFocalMm)

        val savedRangeModeName = prefs.getString("rangefinder_engine_mode", RangefinderEngineMode.INCLINOMETER.name)
        val savedRangeMode = try {
            RangefinderEngineMode.valueOf(savedRangeModeName ?: RangefinderEngineMode.INCLINOMETER.name)
        } catch (_: Exception) {
            RangefinderEngineMode.INCLINOMETER
        }
        val savedUserHeight = prefs.getFloat("user_camera_height_m", 1.50f)
        val savedAfScale = prefs.getFloat("af_distance_calibration_scale", 1.0f)

        // Load persistent shot logs
        val loadedLogs = loadPersistentShotLogs()
        val hasShownCalibPrompt = prefs.getBoolean("has_shown_calibration_prompt_v1", false)

        _uiState.update {
            it.copy(
                currentFilm = initialFilm,
                iso = savedIso,
                calibrationOffsetEv = savedCal,
                focalLengthMm = savedFocalMm,
                selectedFocalLength = focalChoice,
                isDigitalCropZoomEnabled = savedCropZoom,
                selectedAspectRatio = savedAspect,
                gridStyle = savedGrid,
                selectedColorFilter = initialFilter,
                isExperimentalMultiCameraEnabled = savedMultiCam,
                selectedCameraLens = savedLens,
                selectedCameraId = savedCameraId,
                opticsInfo = detectedOptics,
                manualLandscapeBaseFocalMm = savedManualBaseFocal,
                appLanguage = initialLang,
                rangefinderEngineMode = savedRangeMode,
                userHeightMeters = savedUserHeight,
                afDistanceCalibrationScale = savedAfScale,
                shotLogs = loadedLogs,
                showFirstLaunchCalibrationPrompt = !hasShownCalibPrompt
            )
        }
    }

    private val inclinometer = com.example.meter.camera.InclinometerRangefinder(application) { hDist, dDist, pitch, roll, isLevel ->
        _uiState.update {
            it.copy(
                inclinometerHorizontalDistMeters = hDist,
                inclinometerDirectDistMeters = dDist,
                inclinometerPitchDeg = pitch,
                inclinometerRollDeg = roll,
                isPhoneLevel = isLevel
            )
        }
    }

    fun onLuminanceResult(result: LuminanceAnalysisResult) {
        _uiState.update { state ->
            val smoothFactor = 0.30
            val smoothedSpotEv = state.liveSpotEv100 * (1 - smoothFactor) + result.spotEv100 * smoothFactor
            val smoothedFrameEv = state.liveFrameEv100 * (1 - smoothFactor) + result.frameEv100 * smoothFactor

            state.copy(
                liveSpotEv100 = smoothedSpotEv,
                liveFrameEv100 = smoothedFrameEv,
                spotLuminance = result.spotLuminance,
                frameLuminance = result.frameLuminance,
                zoneHistogram = result.zoneHistogram,
                sceneDynamicRangeStops = result.sceneDynamicRangeStops
            )
        }
    }

    fun toggleHoldReading() {
        _uiState.update { state ->
            if (state.isHoldingReading) {
                // Unlock
                state.copy(isHoldingReading = false, lockedEv100 = null)
            } else {
                // Lock
                vibrateTick(50)
                state.copy(isHoldingReading = true, lockedEv100 = state.currentLiveEv100)
            }
        }
    }

    /**
     * Tapping or dragging on viewfinder: Sets spot position, immediately activates spot metering
     */
    fun setSpotPosition(normX: Float, normY: Float) {
        val clampedX = normX.coerceIn(0.05f, 0.95f)
        val clampedY = normY.coerceIn(0.05f, 0.95f)
        _uiState.update {
            it.copy(
                spotNormX = clampedX,
                spotNormY = clampedY,
                isSpotMeteringActive = true,
                meteringMode = MeteringMode.SPOT
            )
        }
    }

    fun resetToAverageMetering() {
        exitSpotMetering()
    }

    /**
     * Exits Spot Metering and returns to Matrix / Frameline Average
     */
    fun exitSpotMetering() {
        vibrateTick(30)
        _uiState.update {
            it.copy(
                isSpotMeteringActive = false,
                meteringMode = MeteringMode.MATRIX,
                spotNormX = 0.5f,
                spotNormY = 0.5f
            )
        }
    }

    fun toggleZoneSystem() {
        vibrateTick(40)
        _uiState.update {
            val newState = !it.isZoneSystemEnabled
            it.copy(
                isZoneSystemEnabled = newState,
                isFalseColorEnabled = if (!newState) false else it.isFalseColorEnabled
            )
        }
    }

    fun setGridStyle(style: CompositionGridStyle) {
        prefs.edit().putString("last_grid_name", style.name).apply()
        _uiState.update { it.copy(gridStyle = style) }
    }

    fun setFramelineBounds(left: Float, top: Float, right: Float, bottom: Float) {
        _uiState.update {
            it.copy(
                framelineNormLeft = left,
                framelineNormTop = top,
                framelineNormRight = right,
                framelineNormBottom = bottom
            )
        }
    }

    /**
     * User assigns target Zone (Zone 0 to Zone X) for the spot
     */
    fun setTargetZone(zone: ZoneLevel) {
        vibrateTick(30)
        _uiState.update { it.copy(selectedTargetZone = zone) }
    }

    /**
     * Align target zone with program's real-time evaluative natural zone measurement (0 EV deviation)
     */
    fun alignTargetZoneToNatural() {
        val natural = _uiState.value.naturalSpotZone
        vibrateTick(35)
        _uiState.update { it.copy(selectedTargetZone = natural) }
    }

    /**
     * Toggles Rangefinder / Focus Distance measurement mode
     */
    fun toggleDistanceMode() {
        vibrateTick(40)
        _uiState.update { state ->
            val nextState = !state.isDistanceModeEnabled
            if (nextState) {
                if (state.rangefinderEngineMode == RangefinderEngineMode.INCLINOMETER) {
                    inclinometer.cameraHeightMeters = state.userHeightMeters
                    inclinometer.start()
                }
            } else {
                inclinometer.stop()
            }
            state.copy(
                isDistanceModeEnabled = nextState,
                measuredDistanceMeters = if (!nextState) null else state.measuredDistanceMeters,
                isDistanceInfinity = if (!nextState) false else state.isDistanceInfinity
            )
        }
    }

    /**
     * Sets the active rangefinder engine (Inclinometer Trigonometry, AF Optical, Stadiametric)
     */
    fun setRangefinderEngineMode(mode: RangefinderEngineMode) {
        prefs.edit().putString("rangefinder_engine_mode", mode.name).apply()
        if (_uiState.value.isDistanceModeEnabled) {
            if (mode == RangefinderEngineMode.INCLINOMETER) {
                inclinometer.cameraHeightMeters = _uiState.value.userHeightMeters
                inclinometer.start()
            } else {
                inclinometer.stop()
            }
        }
        _uiState.update { it.copy(rangefinderEngineMode = mode) }
    }

    /**
     * Sets user camera eye-level height in meters for trigonometric calculation
     */
    fun setUserHeight(heightMeters: Float) {
        val clamped = heightMeters.coerceIn(0.8f, 2.3f)
        prefs.edit().putFloat("user_camera_height_m", clamped).apply()
        inclinometer.cameraHeightMeters = clamped
        _uiState.update { it.copy(userHeightMeters = clamped) }
    }

    /**
     * Sets custom AF motor distance multiplier for lens calibration
     */
    fun setAfDistanceCalibrationScale(scale: Float) {
        val clamped = scale.coerceIn(0.2f, 5.0f)
        prefs.edit().putFloat("af_distance_calibration_scale", clamped).apply()
        _uiState.update { it.copy(afDistanceCalibrationScale = clamped) }
    }

    /**
     * 1-Tap 1.0 Meter Calibration for Camera2 AF Motor
     */
    fun calibrateAfAtOneMeter() {
        val currentDist = _uiState.value.measuredDistanceMeters ?: 1.0f
        if (currentDist > 0.05f) {
            val neededScale = 1.0f / currentDist
            setAfDistanceCalibrationScale(neededScale)
            vibrateCompletionPattern()
        }
    }

    /**
     * Dismisses the first-launch calibration onboarding dialog
     */
    fun dismissCalibrationPrompt(openCalibrationSheet: Boolean = false) {
        prefs.edit().putBoolean("has_shown_calibration_prompt_v1", true).apply()
        _uiState.update { it.copy(showFirstLaunchCalibrationPrompt = false) }
        if (openCalibrationSheet) {
            openSheet(ActiveSheet.SECONDARY_MENU)
        }
    }

    /**
     * Toggles whether distance measurement also moves spot meter target
     */
    fun toggleDistanceSpotLink() {
        vibrateTick(30)
        _uiState.update { it.copy(isDistanceSpotLinkEnabled = !it.isDistanceSpotLinkEnabled) }
    }

    /**
     * Called when user taps on viewfinder while in distance mode
     */
    fun onTriggerDistanceMeasurement(normX: Float, normY: Float) {
        val clampedX = normX.coerceIn(0.05f, 0.95f)
        val clampedY = normY.coerceIn(0.05f, 0.95f)
        vibrateTick(30)
        _uiState.update {
            it.copy(
                distanceFocusPointNormX = clampedX,
                distanceFocusPointNormY = clampedY,
                isDistanceMeasuring = true,
                isDistanceLowContrast = false
            )
        }
    }

    /**
     * Updates focus distance measurement from Camera2 CaptureResult.LENS_FOCUS_DISTANCE with calibration
     */
    fun onDistanceResult(distanceMeters: Float?, isInfinity: Boolean, isLowContrast: Boolean = false) {
        val calibratedDist = distanceMeters?.let { (it * _uiState.value.afDistanceCalibrationScale).coerceIn(0.1f, 150f) }
        _uiState.update {
            it.copy(
                measuredDistanceMeters = calibratedDist,
                isDistanceInfinity = isInfinity,
                isDistanceLowContrast = isLowContrast,
                isDistanceMeasuring = false
            )
        }
    }

    fun setAperture(aperture: Double) {
        _uiState.update { it.copy(selectedAperture = aperture) }
    }

    fun setShutterSpeed(shutterSec: Double) {
        _uiState.update { it.copy(selectedShutterSec = shutterSec) }
    }

    fun setIso(iso: Int) {
        prefs.edit().putInt("last_iso", iso).apply()
        _uiState.update { it.copy(iso = iso) }
    }

    fun setPriorityMode(mode: PriorityMode) {
        _uiState.update { it.copy(priorityMode = mode) }
    }

    fun setMeteringMode(mode: MeteringMode) {
        _uiState.update {
            it.copy(
                meteringMode = mode,
                isSpotMeteringActive = (mode == MeteringMode.SPOT)
            )
        }
    }

    fun setFilmStock(film: FilmStock) {
        prefs.edit()
            .putString("last_film_id", film.id)
            .putInt("last_iso", film.defaultIso)
            .apply()

        _uiState.update { state ->
            val updatedFilter = when {
                film.isBlackAndWhite -> ColorFilterMode.BW_STANDARD
                state.selectedColorFilter == ColorFilterMode.BW_STANDARD -> ColorFilterMode.NONE
                else -> state.selectedColorFilter
            }

            state.copy(
                currentFilm = film,
                iso = film.defaultIso,
                selectedColorFilter = updatedFilter
            )
        }
    }

    fun setAspectRatio(format: FilmAspectRatio) {
        prefs.edit().putString("last_aspect_name", format.name).apply()
        _uiState.update { it.copy(selectedAspectRatio = format) }
    }

    fun setFocalLength(choice: FocalLengthChoice) {
        setCustomFocalLength(choice.focalMm)
    }

    fun setCustomFocalLength(mm: Int) {
        val clamped = mm.coerceIn(10, 1200)
        val choice = FocalLengthChoice.fromFocal(clamped)
        prefs.edit().putInt("last_focal_mm", clamped).apply()

        _uiState.update { state ->
            val targetLens = if (state.isExperimentalMultiCameraEnabled) {
                when {
                    clamped <= 18 -> CameraLensType.ULTRA_WIDE
                    clamped in 19..35 -> CameraLensType.MAIN_WIDE
                    clamped in 36..65 -> CameraLensType.TELEPHOTO_2X
                    clamped in 66..110 -> CameraLensType.TELEPHOTO_3X
                    else -> CameraLensType.TELEPHOTO_5X
                }
            } else state.selectedCameraLens

            state.copy(
                focalLengthMm = clamped,
                selectedFocalLength = choice,
                selectedCameraLens = targetLens
            )
        }
    }

    fun toggleDigitalCropZoom() {
        _uiState.update {
            val newVal = !it.isDigitalCropZoomEnabled
            prefs.edit().putBoolean("is_digital_crop_zoom_enabled", newVal).apply()
            it.copy(isDigitalCropZoomEnabled = newVal)
        }
    }

    fun toggleExperimentalMultiCamera() {
        _uiState.update {
            val newVal = !it.isExperimentalMultiCameraEnabled
            prefs.edit().putBoolean("is_multi_cam_enabled", newVal).apply()
            it.copy(isExperimentalMultiCameraEnabled = newVal)
        }
    }

    fun toggleMultiCamera() {
        toggleExperimentalMultiCamera()
    }

    fun setExperimentalMultiCamera(enabled: Boolean) {
        prefs.edit().putBoolean("is_multi_cam_enabled", enabled).apply()
        _uiState.update { it.copy(isExperimentalMultiCameraEnabled = enabled) }
    }

    fun setAppLanguage(lang: AppLanguage) {
        prefs.edit().putString("last_app_language_code", lang.code).apply()
        _uiState.update { it.copy(appLanguage = lang) }
    }

    fun selectPhysicalLens(lens: PhysicalLensInfo) {
        prefs.edit().putString("last_selected_camera_id", lens.cameraId).apply()
        prefs.edit().putString("camera_lens_name", lens.lensType.name).apply()
        val manual = _uiState.value.manualLandscapeBaseFocalMm
        val updatedOptics = com.example.meter.camera.CameraOpticsDetector.detectBackCameraOptics(getApplication(), manual, lens.cameraId)
        _uiState.update {
            it.copy(
                selectedCameraId = lens.cameraId,
                selectedCameraLens = lens.lensType,
                opticsInfo = updatedOptics
            )
        }
    }

    fun setCameraLens(lens: CameraLensType) {
        prefs.edit().putString("camera_lens_name", lens.name).apply()
        val matchedPhysical = _uiState.value.opticsInfo.availableLenses.find { it.lensType == lens }
        val targetCamId = matchedPhysical?.cameraId ?: _uiState.value.selectedCameraId
        if (targetCamId != null) {
            prefs.edit().putString("last_selected_camera_id", targetCamId).apply()
        }
        _uiState.update { 
            it.copy(
                selectedCameraLens = lens,
                selectedCameraId = targetCamId
            ) 
        }
    }

    fun setColorFilter(filter: ColorFilterMode) {
        _uiState.update { it.copy(selectedColorFilter = filter) }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    fun toggleFalseColor() {
        _uiState.update { it.copy(isFalseColorEnabled = !it.isFalseColorEnabled) }
    }

    fun setExposureCompensation(ev: Double) {
        _uiState.update { it.copy(exposureCompensationEv = ev) }
    }

    fun setNdFilterStops(stops: Double) {
        _uiState.update { it.copy(ndFilterStops = stops) }
    }

    fun setCalibrationOffset(ev: Double) {
        val rounded = (ev * 10).roundToInt() / 10.0
        val delta = rounded - _uiState.value.calibrationOffsetEv
        prefs.edit().putFloat("last_calibration_ev", rounded.toFloat()).apply()
        _uiState.update { state ->
            val updatedLocked = state.lockedEv100?.let { it + delta }
            val updatedSpot = state.liveSpotEv100 + delta
            val updatedFrame = state.liveFrameEv100 + delta
            state.copy(
                calibrationOffsetEv = rounded,
                liveSpotEv100 = updatedSpot,
                liveFrameEv100 = updatedFrame,
                lockedEv100 = updatedLocked
            )
        }
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.update { it.copy(activeTabSheet = sheet) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeTabSheet = null) }
    }

    fun clearLastRecordedMessage() {
        _uiState.update { it.copy(lastRecordedMessage = null) }
    }

    // Multi-spot markers
    fun addCurrentSpotMarker() {
        val state = _uiState.value
        val newId = state.multiSpots.size + 1
        val marker = SpotMarker(
            id = newId,
            normX = state.spotNormX,
            normY = state.spotNormY,
            measuredEv100 = state.effectiveEv100,
            assignedZone = state.selectedTargetZone,
            label = "S$newId (EV ${String.format(java.util.Locale.US, "%.1f", state.effectiveEv100)})"
        )
        vibrateTick(40)
        _uiState.update { it.copy(multiSpots = it.multiSpots + marker) }
    }

    fun clearSpotMarkers() {
        _uiState.update { it.copy(multiSpots = emptyList()) }
    }

    // Save shot log with persistent storage
    fun logCurrentShot(note: String = "") {
        val state = _uiState.value
        val actualShutter = if (state.priorityMode == PriorityMode.SHUTTER) state.selectedShutterSec else state.recommendedShutterSec
        val actualAperture = if (state.priorityMode == PriorityMode.APERTURE) state.selectedAperture else state.recommendedAperture
        val compShutter = state.reciprocityCompensatedShutterSec

        val logItem = MeteringLog(
            filmName = state.currentFilm.name,
            iso = state.iso,
            apertureStr = ExposureCalculator.formatAperture(actualAperture),
            shutterStr = ExposureCalculator.formatShutter(actualShutter),
            compensatedShutterStr = ExposureCalculator.formatShutter(compShutter),
            ev100 = state.effectiveEv100,
            currentEv = state.effectiveEvForIso,
            targetZone = state.selectedTargetZone,
            note = note,
            focalLengthMm = state.focalLengthMm
        )

        vibrateTick(60)
        val updatedLogs = listOf(logItem) + state.shotLogs
        savePersistentShotLogs(updatedLogs)

        val bannerMsg = "已记录第 ${updatedLogs.size} 张: ${logItem.apertureStr} · ${logItem.shutterStr} · EV ${String.format(java.util.Locale.US, "%.1f", logItem.currentEv)}"

        _uiState.update {
            it.copy(
                shotLogs = updatedLogs,
                lastRecordedMessage = bannerMsg
            )
        }

        viewModelScope.launch {
            delay(3500)
            _uiState.update { it.copy(lastRecordedMessage = null) }
        }
    }

    fun deleteShotLog(id: Long) {
        val updated = _uiState.value.shotLogs.filterNot { it.id == id }
        savePersistentShotLogs(updated)
        _uiState.update { it.copy(shotLogs = updated) }
    }

    fun clearAllShotLogs() {
        savePersistentShotLogs(emptyList())
        _uiState.update { it.copy(shotLogs = emptyList()) }
    }

    private fun savePersistentShotLogs(logs: List<MeteringLog>) {
        try {
            val jsonArray = JSONArray()
            logs.take(100).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("timestampMs", item.timestampMs)
                    put("filmName", item.filmName)
                    put("iso", item.iso)
                    put("apertureStr", item.apertureStr)
                    put("shutterStr", item.shutterStr)
                    put("compensatedShutterStr", item.compensatedShutterStr)
                    put("ev100", item.ev100)
                    put("currentEv", item.currentEv)
                    put("targetZoneIndex", item.targetZone.index)
                    put("note", item.note)
                    put("focalLengthMm", item.focalLengthMm)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("persistent_shot_logs_json", jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadPersistentShotLogs(): List<MeteringLog> {
        val jsonStr = prefs.getString("persistent_shot_logs_json", null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<MeteringLog>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val item = MeteringLog(
                    id = obj.optLong("id", System.currentTimeMillis()),
                    timestampMs = obj.optLong("timestampMs", System.currentTimeMillis()),
                    filmName = obj.optString("filmName", "Film"),
                    iso = obj.optInt("iso", 400),
                    apertureStr = obj.optString("apertureStr", "f/2.8"),
                    shutterStr = obj.optString("shutterStr", "1/125"),
                    compensatedShutterStr = obj.optString("compensatedShutterStr", "1/125"),
                    ev100 = obj.optDouble("ev100", 10.0),
                    currentEv = obj.optDouble("currentEv", 12.0),
                    targetZone = ZoneLevel.fromIndex(obj.optInt("targetZoneIndex", 5)),
                    note = obj.optString("note", ""),
                    focalLengthMm = obj.optInt("focalLengthMm", 50)
                )
                list.add(item)
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Long exposure timer
    fun startReciprocityTimer() {
        val state = _uiState.value
        val totalSec = state.reciprocityCompensatedShutterSec
        if (totalSec <= 0.5) return

        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isTimerRunning = true,
                timerTotalSec = totalSec,
                timerRemainingSec = totalSec,
                isTimerCompleted = false
            )
        }

        timerJob = viewModelScope.launch {
            val intervalMs = 100L
            while (_uiState.value.timerRemainingSec > 0) {
                delay(intervalMs)
                _uiState.update { s ->
                    val rem = (s.timerRemainingSec - (intervalMs / 1000.0)).coerceAtLeast(0.0)
                    s.copy(timerRemainingSec = rem)
                }
            }
            // Completed!
            vibrateCompletionPattern()
            _uiState.update { it.copy(isTimerRunning = false, isTimerCompleted = true) }
        }
    }

    fun pauseReciprocityTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun resetReciprocityTimer() {
        timerJob?.cancel()
        val totalSec = _uiState.value.reciprocityCompensatedShutterSec
        _uiState.update {
            it.copy(
                isTimerRunning = false,
                timerTotalSec = totalSec,
                timerRemainingSec = totalSec,
                isTimerCompleted = false
            )
        }
    }

    fun setManualLandscapeBaseFocal(baseFocalMm: Float) {
        val clamped = baseFocalMm.coerceIn(18.0f, 45.0f)
        prefs.edit().putFloat("manual_landscape_base_focal", clamped).apply()
        val updatedOptics = com.example.meter.camera.CameraOpticsDetector.detectBackCameraOptics(getApplication(), clamped)
        _uiState.update {
            it.copy(
                manualLandscapeBaseFocalMm = clamped,
                opticsInfo = updatedOptics
            )
        }
    }

    fun resetManualLandscapeBaseFocal() {
        prefs.edit().remove("manual_landscape_base_focal").apply()
        val defaultOptics = com.example.meter.camera.CameraOpticsDetector.detectBackCameraOptics(getApplication(), null)
        _uiState.update {
            it.copy(
                manualLandscapeBaseFocalMm = null,
                opticsInfo = defaultOptics
            )
        }
    }

    fun refreshCameraOptics() {
        val manual = _uiState.value.manualLandscapeBaseFocalMm
        val optics = com.example.meter.camera.CameraOpticsDetector.detectBackCameraOptics(getApplication(), manual)
        _uiState.update { it.copy(opticsInfo = optics) }
    }

    fun updateHardwareZoomState(actualZoom: Float, minZoom: Float, maxZoom: Float) {
        _uiState.update {
            it.copy(
                hardwareZoomRatio = actualZoom,
                hardwareMinZoomRatio = minZoom,
                hardwareMaxZoomRatio = maxZoom
            )
        }
    }

    fun switchToUltraWide() {
        // Set focal length to ultra-wide (e.g. 18mm for 135, or equivalent for medium format)
        val targetUwFocal = (18f * (_uiState.value.selectedAspectRatio.gateWidthMm / 36.0f)).roundToInt().coerceAtLeast(10)
        setCustomFocalLength(targetUwFocal)
    }

    private fun vibrateTick(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    private fun vibrateCompletionPattern() {
        try {
            val context = getApplication<Application>()
            val timings = longArrayOf(0, 300, 150, 300, 150, 500)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        inclinometer.stop()
    }
}
