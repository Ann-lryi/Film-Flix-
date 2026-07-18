package com.nguonc.stream.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * FilmFlix Premium 3.0 Icon Set
 *
 * Custom SVG-style vectors with a consistent visual language:
 *  - Stroke width: 1.9f for outline variants (slightly heavier than Material's 2.0)
 *  - Stroke caps: Round (premium, no sharp end-kinks)
 *  - Stroke joins: Round
 *  - Optical size: 24dp viewport, drawn on a 20dp "core" grid for visual balance
 *
 * Replaces default Material icons (PlayArrow / Star / Bolt / etc.) with versions
 * that match the brand's "cinematic" feel.
 */
object FilmFlixIcons {

    // ─────────── Filled icons ───────────

    /** Solid play triangle with subtle taper — feels more "cinematic" than the Material default. */
    val PlayFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "PlayFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.0f, 5.14f)
                cubicTo(7.4f, 4.78f, 6.6f, 5.21f, 6.6f, 5.91f)
                lineTo(6.6f, 18.09f)
                cubicTo(6.6f, 18.79f, 7.4f, 19.22f, 8.0f, 18.86f)
                lineTo(17.62f, 12.77f)
                cubicTo(18.16f, 12.43f, 18.16f, 11.57f, 17.62f, 11.23f)
                close()
            }
        }.build()
    }

    /** 5-point star with inner highlight ring — for ratings. */
    val StarFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "StarFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 2.6f)
                lineTo(14.6f, 8.62f)
                lineTo(21.1f, 9.27f)
                cubicTo(21.65f, 9.32f, 21.88f, 9.99f, 21.48f, 10.36f)
                lineTo(16.66f, 14.84f)
                lineTo(18.09f, 21.21f)
                cubicTo(18.22f, 21.75f, 17.65f, 22.17f, 17.17f, 21.89f)
                lineTo(12f, 18.66f)
                lineTo(6.83f, 21.89f)
                cubicTo(6.35f, 22.17f, 5.78f, 21.75f, 5.91f, 21.21f)
                lineTo(7.34f, 14.84f)
                lineTo(2.52f, 10.36f)
                cubicTo(2.12f, 9.99f, 2.35f, 9.32f, 2.9f, 9.27f)
                lineTo(9.4f, 8.62f)
                close()
            }
        }.build()
    }

    /** Heart — solid, for favorites. */
    val HeartFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "HeartFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 21.0f)
                lineTo(10.55f, 19.69f)
                cubicTo(5.4f, 15.0f, 2.0f, 12.0f, 2.0f, 8.5f)
                cubicTo(2.0f, 5.42f, 4.42f, 3.0f, 7.0f, 3.0f)
                cubicTo(8.6f, 3.0f, 10.2f, 3.79f, 12f, 5.27f)
                cubicTo(13.8f, 3.79f, 15.4f, 3.0f, 17f, 3.0f)
                cubicTo(19.58f, 3.0f, 22f, 5.42f, 22f, 8.5f)
                cubicTo(22f, 12.0f, 18.6f, 15.0f, 13.45f, 19.69f)
                close()
            }
        }.build()
    }

    /** Heart — outline, for "not yet favorited" state. */
    val HeartOutline: ImageVector by lazy {
        outlineIcon("HeartOutline") {
            moveTo(12f, 21.0f)
            lineTo(10.55f, 19.69f)
            cubicTo(5.4f, 15.0f, 2.0f, 12.0f, 2.0f, 8.5f)
            cubicTo(2.0f, 5.42f, 4.42f, 3.0f, 7.0f, 3.0f)
            cubicTo(8.6f, 3.0f, 10.2f, 3.79f, 12f, 5.27f)
            cubicTo(13.8f, 3.79f, 15.4f, 3.0f, 17f, 3.0f)
            cubicTo(19.58f, 3.0f, 22f, 5.42f, 22f, 8.5f)
            cubicTo(22f, 12.0f, 18.6f, 15.0f, 13.45f, 19.69f)
            close()
        }
    }

    /** Lightning bolt — solid, for "trending" badges. */
    val BoltFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "BoltFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(13.5f, 2.0f)
                lineTo(4.0f, 13.5f)
                lineTo(11.0f, 13.5f)
                lineTo(10.5f, 22.0f)
                lineTo(20.0f, 10.5f)
                lineTo(13.0f, 10.5f)
                close()
            }
        }.build()
    }

    /** Flame — solid, for "hot/trending". */
    val FlameFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "FlameFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12.5f, 2.0f)
                curveTo(13.4f, 5.4f, 11.8f, 7.7f, 10.2f, 9.4f)
                curveTo(8.6f, 11.1f, 7.0f, 12.6f, 7.0f, 15.5f)
                curveTo(7.0f, 18.81f, 9.24f, 21.5f, 12.0f, 21.5f)
                curveTo(14.76f, 21.5f, 17.0f, 18.81f, 17.0f, 15.5f)
                curveTo(17.0f, 13.6f, 16.1f, 12.2f, 15.2f, 10.9f)
                curveTo(14.9f, 12.4f, 14.0f, 13.4f, 12.9f, 13.8f)
                curveTo(13.4f, 11.4f, 13.2f, 8.2f, 12.5f, 2.0f)
                close()
            }
        }.build()
    }

    // ─────────── Outline icons (consistent 1.9 stroke) ───────────

    /** Search magnifier — rounded, with thick handle. */
    val SearchOutline: ImageVector by lazy {
        outlineIcon("SearchOutline") {
            moveTo(11.0f, 4.0f)
            arcTo(7.0f, 7.0f, 0f, false, true, 18.0f, 11.0f)
            arcTo(7.0f, 7.0f, 0f, false, true, 11.0f, 18.0f)
            arcTo(7.0f, 7.0f, 0f, false, true, 4.0f, 11.0f)
            arcTo(7.0f, 7.0f, 0f, false, true, 11.0f, 4.0f)
            close()
            moveTo(20.0f, 20.0f)
            lineTo(15.65f, 15.65f)
        }
    }

    /** Home — soft roof + open door. */
    val HomeOutline: ImageVector by lazy {
        outlineIcon("HomeOutline") {
            moveTo(3.0f, 10.5f)
            lineTo(12.0f, 3.0f)
            lineTo(21.0f, 10.5f)
            lineTo(21.0f, 20.0f)
            arcTo(1.0f, 1.0f, 0f, false, true, 20.0f, 21.0f)
            lineTo(15.0f, 21.0f)
            lineTo(15.0f, 14.0f)
            lineTo(9.0f, 14.0f)
            lineTo(9.0f, 21.0f)
            lineTo(4.0f, 21.0f)
            arcTo(1.0f, 1.0f, 0f, false, true, 3.0f, 20.0f)
            close()
        }
    }

    /** Bookmark — soft cutout at the bottom-left (cinema ticket style). */
    val BookmarkOutline: ImageVector by lazy {
        outlineIcon("BookmarkOutline") {
            moveTo(6.0f, 3.0f)
            lineTo(18.0f, 3.0f)
            arcTo(1.0f, 1.0f, 0f, false, true, 19.0f, 4.0f)
            lineTo(19.0f, 21.0f)
            lineTo(12.0f, 17.0f)
            lineTo(5.0f, 21.0f)
            lineTo(5.0f, 4.0f)
            arcTo(1.0f, 1.0f, 0f, false, true, 6.0f, 3.0f)
            close()
        }
    }

    /** Compass — for "Browse". */
    val CompassOutline: ImageVector by lazy {
        outlineIcon("CompassOutline") {
            moveTo(12.0f, 3.0f)
            arcTo(9.0f, 9.0f, 0f, true, true, 3.0f, 12.0f)
            arcTo(9.0f, 9.0f, 0f, true, true, 12.0f, 3.0f)
            close()
            moveTo(15.5f, 8.5f)
            lineTo(13.5f, 13.5f)
            lineTo(8.5f, 15.5f)
            lineTo(10.5f, 10.5f)
            close()
        }
    }

    /** Bell — notifications. */
    val BellOutline: ImageVector by lazy {
        outlineIcon("BellOutline") {
            moveTo(6.0f, 9.0f)
            arcTo(6.0f, 6.0f, 0f, false, true, 18.0f, 9.0f)
            curveTo(18.0f, 12.0f, 19.0f, 14.0f, 20.0f, 15.0f)
            lineTo(20.0f, 17.0f)
            lineTo(4.0f, 17.0f)
            lineTo(4.0f, 15.0f)
            curveTo(5.0f, 14.0f, 6.0f, 12.0f, 6.0f, 9.0f)
            close()
            moveTo(10.0f, 20.0f)
            arcTo(2.0f, 2.0f, 0f, false, false, 14.0f, 20.0f)
        }
    }

    /** Chevron-left — back arrow. */
    val ChevronLeft: ImageVector by lazy {
        outlineIcon("ChevronLeft") {
            moveTo(15.0f, 5.0f)
            lineTo(8.0f, 12.0f)
            lineTo(15.0f, 19.0f)
        }
    }

    /** Chevron-right — see-more arrow. */
    val ChevronRight: ImageVector by lazy {
        outlineIcon("ChevronRight") {
            moveTo(9.0f, 5.0f)
            lineTo(16.0f, 12.0f)
            lineTo(9.0f, 19.0f)
        }
    }

    /** Plus — for "add to list". */
    val PlusOutline: ImageVector by lazy {
        outlineIcon("PlusOutline") {
            moveTo(12.0f, 5.0f)
            lineTo(12.0f, 19.0f)
            moveTo(5.0f, 12.0f)
            lineTo(19.0f, 12.0f)
        }
    }

    /** Share — iOS-style arrow-out-of-box. */
    val ShareOutline: ImageVector by lazy {
        outlineIcon("ShareOutline") {
            moveTo(12.0f, 3.0f)
            lineTo(12.0f, 15.0f)
            moveTo(8.0f, 7.0f)
            lineTo(12.0f, 3.0f)
            lineTo(16.0f, 7.0f)
            moveTo(5.0f, 13.0f)
            lineTo(5.0f, 19.0f)
            arcTo(1.0f, 1.0f, 0f, false, false, 6.0f, 20.0f)
            lineTo(18.0f, 20.0f)
            arcTo(1.0f, 1.0f, 0f, false, false, 19.0f, 19.0f)
            lineTo(19.0f, 13.0f)
        }
    }

    /** Filter — sliders icon. */
    val FilterOutline: ImageVector by lazy {
        outlineIcon("FilterOutline") {
            moveTo(4.0f, 7.0f)
            lineTo(11.0f, 7.0f)
            moveTo(17.0f, 7.0f)
            lineTo(20.0f, 7.0f)
            moveTo(4.0f, 17.0f)
            lineTo(7.0f, 17.0f)
            moveTo(13.0f, 17.0f)
            lineTo(20.0f, 17.0f)
            moveTo(14.0f, 7.0f)
            arcTo(3.0f, 3.0f, 0f, true, true, 8.0f, 7.0f)
            arcTo(3.0f, 3.0f, 0f, true, true, 14.0f, 7.0f)
            close()
            moveTo(17.0f, 17.0f)
            arcTo(3.0f, 3.0f, 0f, true, true, 11.0f, 17.0f)
            arcTo(3.0f, 3.0f, 0f, true, true, 17.0f, 17.0f)
            close()
        }
    }

    /** Clock — history. */
    val ClockOutline: ImageVector by lazy {
        outlineIcon("ClockOutline") {
            moveTo(12.0f, 3.0f)
            arcTo(9.0f, 9.0f, 0f, true, true, 3.0f, 12.0f)
            arcTo(9.0f, 9.0f, 0f, true, true, 12.0f, 3.0f)
            close()
            moveTo(12.0f, 7.0f)
            lineTo(12.0f, 12.0f)
            lineTo(16.0f, 14.0f)
        }
    }

    /** Trash — for delete actions. */
    val TrashOutline: ImageVector by lazy {
        outlineIcon("TrashOutline") {
            moveTo(4.0f, 7.0f)
            lineTo(20.0f, 7.0f)
            moveTo(9.0f, 7.0f)
            lineTo(9.0f, 4.0f)
            lineTo(15.0f, 4.0f)
            lineTo(15.0f, 7.0f)
            moveTo(6.0f, 7.0f)
            lineTo(7.0f, 20.0f)
            arcTo(1.0f, 1.0f, 0f, false, false, 8.0f, 21.0f)
            lineTo(16.0f, 21.0f)
            arcTo(1.0f, 1.0f, 0f, false, false, 17.0f, 20.0f)
            lineTo(18.0f, 7.0f)
            moveTo(10.0f, 11.0f)
            lineTo(10.0f, 17.0f)
            moveTo(14.0f, 11.0f)
            lineTo(14.0f, 17.0f)
        }
    }

    /** Cloud-off — error state. */
    val CloudOffOutline: ImageVector by lazy {
        outlineIcon("CloudOffOutline") {
            moveTo(3.0f, 3.0f)
            lineTo(21.0f, 21.0f)
            moveTo(5.5f, 8.0f)
            arcTo(5.5f, 5.5f, 0f, false, false, 9.5f, 17.0f)
            lineTo(18.0f, 17.0f)
            arcTo(2.0f, 2.0f, 0f, false, false, 18.0f, 13.0f)
            arcTo(5.0f, 5.0f, 0f, false, false, 17.0f, 8.0f)
            arcTo(5.0f, 5.0f, 0f, false, false, 13.0f, 5.5f)
            arcTo(5.0f, 5.0f, 0f, false, false, 10.5f, 6.5f)
        }
    }

    /** Folder-open — empty state. */
    val FolderOpenOutline: ImageVector by lazy {
        outlineIcon("FolderOpenOutline") {
            moveTo(4.0f, 6.0f)
            arcTo(1.0f, 1.0f, 0f, false, true, 5.0f, 5.0f)
            lineTo(9.0f, 5.0f)
            lineTo(11.0f, 7.0f)
            lineTo(19.0f, 7.0f)
            arcTo(1.0f, 1.0f, 0f, false, true, 20.0f, 8.0f)
            lineTo(20.0f, 11.0f)
            moveTo(3.0f, 9.0f)
            lineTo(5.0f, 19.0f)
            arcTo(1.0f, 1.0f, 0f, false, false, 6.0f, 20.0f)
            lineTo(18.0f, 20.0f)
            arcTo(1.0f, 1.0f, 0f, false, false, 19.0f, 19.0f)
            lineTo(21.0f, 11.0f)
            arcTo(0.5f, 0.5f, 0f, false, false, 20.5f, 10.5f)
            lineTo(5.0f, 10.5f)
            arcTo(1.0f, 1.0f, 0f, false, false, 4.0f, 11.5f)
            close()
        }
    }

    /** Globe — for country. */
    val GlobeOutline: ImageVector by lazy {
        outlineIcon("GlobeOutline") {
            moveTo(12.0f, 3.0f)
            arcTo(9.0f, 9.0f, 0f, true, true, 3.0f, 12.0f)
            arcTo(9.0f, 9.0f, 0f, true, true, 12.0f, 3.0f)
            close()
            moveTo(3.5f, 9.0f)
            lineTo(20.5f, 9.0f)
            moveTo(3.5f, 15.0f)
            lineTo(20.5f, 15.0f)
            moveTo(12.0f, 3.0f)
            arcTo(15.0f, 9.0f, 0f, false, true, 12.0f, 21.0f)
            arcTo(15.0f, 9.0f, 0f, false, true, 12.0f, 3.0f)
            close()
        }
    }

    /** Tag — for category. */
    val TagOutline: ImageVector by lazy {
        outlineIcon("TagOutline") {
            moveTo(3.0f, 12.0f)
            lineTo(12.0f, 3.0f)
            lineTo(21.0f, 3.0f)
            lineTo(21.0f, 12.0f)
            lineTo(12.0f, 21.0f)
            close()
            moveTo(7.5f, 7.5f)
            arcTo(0.5f, 0.5f, 0f, true, true, 7.5f, 8.5f)
            arcTo(0.5f, 0.5f, 0f, true, true, 7.5f, 7.5f)
            close()
        }
    }

    /** Clear (X) — for clearing search input. */
    val ClearOutline: ImageVector by lazy {
        outlineIcon("ClearOutline") {
            moveTo(6.0f, 6.0f)
            lineTo(18.0f, 18.0f)
            moveTo(18.0f, 6.0f)
            lineTo(6.0f, 18.0f)
        }
    }

    /** Volume / audio language — for "lang" chip. */
    val LanguageOutline: ImageVector by lazy {
        outlineIcon("LanguageOutline") {
            moveTo(11.0f, 5.0f)
            lineTo(11.0f, 19.0f)
            moveTo(7.0f, 8.0f)
            lineTo(11.0f, 5.0f)
            lineTo(15.0f, 8.0f)
            moveTo(8.0f, 13.0f)
            lineTo(16.0f, 13.0f)
            arcTo(2.0f, 2.0f, 0f, false, true, 18.0f, 15.0f)
            lineTo(18.0f, 17.0f)
            arcTo(2.0f, 2.0f, 0f, false, false, 20.0f, 19.0f)
            lineTo(20.0f, 19.0f)
            arcTo(2.0f, 2.0f, 0f, false, false, 22.0f, 17.0f)
            lineTo(22.0f, 16.0f)
            arcTo(4.0f, 4.0f, 0f, false, false, 18.0f, 12.0f)
        }
    }

    /** Quality / resolution — diamond gem for 4K/HDR badges. */
    val DiamondOutline: ImageVector by lazy {
        outlineIcon("DiamondOutline") {
            moveTo(6.0f, 3.0f)
            lineTo(18.0f, 3.0f)
            lineTo(22.0f, 9.0f)
            lineTo(12.0f, 21.0f)
            lineTo(2.0f, 9.0f)
            close()
            moveTo(2.0f, 9.0f)
            lineTo(22.0f, 9.0f)
            moveTo(9.0f, 3.0f)
            lineTo(7.0f, 9.0f)
            lineTo(12.0f, 21.0f)
            lineTo(17.0f, 9.0f)
            lineTo(15.0f, 3.0f)
        }
    }
}

// ─────────── Helpers ───────────

/**
 * Convenience builder for outline-style icons.
 * Strokes are 1.9dp wide with round caps/joins — a slightly softer feel than Material's 2.0.
 */
private inline fun outlineIcon(
    name: String,
    pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBuilder
    )
}.build()
