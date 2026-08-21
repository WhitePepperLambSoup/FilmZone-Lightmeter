package com.example.meter.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meter.model.AppLanguage
import com.example.meter.model.AppStrings
import com.example.meter.model.ColorFilterMode
import com.example.meter.model.CompositionGridStyle
import com.example.meter.model.FilmAspectRatio
import com.example.meter.model.FocalLengthChoice
import com.example.meter.model.PriorityMode
import com.example.meter.viewmodel.ActiveSheet
import com.example.meter.viewmodel.LightMeterViewModel
import com.example.ui.theme.MeterAccentOnPrimary
import com.example.ui.theme.MeterAccentPrimary
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBlack
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterBorderSubtle
import com.example.ui.theme.MeterCardBg
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterEmerald
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterTextMuted
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary

@Composable
fun MainScreen(
    viewModel: LightMeterViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val s = AppStrings.get(uiState.appLanguage)
    val isZh = uiState.appLanguage == AppLanguage.SIMPLIFIED_CHINESE || uiState.appLanguage == AppLanguage.TRADITIONAL_CHINESE

    // Camera permission check
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MeterBlack),
        containerColor = MeterBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. TOP BAR: Compact Header with High-Visibility Action Buttons
            TopControlBar(
                filmName = uiState.currentFilm.name,
                iso = uiState.iso,
                aspectRatio = uiState.selectedAspectRatio,
                focalLengthMm = uiState.focalLengthMm,
                isZoneSystemEnabled = uiState.isZoneSystemEnabled,
                isDistanceModeEnabled = uiState.isDistanceModeEnabled,
                isTorchOn = uiState.isTorchOn,
                logCount = uiState.shotLogs.size,
                appLanguage = uiState.appLanguage,
                onOpenFilmSheet = { viewModel.openSheet(ActiveSheet.FILM_RECIPROCITY) },
                onOpenFramelineSheet = { viewModel.openSheet(ActiveSheet.FRAMELINE_SETTINGS) },
                onToggleZoneSystem = { viewModel.toggleZoneSystem() },
                onToggleDistanceMode = { viewModel.toggleDistanceMode() },
                onToggleTorch = { viewModel.toggleTorch() },
                onOpenSecondaryMenu = { viewModel.openSheet(ActiveSheet.SECONDARY_MENU) },
                onOpenShotLogs = { viewModel.openSheet(ActiveSheet.SHOT_LOG) }
            )

            // 2. VIEWFINDER AREA (Hero Camera Chamber - Fixed rock-solid aspect ratio to prevent UI shifts)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black)
                    .border(1.dp, MeterBorder, RoundedCornerShape(22.dp))
            ) {
                if (hasCameraPermission) {
                    CameraPreviewView(
                        targetCameraId = uiState.selectedCameraId,
                        targetCameraLens = uiState.selectedCameraLens,
                        spotNormX = uiState.spotNormX,
                        spotNormY = uiState.spotNormY,
                        frameNormLeft = uiState.framelineNormLeft,
                        frameNormTop = uiState.framelineNormTop,
                        frameNormRight = uiState.framelineNormRight,
                        frameNormBottom = uiState.framelineNormBottom,
                        focusTriggerPoint = uiState.focusTriggerPoint,
                        calibrationOffset = uiState.calibrationOffsetEv,
                        isFalseColorEnabled = uiState.isFalseColorEnabled,
                        isTorchOn = uiState.isTorchOn,
                        colorFilter = uiState.selectedColorFilter,
                        simulatedExposureShiftStops = uiState.simulatedExposureShiftStops,
                        zoomFactor = uiState.dynamicZoomRatio,
                        onLuminanceResult = { viewModel.onLuminanceResult(it) },
                        onDistanceResult = { dist, isInf, isLowContrast -> viewModel.onDistanceResult(dist, isInf, isLowContrast) },
                        onZoomStateChanged = { actual, minZ, maxZ -> viewModel.updateHardwareZoomState(actual, minZ, maxZ) }
                    )
                } else {
                    // Permission Request Prompt Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MeterCardBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera Required",
                                tint = MeterAccentPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = s.cameraPermissionRequired,
                                color = MeterTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = s.cameraPermissionDesc,
                                color = MeterTextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = MeterAccentPrimary, contentColor = MeterAccentOnPrimary),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("grant_camera_permission_btn")
                            ) {
                                Text(s.grantPermission, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Viewfinder Framing, Masks, Reticles, and Exposure Simulation
                ViewfinderOverlay(
                    spotNormX = uiState.spotNormX,
                    spotNormY = uiState.spotNormY,
                    aspectRatio = uiState.selectedAspectRatio,
                    focalLengthMm = uiState.focalLengthMm,
                    isDigitalCropZoomEnabled = uiState.isDigitalCropZoomEnabled,
                    cameraLens = uiState.selectedCameraLens,
                    isExperimentalMultiCameraEnabled = uiState.isExperimentalMultiCameraEnabled,
                    opticsInfo = uiState.opticsInfo,
                    hardwareZoomRatio = uiState.hardwareZoomRatio,
                    hardwareMinZoomRatio = uiState.hardwareMinZoomRatio,
                    gridStyle = uiState.gridStyle,
                    colorFilter = uiState.selectedColorFilter,
                    targetZone = uiState.selectedTargetZone,
                    liveEv100 = uiState.effectiveEv100,
                    effectiveAperture = uiState.effectiveAperture,
                    isHold = uiState.isHoldingReading,
                    isFalseColor = uiState.isFalseColorEnabled,
                    isSpotMeteringActive = uiState.isSpotMeteringActive,
                    isZoneSystemEnabled = uiState.isZoneSystemEnabled,
                    isDistanceModeEnabled = uiState.isDistanceModeEnabled,
                    isDistanceSpotLinkEnabled = uiState.isDistanceSpotLinkEnabled,
                    rangefinderEngineMode = uiState.rangefinderEngineMode,
                    measuredDistanceMeters = uiState.measuredDistanceMeters,
                    isDistanceInfinity = uiState.isDistanceInfinity,
                    isDistanceLowContrast = uiState.isDistanceLowContrast,
                    isDistanceMeasuring = uiState.isDistanceMeasuring,
                    distanceNormX = uiState.distanceNormX,
                    distanceNormY = uiState.distanceNormY,
                    inclinometerHorizontalDistMeters = uiState.inclinometerHorizontalDistMeters,
                    inclinometerDirectDistMeters = uiState.inclinometerDirectDistMeters,
                    inclinometerPitchDeg = uiState.inclinometerPitchDeg,
                    inclinometerRollDeg = uiState.inclinometerRollDeg,
                    isPhoneLevel = uiState.isPhoneLevel,
                    multiSpots = uiState.multiSpots,
                    appLanguage = uiState.appLanguage,
                    onSpotMoved = { x, y -> viewModel.setSpotPosition(x, y) },
                    onTapToSetSpot = { x, y -> viewModel.setSpotPosition(x, y) },
                    onTriggerDistance = { x, y -> viewModel.onTriggerDistanceMeasurement(x, y) },
                    onToggleDistanceSpotLink = { viewModel.toggleDistanceSpotLink() },
                    onResetToAverage = { viewModel.exitSpotMetering() },
                    onOpenFramelines = { viewModel.openSheet(ActiveSheet.FRAMELINE_SETTINGS) },
                    onOpenSecondaryMenu = { viewModel.openSheet(ActiveSheet.SECONDARY_MENU) },
                    onToggleZoneSystem = { viewModel.toggleZoneSystem() },
                    onSwitchToUltraWide = { viewModel.switchToUltraWide() },
                    onFramelineBoundsChanged = { left, top, right, bottom ->
                        viewModel.setFramelineBounds(left, top, right, bottom)
                    }
                )

                // Multi-Spot Pin Floating Button on Viewfinder (when Zone System enabled)
                if (uiState.isZoneSystemEnabled) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (uiState.multiSpots.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC1A1C1E))
                                    .border(1.dp, MeterRed, CircleShape)
                                    .clickable { viewModel.clearSpotMarkers() }
                                    .testTag("clear_spots_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Spots", tint = MeterRed, modifier = Modifier.size(16.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xCC1A1C1E))
                                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(16.dp))
                                .clickable { viewModel.addCurrentSpotMarker() }
                                .padding(horizontal = 8.dp)
                                .testTag("add_spot_marker_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddLocation, contentDescription = "Pin Spot", tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (uiState.multiSpots.isEmpty()) s.pinSpot else "${s.pinSpot}(${uiState.multiSpots.size})",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Toast/Banner Notification on successful shot logging
                if (uiState.lastRecordedMessage != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xEE1A1C1E))
                            .border(1.dp, MeterEmerald, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = uiState.lastRecordedMessage ?: "",
                            color = MeterEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 3. LOWER CONTROL DECK (Scrollable & dynamically adapting without ever squeezing the viewfinder)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ANSEL ADAMS ZONE SYSTEM PANEL (Animated Expansion when enabled)
                AnimatedVisibility(
                    visible = uiState.isZoneSystemEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ZoneSystemPanel(
                        selectedZone = uiState.selectedTargetZone,
                        naturalZone = uiState.naturalSpotZone,
                        naturalDeltaEv = uiState.naturalSpotDeltaEv,
                        placementShiftEv = uiState.simulatedExposureShiftStops,
                        zoneHistogram = uiState.zoneHistogram,
                        multiSpots = uiState.multiSpots,
                        isFalseColor = uiState.isFalseColorEnabled,
                        appLanguage = uiState.appLanguage,
                        onZoneSelected = { viewModel.setTargetZone(it) },
                        onAlignNatural = { viewModel.alignTargetZoneToNatural() },
                        onToggleFalseColor = { viewModel.toggleFalseColor() },
                        onOpenZoneGuide = { viewModel.openSheet(ActiveSheet.ZONE_GUIDE) }
                    )
                }

                // TACTICAL EXPOSURE CONTROL DECK
                ExposureControlDials(
                    ev100 = uiState.effectiveEv100,
                    evForIso = uiState.effectiveEvForIso,
                    lux = uiState.lux,
                    iso = uiState.iso,
                    selectedAperture = uiState.selectedAperture,
                    selectedShutterSec = uiState.selectedShutterSec,
                    recommendedAperture = uiState.recommendedAperture,
                    recommendedShutterSec = uiState.recommendedShutterSec,
                    reciprocityCompensatedShutterSec = uiState.reciprocityCompensatedShutterSec,
                    reciprocityStopsAdded = uiState.reciprocityStopsAdded,
                    priorityMode = uiState.priorityMode,
                    isHold = uiState.isHoldingReading,
                    exposureCompEv = uiState.exposureCompensationEv,
                    ndStops = uiState.ndFilterStops,
                    appLanguage = uiState.appLanguage,
                    onSetAperture = { viewModel.setAperture(it) },
                    onSetShutter = { viewModel.setShutterSpeed(it) },
                    onSetIso = { viewModel.setIso(it) },
                    onSetPriorityMode = { viewModel.setPriorityMode(it) },
                    onToggleHold = { viewModel.toggleHoldReading() },
                    onSetExposureComp = { viewModel.setExposureCompensation(it) },
                    onSetNdStops = { viewModel.setNdFilterStops(it) },
                    onLogShot = { viewModel.logCurrentShot() },
                    onOpenReciprocityTimer = { viewModel.openSheet(ActiveSheet.FILM_RECIPROCITY) }
                )

                // 4. DEDICATED QUICK MENU DOCK (Distance + Menu Direct Action Buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Distance quick toggle button on main dock
                    Box(
                        modifier = Modifier
                            .weight(0.42f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (uiState.isDistanceModeEnabled) Color(0xFF0F2D1F) else MeterCardBg)
                            .border(
                                1.2.dp,
                                if (uiState.isDistanceModeEnabled) MeterEmerald else MeterBorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.toggleDistanceMode() }
                            .padding(vertical = 11.dp, horizontal = 8.dp)
                            .testTag("main_screen_distance_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = "Distance",
                                tint = if (uiState.isDistanceModeEnabled) MeterEmerald else MeterTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isDistanceModeEnabled) s.distanceOn else s.distanceOff,
                                color = if (uiState.isDistanceModeEnabled) MeterEmerald else MeterTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Main Menu Button
                    Box(
                        modifier = Modifier
                            .weight(0.58f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F2438))
                            .border(1.2.dp, MeterCyan, RoundedCornerShape(12.dp))
                            .clickable { viewModel.openSheet(ActiveSheet.SECONDARY_MENU) }
                            .padding(vertical = 11.dp, horizontal = 10.dp)
                            .testTag("main_screen_secondary_menu_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Menu",
                                tint = MeterCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = s.menuCalibration,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Bottom Sheets
    when (uiState.activeTabSheet) {
        ActiveSheet.FILM_RECIPROCITY -> {
            ReciprocityTimerSheet(
                currentFilm = uiState.currentFilm,
                meteredShutterSec = if (uiState.priorityMode == PriorityMode.SHUTTER) uiState.selectedShutterSec else uiState.recommendedShutterSec,
                compensatedShutterSec = uiState.reciprocityCompensatedShutterSec,
                stopsAdded = uiState.reciprocityStopsAdded,
                isTimerRunning = uiState.isTimerRunning,
                timerTotalSec = uiState.timerTotalSec,
                timerRemainingSec = uiState.timerRemainingSec,
                isTimerCompleted = uiState.isTimerCompleted,
                onFilmSelected = { viewModel.setFilmStock(it) },
                onStartTimer = { viewModel.startReciprocityTimer() },
                onPauseTimer = { viewModel.pauseReciprocityTimer() },
                onResetTimer = { viewModel.resetReciprocityTimer() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.FRAMELINE_SETTINGS -> {
            FramelineSettingsSheet(
                selectedAspectRatio = uiState.selectedAspectRatio,
                focalLengthMm = uiState.focalLengthMm,
                gridStyle = uiState.gridStyle,
                selectedColorFilter = uiState.selectedColorFilter,
                isExperimentalMultiCameraEnabled = uiState.isExperimentalMultiCameraEnabled,
                selectedCameraLens = uiState.selectedCameraLens,
                opticsInfo = uiState.opticsInfo,
                manualBaseFocal = uiState.manualLandscapeBaseFocalMm,
                appLanguage = uiState.appLanguage,
                onSelectAspectRatio = { viewModel.setAspectRatio(it) },
                onSelectCustomFocalLength = { viewModel.setCustomFocalLength(it) },
                onSelectGridStyle = { viewModel.setGridStyle(it) },
                onSelectColorFilter = { viewModel.setColorFilter(it) },
                onToggleMultiCamera = { viewModel.toggleMultiCamera() },
                onSelectCameraLens = { viewModel.setCameraLens(it) },
                onSetManualBaseFocal = { viewModel.setManualLandscapeBaseFocal(it) },
                onResetManualBaseFocal = { viewModel.resetManualLandscapeBaseFocal() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.ZONE_GUIDE -> {
            ZoneGuideSheet(
                appLanguage = uiState.appLanguage,
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.SHOT_LOG -> {
            ShotLogSheet(
                logs = uiState.shotLogs,
                onDeleteLog = { viewModel.deleteShotLog(it) },
                onClearAllLogs = { viewModel.clearAllShotLogs() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.SETTINGS,
        ActiveSheet.SECONDARY_MENU -> {
            SecondaryMenuSheet(
                calibrationOffset = uiState.calibrationOffsetEv,
                meteringMode = uiState.meteringMode,
                isSpotMeteringActive = uiState.isSpotMeteringActive,
                isExperimentalMultiCameraEnabled = uiState.isExperimentalMultiCameraEnabled,
                selectedCameraLens = uiState.selectedCameraLens,
                selectedCameraId = uiState.selectedCameraId,
                selectedColorFilter = uiState.selectedColorFilter,
                isZoneSystemEnabled = uiState.isZoneSystemEnabled,
                isFalseColorEnabled = uiState.isFalseColorEnabled,
                isDistanceModeEnabled = uiState.isDistanceModeEnabled,
                isDistanceSpotLinkEnabled = uiState.isDistanceSpotLinkEnabled,
                rangefinderEngineMode = uiState.rangefinderEngineMode,
                userHeightMeters = uiState.userHeightMeters,
                afDistanceCalibrationScale = uiState.afDistanceCalibrationScale,
                gridStyle = uiState.gridStyle,
                isTorchOn = uiState.isTorchOn,
                appLanguage = uiState.appLanguage,
                opticsInfo = uiState.opticsInfo,
                manualBaseFocal = uiState.manualLandscapeBaseFocalMm,
                onSetCalibrationOffset = { viewModel.setCalibrationOffset(it) },
                onSetMeteringMode = { viewModel.setMeteringMode(it) },
                onExitSpotMetering = { viewModel.exitSpotMetering() },
                onToggleDistanceMode = { viewModel.toggleDistanceMode() },
                onToggleDistanceSpotLink = { viewModel.toggleDistanceSpotLink() },
                onSelectRangefinderEngineMode = { viewModel.setRangefinderEngineMode(it) },
                onSetUserHeight = { viewModel.setUserHeight(it) },
                onSetAfDistanceCalibrationScale = { viewModel.setAfDistanceCalibrationScale(it) },
                onCalibrateAfAtOneMeter = { viewModel.calibrateAfAtOneMeter() },
                onToggleMultiCamera = { viewModel.toggleMultiCamera() },
                onSelectCameraLens = { viewModel.setCameraLens(it) },
                onSelectPhysicalLens = { viewModel.selectPhysicalLens(it) },
                onSelectColorFilter = { viewModel.setColorFilter(it) },
                onToggleZoneSystem = { viewModel.toggleZoneSystem() },
                onToggleFalseColor = { viewModel.toggleFalseColor() },
                onSelectGridStyle = { viewModel.setGridStyle(it) },
                onToggleTorch = { viewModel.toggleTorch() },
                onSelectLanguage = { viewModel.setAppLanguage(it) },
                onSetManualBaseFocal = { viewModel.setManualLandscapeBaseFocal(it) },
                onResetManualBaseFocal = { viewModel.resetManualLandscapeBaseFocal() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        null -> {}
    }

    // First Launch Calibration Recommendation Dialog
    if (uiState.showFirstLaunchCalibrationPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCalibrationPrompt(openCalibrationSheet = false) },
            containerColor = Color(0xFF101924),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MeterAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MeterAmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.firstLaunchCalibrationTitle,
                        color = MeterTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = s.firstLaunchCalibrationDesc,
                        color = MeterTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCalibrationPrompt(openCalibrationSheet = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MeterCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = s.calibrateNowBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissCalibrationPrompt(openCalibrationSheet = false) }
                ) {
                    Text(
                        text = s.gotItBtn,
                        color = MeterTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun TopControlBar(
    filmName: String,
    iso: Int,
    aspectRatio: FilmAspectRatio,
    focalLengthMm: Int,
    isZoneSystemEnabled: Boolean,
    isDistanceModeEnabled: Boolean = false,
    isTorchOn: Boolean,
    logCount: Int,
    appLanguage: AppLanguage = AppLanguage.DEFAULT,
    onOpenFilmSheet: () -> Unit,
    onOpenFramelineSheet: () -> Unit,
    onToggleZoneSystem: () -> Unit,
    onToggleDistanceMode: () -> Unit = {},
    onToggleTorch: () -> Unit,
    onOpenSecondaryMenu: () -> Unit,
    onOpenShotLogs: () -> Unit
) {
    val s = AppStrings.get(appLanguage)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MeterBlack)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Film Capsule
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MeterAccentPrimary)
                .clickable { onOpenFilmSheet() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("top_film_badge_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = filmName.take(10).uppercase(),
                    color = MeterAccentOnPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "· $iso",
                    color = MeterAccentOnPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Framelines & Focal Pill (Directly opens Framelines Sheet)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MeterCardBg)
                .border(1.dp, MeterBorderSubtle, RoundedCornerShape(50))
                .clickable { onOpenFramelineSheet() }
                .padding(horizontal = 9.dp, vertical = 5.dp)
                .testTag("open_framelines_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Frameline",
                    tint = MeterAmber,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${aspectRatio.subLabel.take(5)} · ${focalLengthMm}mm",
                    color = MeterTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Zone System quick toggle button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isZoneSystemEnabled) MeterAmber.copy(alpha = 0.2f) else MeterCardBg)
                .border(1.dp, if (isZoneSystemEnabled) MeterAmber else MeterBorderSubtle, RoundedCornerShape(50))
                .clickable { onToggleZoneSystem() }
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("toggle_zone_system_btn"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tonality,
                    contentDescription = "Zone",
                    tint = if (isZoneSystemEnabled) MeterAmberBright else MeterTextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = s.zoneSystem.take(4),
                    color = if (isZoneSystemEnabled) MeterAmberBright else MeterTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Distance Mode quick toggle button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isDistanceModeEnabled) MeterEmerald.copy(alpha = 0.25f) else MeterCardBg)
                .border(1.dp, if (isDistanceModeEnabled) MeterEmerald else MeterBorderSubtle, RoundedCornerShape(50))
                .clickable { onToggleDistanceMode() }
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("toggle_distance_mode_top_btn"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Distance",
                    tint = if (isDistanceModeEnabled) MeterEmerald else MeterTextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = s.rangefinder.take(4),
                    color = if (isDistanceModeEnabled) MeterEmerald else MeterTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Torch toggle button
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (isTorchOn) MeterAmber.copy(alpha = 0.2f) else MeterCardBg)
                .border(1.dp, if (isTorchOn) MeterAmber else MeterBorderSubtle, CircleShape)
                .clickable { onToggleTorch() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Torch",
                tint = if (isTorchOn) MeterAmberBright else MeterTextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        // Shot Log button
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MeterCardBg)
                .border(1.dp, MeterBorderSubtle, CircleShape)
                .clickable { onOpenShotLogs() }
                .testTag("open_logs_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "Logs",
                tint = if (logCount > 0) MeterAmberBright else MeterTextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        // Menu Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF0F2438))
                .border(1.2.dp, MeterCyan, RoundedCornerShape(50))
                .clickable { onOpenSecondaryMenu() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("open_secondary_menu_btn"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Menu",
                    tint = MeterCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = s.menu,
                    color = MeterCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
