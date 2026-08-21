package com.example.meter.model

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Ansel Adams 11-Zone System (Zone 0 to Zone X)
 */
enum class ZoneLevel(
    val index: Int,
    val roman: String,
    val relativeEv: Double, // Relative EV offset from middle gray (Zone V = 0 EV)
    val titleEn: String,
    val descriptionEn: String,
    val titleZh: String,
    val descriptionZh: String,
    val previewColor: Color,
    val falseColorHex: Long
) {
    ZONE_0(0, "0", -5.0, "Pure Black / No Detail", "Film D-Max, total black without silver grain exposure", "极黑 / 无细节", "胶片最大密度 D-Max，全黑无银盐感光", Color(0xFF000000), 0xFF000080),
    ZONE_I(1, "I", -4.0, "Near Black / Tonality Edge", "Slight tone without discernible texture, darkroom edge", "近乎全黑", "略有调性但无可见纹理，暗室边缘", Color(0xFF181818), 0xFF0000FF),
    ZONE_II(2, "II", -3.0, "First Shadow Texture", "Beginning of shadow texture and threshold detail", "初现影调", "可隐约察觉阴影纹理的起始区", Color(0xFF303030), 0xFF0080FF),
    ZONE_III(3, "III", -2.0, "Textured Deep Shadow", "Dark fabrics, bark in shade, shadow calibration anchor", "富质感深暗部", "深色衣物、树皮阴影、暗部岩石（阴影标准定标点）", Color(0xFF4C4C4C), 0xFF00FFFF),
    ZONE_IV(4, "IV", -1.0, "Dark Midtone", "Dark skin, shaded foliage, deep landscape elements", "较暗中调", "深色皮肤、阴影中的草地、阳光下的深色景物", Color(0xFF6B6B6B), 0xFF00FF80),
    ZONE_V(5, "V", 0.0, "18% Middle Gray", "Standard neutral gray, clear north sky, weathered wood, meter reference", "标准中灰 (18%)", "标准中性灰、晴朗北空、风化木材、测光表基准", Color(0xFF8E8E8E), 0xFF00FF00),
    ZONE_VI(6, "VI", +1.0, "Average Skin Tone", "Caucasian/Asian skin in sunlight, light concrete", "明亮肤色 / 浅调", "亚洲人/高加索人正常肤色、阳光下浅水泥地", Color(0xFFB0B0B0), 0xFFFFFF00),
    ZONE_VII(7, "VII", +2.0, "Light Midtone / Pale Gray", "Light clothing, side-lit snow, very pale skin", "明亮浅调 / 浅灰", "浅色衣服、白雪侧光面、亮水泥、极浅肤色", Color(0xFFCECECE), 0xFFFF8000),
    ZONE_VIII(8, "VIII", +3.0, "Textured Highlight", "White paper, wedding dress, textured front-lit snow (highlight texture ceiling)", "富质感高光", "白纸、新娘婚纱、顺光雪地（高光可保留纹理上限）", Color(0xFFE8E8E8), 0xFFFF0000),
    ZONE_IX(9, "IX", +4.0, "Glaring White / Soft Highlight", "Slight tonality without visible texture, specular margin", "近乎纯白", "微弱调性但无纹理，刺眼亮部边缘", Color(0xFFF6F6F6), 0xFFFF00FF),
    ZONE_X(10, "X", +5.0, "Pure White / Specular Highlight", "Direct light sources, sun reflection, clear film base", "纯白 / 镜面高光", "直射光源、反光镜面、胶片片基透明区", Color(0xFFFFFFFF), 0xFFFFFFFF);

    companion object {
        fun fromIndex(index: Int): ZoneLevel {
            return entries.getOrNull(index.coerceIn(0, 10)) ?: ZONE_V
        }

        fun fromEvDifference(deltaEv: Double): ZoneLevel {
            val roundedIndex = (5 + deltaEv).roundToInt().coerceIn(0, 10)
            return fromIndex(roundedIndex)
        }
    }
}

/**
 * Aspect Ratios and Physical Film Gate Dimensions for Film Formats
 */
enum class FilmAspectRatio(
    val labelZh: String,
    val labelEn: String,
    val subLabel: String,
    val widthRatio: Float,
    val heightRatio: Float,
    val gateWidthMm: Float = 36f,
    val gateHeightMm: Float = 24f
) {
    FORMAT_135("135 全画幅", "135 Full Frame", "3:2 (35mm)", 3f, 2f, 36.0f, 24.0f),
    FORMAT_645("中画幅 645", "Medium Format 645", "4:3 (6x4.5cm)", 4f, 3f, 56.0f, 41.5f),
    FORMAT_66("中画幅 6x6", "Medium Format 6x6", "1:1 (Hasselblad/Rolleiflex)", 1f, 1f, 56.0f, 56.0f),
    FORMAT_67("中画幅 6x7", "Medium Format 6x7", "7:6 (RZ67/P67)", 7f, 6f, 70.0f, 56.0f),
    FORMAT_69("中画幅 6x9", "Medium Format 6x9", "3:2 (GW690 Texas Leica)", 3f, 2f, 84.0f, 56.0f),
    FORMAT_XPAN("宽幅 XPan", "Panoramic XPan", "65:24 (TX-1/XPan)", 65f, 24f, 65.0f, 24.0f),
    FORMAT_612("全景 6x12", "Panorama 6x12", "2:1 (Horseman/Linhof)", 2f, 1f, 112.0f, 56.0f),
    FORMAT_617("全景 6x17", "Panorama 6x17", "3:1 (GX617/Linhof)", 3f, 1f, 168.0f, 56.0f),
    FORMAT_45("大画幅 4x5", "Large Format 4x5", "5:4 (View Camera)", 5f, 4f, 120.0f, 95.0f),
    FORMAT_810("大画幅 8x10", "Large Format 8x10", "5:4 (8x10 Inch)", 5f, 4f, 240.0f, 190.0f);

    val ratio: Float get() = widthRatio / heightRatio
}

/**
 * Camera Optical Properties and Sensor Dimensions detected via Camera2
 */
data class CameraOpticsInfo(
    val cameraId: String = "0",
    val physicalFocalLengthMm: Float = 4.4f,
    val sensorWidthMm: Float = 6.4f,
    val sensorHeightMm: Float = 4.8f,
    val sensorOrientation: Int = 90,
    val nativeSensorAspectRatio: Float = 4f / 3f,
    
    // Theoretical 35mm equivalent in full-frame Landscape (36x24mm)
    val landscapeEquivFocalMm: Float = 24.0f,
    
    // Theoretical 35mm equivalent in full-frame Portrait (when phone is held vertically)
    val portraitEquivFocalMm: Float = 32.0f,
    
    // Horizontal and Vertical FOV in degrees for Portrait holding
    val portraitHFOV: Float = 58.7f,
    val portraitVFOV: Float = 73.7f,
    
    // Available physical camera lenses on this phone
    val availableLenses: List<PhysicalLensInfo> = emptyList(),
    val isAutoDetected: Boolean = false
)

data class PhysicalLensInfo(
    val cameraId: String,
    val facing: Int,
    val physicalFocalLengthMm: Float,
    val sensorWidthMm: Float,
    val sensorHeightMm: Float,
    val landscapeEquivFocalMm: Float,
    val portraitEquivFocalMm: Float,
    val lensType: CameraLensType,
    val displayName: String
)

/**
 * Viewfinder Composition Grid Guides
 */
enum class CompositionGridStyle(
    val labelZh: String,
    val labelEn: String,
    val shortName: String,
    val descriptionZh: String,
    val descriptionEn: String
) {
    RULE_OF_THIRDS("三分构图线", "Rule of Thirds", "3x3", "经典九宫格三分线，适合风景与主体平衡", "Classic 3x3 grid for balanced framing"),
    GOLDEN_RATIO("黄金分割线", "Golden Ratio", "Phi", "1:0.618 斐波那契黄金比例线", "1:0.618 Fibonacci golden spiral & ratio"),
    CENTER_CROSS("十字中心线", "Center Cross", "Cross", "精准中心对称与水平基准", "Precision center symmetry crosshair"),
    DIAGONAL("对角引导线", "Diagonals", "Diag", "动态透视与对角线构图", "Dynamic perspective diagonal lines"),
    NONE("关闭网格", "Off", "Off", "纯净无遮挡取景框", "Clean unobstructed viewfinder")
}

/**
 * Camera Lens Multiplier Types for Multi-Camera Switching
 */
enum class CameraLensType(val labelZh: String, val labelEn: String, val zoomRatio: Float, val baseFocalMm: Int) {
    ULTRA_WIDE("0.6x 超广角 (14mm)", "0.6x Ultra Wide (14mm)", 0.6f, 14),
    MAIN_WIDE("1.0x 标准主摄 (24mm)", "1.0x Main Wide (24mm)", 1.0f, 24),
    TELEPHOTO_2X("2.0x 长焦 (50mm)", "2.0x Telephoto (50mm)", 2.0f, 50),
    TELEPHOTO_3X("3.0x 人像长焦 (75mm)", "3.0x Portrait Tele (75mm)", 3.0f, 75),
    TELEPHOTO_5X("5.0x 超长焦 (120mm)", "5.0x Super Tele (120mm)", 5.0f, 120)
}

/**
 * Common Focal Length Presets for Quick Selection (35mm equivalent)
 */
data class FocalPreset(
    val focalMm: Int,
    val nameZh: String,
    val nameEn: String,
    val categoryZh: String,
    val categoryEn: String
)

val COMMON_FOCAL_PRESETS = listOf(
    FocalPreset(14, "14mm", "14mm", "超广角", "Ultra Wide"),
    FocalPreset(21, "21mm", "21mm", "超广角", "Ultra Wide"),
    FocalPreset(24, "24mm", "24mm", "风光广角", "Landscape Wide"),
    FocalPreset(28, "28mm", "28mm", "经典街头 (GR/Q)", "Street (GR/Q)"),
    FocalPreset(35, "35mm", "35mm", "人文之眼 (Leica)", "Reportage (Leica)"),
    FocalPreset(40, "40mm", "40mm", "自然标头 (Cle)", "Natural Standard"),
    FocalPreset(43, "43mm", "43mm", "黄金标头 (FA43)", "Golden Standard"),
    FocalPreset(50, "50mm", "50mm", "标准标头 (Summicron)", "Classic 50mm"),
    FocalPreset(58, "58mm", "58mm", "经典八枚 (Helios)", "Helios 58mm"),
    FocalPreset(75, "75mm", "75mm", "中画幅标头", "Medium Format Normal"),
    FocalPreset(85, "85mm", "85mm", "人像定焦 (Planar)", "Portrait Prime"),
    FocalPreset(90, "90mm", "90mm", "旁轴中长焦", "Rangefinder Tele"),
    FocalPreset(105, "105mm", "105mm", "微距/中远摄", "Macro / Short Tele"),
    FocalPreset(135, "135mm", "135mm", "远摄特写 (Sonnar)", "Telephoto Sonnar"),
    FocalPreset(200, "200mm", "200mm", "长焦远摄", "Long Telephoto")
)

/**
 * Focal Lengths choice wrapper
 */
enum class FocalLengthChoice(
    val focalMm: Int,
    val displayLabel: String,
    val digitalZoomFactor: Float
) {
    F14(14, "14mm (Ultra Wide)", 0.6f),
    F21(21, "21mm (Ultra Wide)", 0.8f),
    F24(24, "24mm (Wide)", 1.0f),
    F28(28, "28mm (Street)", 1.15f),
    F35(35, "35mm (Reportage)", 1.45f),
    F40(40, "40mm (Natural)", 1.65f),
    F50(50, "50mm (Standard)", 2.08f),
    F75(75, "75mm (Portrait)", 3.1f),
    F85(85, "85mm (Portrait Prime)", 3.54f),
    F90(90, "90mm (Telephoto)", 3.75f),
    F105(105, "105mm (Macro/Tele)", 4.37f),
    F135(135, "135mm (Long Tele)", 5.62f),
    F200(200, "200mm (Super Tele)", 8.33f);

    companion object {
        fun fromFocal(mm: Int): FocalLengthChoice {
            return entries.minByOrNull { kotlin.math.abs(it.focalMm - mm) } ?: F50
        }
    }
}

/**
 * Film Type classification
 */
enum class FilmType(val labelZh: String, val labelEn: String) {
    BW_NEGATIVE("黑白负片", "B&W Negative"),
    COLOR_NEGATIVE("彩色负片 (C-41)", "Color Negative (C-41)"),
    COLOR_SLIDE("彩色反转片 (E-6)", "Color Slide (E-6)")
}

/**
 * Film stock with Schwarzschild Reciprocity Failure parameters
 */
data class FilmStock(
    val id: String,
    val name: String,
    val brand: String,
    val defaultIso: Int,
    val type: FilmType,
    val schwarzschildP: Double, // P parameter: t_corr = t^(1/p) or formula
    val descriptionZh: String,
    val descriptionEn: String = "",
    val calculateReciprocity: (Double) -> Double = { meteredSec ->
        if (meteredSec <= 1.0) {
            meteredSec
        } else {
            meteredSec.pow(1.0 / schwarzschildP)
        }
    }
) {
    val isBlackAndWhite: Boolean get() = type == FilmType.BW_NEGATIVE
}

/**
 * Comprehensive Preset Film Database with Accurate Reciprocity Failure Curves
 */
object FilmDatabase {
    val allFilms: List<FilmStock> = listOf(
        // Generic defaults
        FilmStock(
            id = "default_color_film",
            name = "Default Color Film (Generic)",
            brand = "Generic",
            defaultIso = 400,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.80,
            descriptionZh = "通用标准彩色负片预设，适用于数据库中未包含的所有彩色胶片",
            descriptionEn = "Standard color negative preset, suitable for unlisted color stocks",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.25) }
        ),
        FilmStock(
            id = "default_bw_film",
            name = "Default B&W Film (Generic)",
            brand = "Generic",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.80,
            descriptionZh = "通用标准黑白负片预设，适用于数据库中未包含的所有黑白胶片",
            descriptionEn = "Standard B&W negative preset, suitable for unlisted black and white stocks",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.25) }
        ),

        // --- KODAK ---
        FilmStock(
            id = "kodak_portra_400",
            name = "Kodak Portra 400",
            brand = "Kodak",
            defaultIso = 400,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.80,
            descriptionZh = "顶级人像彩负，温润肤色还原，过曝宽容度极强",
            descriptionEn = "Industry benchmark portrait color film, exceptional skin tones and overexposure latitude",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.25) }
        ),
        FilmStock(
            id = "kodak_portra_160",
            name = "Kodak Portra 160",
            brand = "Kodak",
            defaultIso = 160,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.80,
            descriptionZh = "细腻自然肤色，低饱和柔和反差，适合日光人像",
            descriptionEn = "Ultra-fine grain and soft contrast, ideal for natural daylight portraits",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.25) }
        ),
        FilmStock(
            id = "kodak_portra_800",
            name = "Kodak Portra 800",
            brand = "Kodak",
            defaultIso = 800,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.78,
            descriptionZh = "暗光高速人像彩负，色彩鲜活，宽容度优秀",
            descriptionEn = "High-speed portrait film for low-light situations, vibrant saturation",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.28) }
        ),
        FilmStock(
            id = "kodak_ektar_100",
            name = "Kodak Ektar 100",
            brand = "Kodak",
            defaultIso = 100,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.78,
            descriptionZh = "世界上最细腻的彩负，高饱和鲜艳色彩，风光利器",
            descriptionEn = "World's finest grain color negative film, vivid saturation for landscapes",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.28) }
        ),
        FilmStock(
            id = "kodak_gold_200",
            name = "Kodak Gold 200",
            brand = "Kodak",
            defaultIso = 200,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.77,
            descriptionZh = "经典暖调日常民用卷，金黄日落色调，性价比极高",
            descriptionEn = "Classic warm consumer film with golden sunset tones and high latitude",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.30) }
        ),
        FilmStock(
            id = "kodak_colorplus_200",
            name = "Kodak ColorPlus 200",
            brand = "Kodak",
            defaultIso = 200,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.77,
            descriptionZh = "经典复古民用彩色胶卷，质朴胶片色调与柔和颗粒",
            descriptionEn = "Vintage nostalgic color palette, reliable everyday film stock",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.30) }
        ),
        FilmStock(
            id = "kodak_ultramax_400",
            name = "Kodak UltraMax 400",
            brand = "Kodak",
            defaultIso = 400,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.77,
            descriptionZh = "高感光日常彩色负片，色彩鲜艳，全天候随拍首选",
            descriptionEn = "Versatile 400 ISO color film with saturated colors and crisp sharpness",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.30) }
        ),
        FilmStock(
            id = "kodak_proimage_100",
            name = "Kodak Pro Image 100",
            brand = "Kodak",
            defaultIso = 100,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.78,
            descriptionZh = "热带耐候人像彩负，自然暖调与良好耐高温特性",
            descriptionEn = "Stable high-temperature portrait stock with natural warm rendering",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.28) }
        ),
        FilmStock(
            id = "kodak_vision3_500t",
            name = "Kodak Vision3 500T (5219)",
            brand = "Kodak",
            defaultIso = 500,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.81,
            descriptionZh = "好莱坞电影胶片，极宽动态范围与夜景电影调色质感",
            descriptionEn = "Iconic motion picture tungsten film with immense dynamic range and cinematic halation",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.24) }
        ),
        FilmStock(
            id = "kodak_vision3_250d",
            name = "Kodak Vision3 250D (5207)",
            brand = "Kodak",
            defaultIso = 250,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.81,
            descriptionZh = "日光型电影胶片，真实中性色彩还原与细腻高光过渡",
            descriptionEn = "Daylight-balanced motion picture stock, natural colors and smooth highlight roll-off",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.24) }
        ),
        FilmStock(
            id = "kodak_trix_400",
            name = "Kodak Tri-X 400 (400TX)",
            brand = "Kodak",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.79,
            descriptionZh = "经典纪实黑白胶片，宽容度极高，高反差颗粒感强",
            descriptionEn = "Legendary photojournalism black and white film with rich contrast and gritty grain",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.26) }
        ),
        FilmStock(
            id = "kodak_tmax_400",
            name = "Kodak T-Max 400 (TMY)",
            brand = "Kodak",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.87,
            descriptionZh = "T颗粒技术黑白胶片，倒易率失效轻微，细腻高锐度",
            descriptionEn = "Advanced T-grain technology with minimal reciprocity failure and fine resolution",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.15) }
        ),
        FilmStock(
            id = "kodak_tmax_100",
            name = "Kodak T-Max 100 (TMX)",
            brand = "Kodak",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.88,
            descriptionZh = "极微细颗粒，极高分辨率，风景与静物摄影首选",
            descriptionEn = "Ultra-high resolution and microscopic grain for architecture and landscape",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.15) }
        ),
        FilmStock(
            id = "kodak_double_x",
            name = "Kodak Double-X (5222)",
            brand = "Kodak",
            defaultIso = 250,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.76,
            descriptionZh = "经典好莱坞黑白电影卷，浓郁银盐影调与经典电影感",
            descriptionEn = "Classic Hollywood B&W motion picture film with rich tonal scale",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.31) }
        ),
        FilmStock(
            id = "kodak_ektachrome_e100",
            name = "Kodak Ektachrome E100",
            brand = "Kodak",
            defaultIso = 100,
            type = FilmType.COLOR_SLIDE,
            schwarzschildP = 0.90,
            descriptionZh = "专业反转片，极细腻颗粒与纯正自然的中性色彩",
            descriptionEn = "Modern daylight color reversal transparency film with micro-fine grain",
            calculateReciprocity = { t ->
                when {
                    t <= 1.0 -> t
                    t <= 10.0 -> t * 1.25 // +1/3 stop
                    t <= 30.0 -> t * 1.41 // +1/2 stop
                    else -> t.pow(1.18)
                }
            }
        ),

        // --- ILFORD ---
        FilmStock(
            id = "ilford_hp5_plus",
            name = "Ilford HP5 Plus 400",
            brand = "Ilford",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.76,
            descriptionZh = "英伦传奇黑白卷，极佳迫冲性能，丰富中灰影调",
            descriptionEn = "Iconic British B&W stock with tremendous push-processing latitude",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.31) }
        ),
        FilmStock(
            id = "ilford_fp4_plus",
            name = "Ilford FP4 Plus 125",
            brand = "Ilford",
            defaultIso = 125,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.79,
            descriptionZh = "中速高锐度黑白卷，细腻影调过渡，室内外通用",
            descriptionEn = "Medium-speed fine grain film with superb sharpness and tonality",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.26) }
        ),
        FilmStock(
            id = "ilford_delta_100",
            name = "Ilford Delta 100",
            brand = "Ilford",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.82,
            descriptionZh = "现代外延晶体黑白胶片，超高解析力与清透影调",
            descriptionEn = "Epitaxial crystal technology delivering razor sharpness and clean tones",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.26) }
        ),
        FilmStock(
            id = "ilford_delta_400",
            name = "Ilford Delta 400",
            brand = "Ilford",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.71,
            descriptionZh = "高速现代晶体黑白卷，极低颗粒与优异阴影细节",
            descriptionEn = "High-speed modern emulsion with fine grain and rich shadow detail",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.41) }
        ),
        FilmStock(
            id = "ilford_delta_3200",
            name = "Ilford Delta 3200",
            brand = "Ilford",
            defaultIso = 3200,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.75,
            descriptionZh = "超高速暗光黑白胶片，极限暗光与舞台摄影利器",
            descriptionEn = "Ultra-high speed black and white film for extreme low light and concert action",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.33) }
        ),
        FilmStock(
            id = "ilford_pan_f",
            name = "Ilford Pan F Plus 50",
            brand = "Ilford",
            defaultIso = 50,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.75,
            descriptionZh = "超低速高对比黑白卷，无颗粒纯净质感，需要充足光线",
            descriptionEn = "Ultra-fine grain slow film with outstanding contrast and detail",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.33) }
        ),
        FilmStock(
            id = "ilford_xp2_super",
            name = "Ilford XP2 Super 400",
            brand = "Ilford",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.76,
            descriptionZh = "C-41工艺染料型黑白胶卷，微粒无噪点，超大曝光宽容度",
            descriptionEn = "Chromogenic C-41 process B&W film with virtually grainless highlights",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.31) }
        ),
        FilmStock(
            id = "ilford_sfx_200",
            name = "Ilford SFX 200",
            brand = "Ilford",
            defaultIso = 200,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.70,
            descriptionZh = "近红外扩展黑白胶片，配合红镜呈现戏剧性暗黑天空与白木效应",
            descriptionEn = "Extended red sensitivity film for dramatic infrared and Wood effect landscapes",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.43) }
        ),
        FilmStock(
            id = "ilford_ortho_plus",
            name = "Ilford Ortho Plus 80",
            brand = "Ilford",
            defaultIso = 80,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.75,
            descriptionZh = "正色黑白胶片，蓝绿光敏感，高反差与独特肖像质感",
            descriptionEn = "Orthochromatic fine grain B&W film sensitive to blue and green light",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.33) }
        ),

        // --- FUJIFILM ---
        FilmStock(
            id = "fuji_acros_ii",
            name = "Fujifilm Neopan Acros 100 II",
            brand = "Fujifilm",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.98,
            descriptionZh = "倒易率性能之王！120秒内完全无需补偿，夜景长曝神卷",
            descriptionEn = "King of long exposure! Zero reciprocity compensation needed up to 120 seconds",
            calculateReciprocity = { t ->
                when {
                    t <= 120.0 -> t // 120s内无失效
                    t <= 1000.0 -> t * 1.414 // +0.5 EV
                    else -> t * 2.0 // +1.0 EV
                }
            }
        ),
        FilmStock(
            id = "fuji_provia_100f",
            name = "Fujifilm Provia 100F (RDPIII)",
            brand = "Fujifilm",
            defaultIso = 100,
            type = FilmType.COLOR_SLIDE,
            schwarzschildP = 0.92,
            descriptionZh = "专业反转片标杆，中性真实色彩，极低颗粒与高倒易率容忍度",
            descriptionEn = "Professional slide standard, true neutral colors and excellent reciprocity up to 128s",
            calculateReciprocity = { t ->
                when {
                    t <= 4.0 -> t
                    t <= 32.0 -> t * 1.25 // +1/3 stop
                    t <= 64.0 -> t * 1.41 // +1/2 stop
                    else -> t * 2.0 // +1 stop
                }
            }
        ),
        FilmStock(
            id = "fuji_velvia_50",
            name = "Fujifilm Velvia 50 (RVP50)",
            brand = "Fujifilm",
            defaultIso = 50,
            type = FilmType.COLOR_SLIDE,
            schwarzschildP = 0.80,
            descriptionZh = "风光圣杯正片，超高色彩饱和度与深邃黑色，极富戏剧性",
            descriptionEn = "Holy grail landscape slide film, unmatched saturation and dramatic contrast",
            calculateReciprocity = { t ->
                when {
                    t <= 1.0 -> t
                    t <= 4.0 -> t * 1.25 // +1/3 stop
                    t <= 16.0 -> t * 1.41 // +1/2 stop
                    t <= 32.0 -> t * 2.0 // +1 stop
                    else -> t.pow(1.35)
                }
            }
        ),
        FilmStock(
            id = "fuji_velvia_100",
            name = "Fujifilm Velvia 100 (RVP100)",
            brand = "Fujifilm",
            defaultIso = 100,
            type = FilmType.COLOR_SLIDE,
            schwarzschildP = 0.82,
            descriptionZh = "高速风光反转片，鲜艳红黄色调，风光摄影经典",
            descriptionEn = "High-saturation daylight slide film with vivid warm rendering",
            calculateReciprocity = { t ->
                when {
                    t <= 1.0 -> t
                    t <= 4.0 -> t * 1.25
                    t <= 16.0 -> t * 1.41
                    t <= 64.0 -> t * 2.0
                    else -> t.pow(1.30)
                }
            }
        ),
        FilmStock(
            id = "fuji_c200",
            name = "Fujifilm Fujicolor 200 (C200)",
            brand = "Fujifilm",
            defaultIso = 200,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.77,
            descriptionZh = "日系清透色彩，青绿暗部调性，经典民用随拍卷",
            descriptionEn = "Clean Japanese color tones with cool greens and crisp contrast",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.30) }
        ),
        FilmStock(
            id = "fuji_superia_400",
            name = "Fujifilm Superia X-TRA 400",
            brand = "Fujifilm",
            defaultIso = 400,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.78,
            descriptionZh = "第4感色层技术，荧光灯下色彩校正优异，高解析度",
            descriptionEn = "4th color layer technology, superior under mixed and fluorescent lighting",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.28) }
        ),
        FilmStock(
            id = "fuji_pro_400h",
            name = "Fujifilm Pro 400H",
            brand = "Fujifilm",
            defaultIso = 400,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.80,
            descriptionZh = "日系空气感人像卷，柔美粉绿色调，婚礼与肖像挚爱",
            descriptionEn = "Soft pastel tones with airy greens and glowing skin highlights",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.25) }
        ),

        // --- FOMA ---
        FilmStock(
            id = "fomapan_400",
            name = "Fomapan 400 Action",
            brand = "Foma",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.60,
            descriptionZh = "复古捷克黑白卷，倒易率失效极为显著，需大幅长曝补偿",
            descriptionEn = "Vintage Czech B&W stock with rapid reciprocity failure curve",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.68) }
        ),
        FilmStock(
            id = "fomapan_200",
            name = "Fomapan 200 Creative",
            brand = "Foma",
            defaultIso = 200,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.62,
            descriptionZh = "六角晶体与传统晶体混合乳剂，极佳清晰度与复古调性",
            descriptionEn = "Unique tabular and cubic crystal blend with high sharpness",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.62) }
        ),
        FilmStock(
            id = "fomapan_100",
            name = "Fomapan 100 Classic",
            brand = "Foma",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.61,
            descriptionZh = "经典欧系黑白卷，复古银盐颗粒，倒易率失效较快",
            descriptionEn = "Traditional European silver halide grain and nostalgic contrast",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.65) }
        ),
        FilmStock(
            id = "foma_retropan_320",
            name = "Foma Retropan 320 Soft",
            brand = "Foma",
            defaultIso = 320,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.63,
            descriptionZh = "低反差复古柔光黑白胶片，专为古董肖像与静物设计",
            descriptionEn = "Soft contrast retro panchromatic film for artistic vintage imagery",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.60) }
        ),

        // --- CINESTILL ---
        FilmStock(
            id = "cinestill_800t",
            name = "CineStill 800T Tungsten",
            brand = "CineStill",
            defaultIso = 800,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.81,
            descriptionZh = "电影胶片改装卷，独特红色光晕，夜景街头氛围之王",
            descriptionEn = "Motion picture tungsten stock with signature red neon halations",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.24) }
        ),
        FilmStock(
            id = "cinestill_400d",
            name = "CineStill 400D Dynamic",
            brand = "CineStill",
            defaultIso = 400,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.81,
            descriptionZh = "日光型电影彩色胶片，柔和自然色调与电影级高光光晕",
            descriptionEn = "Daylight motion picture color negative with wide latitude and subtle halation",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.24) }
        ),
        FilmStock(
            id = "cinestill_50d",
            name = "CineStill 50D Daylight",
            brand = "CineStill",
            defaultIso = 50,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.80,
            descriptionZh = "极低感光度电影日光卷，极致细腻画质与真实色彩",
            descriptionEn = "Ultra-fine grain slow motion picture film for bright daylight",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.25) }
        ),
        FilmStock(
            id = "cinestill_bwxx",
            name = "CineStill BwXX (Double-X)",
            brand = "CineStill",
            defaultIso = 250,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.76,
            descriptionZh = "经典电影黑白负片，浓厚电影光影与细腻颗粒",
            descriptionEn = "Legendary motion picture B&W emulsion for cinema aesthetics",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.31) }
        ),

        // --- ROLLEI ---
        FilmStock(
            id = "rollei_rpx_100",
            name = "Rollei RPX 100",
            brand = "Rollei",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.79,
            descriptionZh = "高分辨率黑白负片，颗粒极细，影调丰富细腻",
            descriptionEn = "Fine-grained traditional B&W film with sharp tonality",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.26) }
        ),
        FilmStock(
            id = "rollei_rpx_400",
            name = "Rollei RPX 400",
            brand = "Rollei",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.77,
            descriptionZh = "高感度通用黑白胶卷，迫冲潜力大，对比强烈",
            descriptionEn = "Versatile high-speed black & white stock with strong push capability",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.30) }
        ),
        FilmStock(
            id = "rollei_retro_80s",
            name = "Rollei Retro 80S",
            brand = "Rollei",
            defaultIso = 80,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.80,
            descriptionZh = "航空胶片基底，超高对比度与近红外感光度，通透锐利",
            descriptionEn = "Aviation-grade polyester base, ultra-contrast with near-IR sensitivity",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.25) }
        ),
        FilmStock(
            id = "rollei_infrared_400",
            name = "Rollei Infrared 400",
            brand = "Rollei",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.75,
            descriptionZh = "真正红外黑白胶卷，配合IR720滤镜拍摄梦幻白木效果",
            descriptionEn = "True infrared film sensitive up to 820nm for surreal Wood effects",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.33) }
        ),

        // --- KENTMERE ---
        FilmStock(
            id = "kentmere_pan_100",
            name = "Kentmere Pan 100",
            brand = "Kentmere",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.79,
            descriptionZh = "Harman出品高性价比黑白卷，细节清晰，宽容度良好",
            descriptionEn = "Produced by Harman Technology, exceptional sharpness and budget value",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.26) }
        ),
        FilmStock(
            id = "kentmere_pan_400",
            name = "Kentmere Pan 400",
            brand = "Kentmere",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.77,
            descriptionZh = "经典高速黑白胶片，颗粒适中，适合纪实与扫街",
            descriptionEn = "Reliable 400 ISO street photography film with classic grain",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.30) }
        ),

        // --- ADOX ---
        FilmStock(
            id = "adox_chs_100_ii",
            name = "Adox CHS 100 II",
            brand = "Adox",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.74,
            descriptionZh = "经典经典配方黑白胶卷，双层乳剂结构，高光过渡极为自然",
            descriptionEn = "Classic German dual-layer emulsion with legendary highlight gradation",
            calculateReciprocity = { t ->
                when {
                    t <= 1.0 -> t
                    t <= 2.0 -> t * 1.5
                    t <= 4.0 -> t * 2.0
                    else -> t.pow(1.35)
                }
            }
        ),
        FilmStock(
            id = "adox_silvermax_100",
            name = "Adox Silvermax 100",
            brand = "Adox",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.79,
            descriptionZh = "超高含银量黑白胶卷，拥有多达14级动态范围与清透影调",
            descriptionEn = "High silver content emulsion offering up to 14 stops of dynamic range",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.26) }
        ),

        // --- LOMOGRAPHY ---
        FilmStock(
            id = "lomo_color_400",
            name = "Lomography Color 400",
            brand = "Lomography",
            defaultIso = 400,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.78,
            descriptionZh = "高饱和色彩与特有颗粒，充满街头艺术感与活力",
            descriptionEn = "Bold vibrant saturation and punchy contrast for creative street style",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.28) }
        ),
        FilmStock(
            id = "lomo_color_800",
            name = "Lomography Color 800",
            brand = "Lomography",
            defaultIso = 800,
            type = FilmType.COLOR_NEGATIVE,
            schwarzschildP = 0.78,
            descriptionZh = "高速复古彩负，夜拍与室内暗光色彩浓烈",
            descriptionEn = "High-speed creative color film with rich tones in twilight and neon",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.28) }
        ),

        // --- BERGGER & FERRANIA & SHANGHAI ---
        FilmStock(
            id = "bergger_pancro_400",
            name = "Bergger Pancro 400",
            brand = "Bergger",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.75,
            descriptionZh = "法国双重银盐乳剂（溴化银与碘化银），艺术级黑白阶调",
            descriptionEn = "French dual silver halide emulsion with extraordinary artistic grayscale",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.33) }
        ),
        FilmStock(
            id = "ferrania_p30",
            name = "Ferrania P30 Alpha",
            brand = "Ferrania",
            defaultIso = 80,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.77,
            descriptionZh = "意大利经典电影黑白配方，极高对比度与浓黑深邃影调",
            descriptionEn = "Italian cinema heritage emulsion with dramatic contrast and deep blacks",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.30) }
        ),
        FilmStock(
            id = "shanghai_gp3_100",
            name = "Shanghai GP3 100 (上海全色)",
            brand = "Shanghai",
            defaultIso = 100,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.71,
            descriptionZh = "经典国产全色黑白胶片，复古银盐颗粒与中式纪实质感",
            descriptionEn = "Classic Chinese panchromatic B&W film with vintage silver grain",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.40) }
        ),
        FilmStock(
            id = "arista_edu_400",
            name = "Arista EDU Ultra 400",
            brand = "Arista",
            defaultIso = 400,
            type = FilmType.BW_NEGATIVE,
            schwarzschildP = 0.60,
            descriptionZh = "经典教学黑白负片，对比鲜明，适合传统暗房冲洗",
            descriptionEn = "Traditional student black & white film with strong contrast",
            calculateReciprocity = { t -> if (t <= 1.0) t else t.pow(1.68) }
        )
    )

    fun getFilmById(id: String): FilmStock {
        return allFilms.find { it.id == id } ?: allFilms.first()
    }
}

/**
 * Monochrome color filter simulations for B&W film
 */
enum class ColorFilterMode(
    val labelZh: String,
    val labelEn: String,
    val code: String,
    val filterFactorStops: Double, // Stop compensation needed when physical filter attached
    val filterColor: Color,
    val descriptionZh: String,
    val descriptionEn: String
) {
    NONE("彩色实景", "Full Color", "Color", 0.0, Color.Transparent, "自然真实全彩取景", "Natural full-color live preview"),
    BW_STANDARD("全色黑白", "B&W Standard", "B&W", 0.0, Color(0xFFE2E8F0), "标准全色黑白灰阶，等效人眼感知", "Panchromatic standard grayscale rendering"),
    BW_RED("红镜 (R25)", "Red Filter (R25)", "Red", +3.0, Color(0xFFEF4444), "压暗蓝天白云强烈反差，消除远景薄雾", "Darkens blue sky for intense clouds and cuts haze"),
    BW_YELLOW("黄镜 (Y8)", "Yellow Filter (Y8)", "Yellow", +1.0, Color(0xFFFACC15), "经典街拍滤镜，自然云彩对比与景深分离", "Classic street filter for balanced cloud separation"),
    BW_ORANGE("橙镜 (O16)", "Orange Filter (O16)", "Orange", +2.0, Color(0xFFFB923C), "柔化人像肤色雀斑，强化建筑与阴影层次", "Smooths skin blemishes, intensifies architecture"),
    BW_GREEN("绿镜 (G11)", "Green Filter (G11)", "Green", +2.0, Color(0xFF22C55E), "提亮绿色树叶草地，平衡日光下人物唇色", "Lightens foliage and balances outdoor skin tones")
}

/**
 * Metering mode
 */
enum class MeteringMode(val labelZh: String, val labelEn: String, val shortName: String) {
    SPOT("单点测光", "Spot", "Spot"),
    MATRIX("全画幅平均", "Matrix", "Matrix"),
    MULTI_SPOT("多点分区对比", "Multi-Spot", "Multi-Spot")
}

/**
 * Priority mode
 */
enum class PriorityMode(val labelZh: String, val labelEn: String, val code: String) {
    APERTURE("光圈优先 (Av)", "Aperture Priority (Av)", "Av"),
    SHUTTER("快门优先 (Tv)", "Shutter Priority (Tv)", "Tv"),
    ISO_LOCK("ISO基准", "ISO Priority", "ISO"),
    FREE_MATCH("自由曝光联动", "EV-Match (Manual)", "EV-Lock")
}

/**
 * Spot metering mark on the viewfinder
 */
data class SpotMarker(
    val id: Int,
    val normX: Float,
    val normY: Float,
    val measuredEv100: Double,
    val assignedZone: ZoneLevel = ZoneLevel.ZONE_V,
    val label: String = "S$id"
)

/**
 * Shot record for logging
 */
data class MeteringLog(
    val id: Long = System.currentTimeMillis(),
    val timestampMs: Long = System.currentTimeMillis(),
    val filmName: String,
    val iso: Int,
    val apertureStr: String,
    val shutterStr: String,
    val compensatedShutterStr: String,
    val ev100: Double,
    val currentEv: Double,
    val targetZone: ZoneLevel,
    val note: String = "",
    val focalLengthMm: Int = 50
)

/**
 * Rangefinder Measurement Engine Mode
 */
enum class RangefinderEngineMode(
    val labelZh: String,
    val labelEn: String,
    val descriptionZh: String,
    val descriptionEn: String
) {
    INCLINOMETER(
        "地面倾角几何测距 (高精度)",
        "Inclinometer Geometry (High Precision)",
        "瞄准物体与地面接触线，利用重力陀螺仪与站姿高度实时三角解算，不受低反差与光线影响",
        "Point at object base on ground. Calculates distance using gravity sensor and eye height trigonometry. 100% immune to low contrast."
    ),
    AF_OPTICAL(
        "相机对焦马达测距 (含标定)",
        "Camera AF Motor (Calibratable)",
        "读取相机音圈马达实际对焦屈光度，支持 1.0m 一键精准标定与低反差边缘自适应",
        "Reads Camera2 AF motor diopters with 1.0m one-tap calibration and multi-zone edge capture."
    ),
    STADIAMETRIC(
        "经典光学视距测距 (人像标尺)",
        "Stadiametric Optical Reticle",
        "经典徕卡/军用光学分划标尺，利用 1.7m 人体或标准参考物在不同焦距下的片门成像比例精准测距",
        "Classic optical stadiametric reticle based on human height proportions and 35mm equivalent focal length."
    )
}
