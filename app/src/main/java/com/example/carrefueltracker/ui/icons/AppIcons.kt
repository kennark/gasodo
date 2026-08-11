package com.example.carrefueltracker.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icons for use with this app.
 * todo: Replace XML icons with these
 */

@Suppress("CheckReturnValue")
public val error: ImageVector
    get() {
        if (_error != null) {
            return _error!!
        }
        _error =
            ImageVector.Builder(
                name = "error",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(12.71f, 16.71f)
                        quadTo(13f, 16.43f, 13f, 16f)
                        reflectiveQuadTo(12.71f, 15.29f)
                        reflectiveQuadTo(12f, 15f)
                        reflectiveQuadToRelative(-0.71f, 0.29f)
                        reflectiveQuadTo(11f, 16f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(12f, 17f)
                        reflectiveQuadToRelative(0.71f, -0.29f)
                        close()
                        moveTo(11f, 13f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(7f)
                        horizontalLineTo(11f)
                        verticalLineToRelative(6f)
                        close()
                        moveToRelative(1f, 9f)
                        quadTo(9.93f, 22f, 8.1f, 21.21f)
                        quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                        quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                        reflectiveQuadTo(2f, 12f)
                        quadTo(2f, 9.92f, 2.79f, 8.1f)
                        quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                        quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                        quadTo(9.93f, 2f, 12f, 2f)
                        reflectiveQuadToRelative(3.9f, 0.79f)
                        reflectiveQuadToRelative(3.17f, 2.14f)
                        quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                        quadTo(22f, 9.92f, 22f, 12f)
                        reflectiveQuadToRelative(-0.79f, 3.9f)
                        reflectiveQuadToRelative(-2.14f, 3.17f)
                        quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                        reflectiveQuadTo(12f, 22f)
                        close()
                        moveToRelative(0f, -2f)
                        quadToRelative(3.35f, 0f, 5.68f, -2.32f)
                        reflectiveQuadTo(20f, 12f)
                        reflectiveQuadTo(17.68f, 6.32f)
                        reflectiveQuadTo(12f, 4f)
                        reflectiveQuadTo(6.33f, 6.32f)
                        reflectiveQuadTo(4f, 12f)
                        reflectiveQuadToRelative(2.33f, 5.68f)
                        reflectiveQuadTo(12f, 20f)
                        close()
                        moveToRelative(0f, -8f)
                        close()
                    }
                }
                .build()
        return _error!!
    }

private var _error: ImageVector? = null

@Suppress("CheckReturnValue")
val check_circle: ImageVector
    get() {
        if (_check_circle != null) {
            return _check_circle!!
        }
        _check_circle =
            ImageVector.Builder(
                name = "check_circle",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(10.6f, 16.6f)
                        lineTo(17.65f, 9.55f)
                        lineToRelative(-1.4f, -1.4f)
                        lineTo(10.6f, 13.8f)
                        lineTo(7.75f, 10.95f)
                        lineToRelative(-1.4f, 1.4f)
                        lineTo(10.6f, 16.6f)
                        close()
                        moveTo(12f, 22f)
                        quadTo(9.93f, 22f, 8.1f, 21.21f)
                        quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                        quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                        reflectiveQuadTo(2f, 12f)
                        quadTo(2f, 9.92f, 2.79f, 8.1f)
                        quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                        quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                        quadTo(9.93f, 2f, 12f, 2f)
                        reflectiveQuadToRelative(3.9f, 0.79f)
                        reflectiveQuadToRelative(3.17f, 2.14f)
                        quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                        quadTo(22f, 9.92f, 22f, 12f)
                        reflectiveQuadToRelative(-0.79f, 3.9f)
                        reflectiveQuadToRelative(-2.14f, 3.17f)
                        quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                        reflectiveQuadTo(12f, 22f)
                        close()
                    }
                }
                .build()
        return _check_circle!!
    }

private var _check_circle: ImageVector? = null

@Suppress("CheckReturnValue")
val local_gas_station: ImageVector
    get() {
        if (_local_gas_station != null) {
            return _local_gas_station!!
        }
        _local_gas_station =
            ImageVector.Builder(
                name = "local_gas_station",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(4f, 21f)
                        verticalLineTo(5f)
                        quadTo(4f, 4.17f, 4.59f, 3.59f)
                        reflectiveQuadTo(6f, 3f)
                        horizontalLineToRelative(6f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(14f, 5f)
                        verticalLineToRelative(7f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(17f, 13.18f, 17f, 14f)
                        verticalLineToRelative(4.5f)
                        quadToRelative(0f, 0.43f, 0.29f, 0.71f)
                        reflectiveQuadTo(18f, 19.5f)
                        reflectiveQuadToRelative(0.71f, -0.29f)
                        quadTo(19f, 18.93f, 19f, 18.5f)
                        verticalLineTo(11.3f)
                        quadToRelative(-0.22f, 0.13f, -0.47f, 0.16f)
                        reflectiveQuadTo(18f, 11.5f)
                        quadToRelative(-1.05f, 0f, -1.77f, -0.73f)
                        reflectiveQuadTo(15.5f, 9f)
                        quadToRelative(0f, -0.8f, 0.44f, -1.44f)
                        reflectiveQuadTo(17.1f, 6.65f)
                        lineTo(15f, 4.55f)
                        lineTo(16.05f, 3.5f)
                        lineToRelative(3.7f, 3.6f)
                        quadToRelative(0.38f, 0.38f, 0.56f, 0.88f)
                        quadTo(20.5f, 8.48f, 20.5f, 9f)
                        verticalLineToRelative(9.5f)
                        quadToRelative(0f, 1.05f, -0.72f, 1.77f)
                        reflectiveQuadTo(18f, 21f)
                        reflectiveQuadTo(16.23f, 20.27f)
                        reflectiveQuadTo(15.5f, 18.5f)
                        verticalLineToRelative(-5f)
                        horizontalLineTo(14f)
                        verticalLineTo(21f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(6f, 10f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(5f)
                        horizontalLineTo(6f)
                        verticalLineToRelative(5f)
                        close()
                        moveToRelative(12f, 0f)
                        quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                        reflectiveQuadTo(19f, 9f)
                        quadTo(19f, 8.57f, 18.71f, 8.29f)
                        reflectiveQuadTo(18f, 8f)
                        reflectiveQuadTo(17.29f, 8.29f)
                        reflectiveQuadTo(17f, 9f)
                        quadToRelative(0f, 0.42f, 0.29f, 0.71f)
                        reflectiveQuadTo(18f, 10f)
                        close()
                        moveTo(6f, 19f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(12f)
                        horizontalLineTo(6f)
                        verticalLineToRelative(7f)
                        close()
                        moveToRelative(6f, 0f)
                        horizontalLineTo(6f)
                        horizontalLineToRelative(6f)
                        close()
                    }
                }
                .build()
        return _local_gas_station!!
    }

private var _local_gas_station: ImageVector? = null

@Suppress("CheckReturnValue")
val expand_circle_down: ImageVector
    get() {
        if (_expand_circle_down != null) {
            return _expand_circle_down!!
        }
        _expand_circle_down =
            ImageVector.Builder(
                name = "expand_circle_down",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(12f, 15.5f)
                        lineTo(16.5f, 11f)
                        lineTo(15.08f, 9.6f)
                        lineTo(12f, 12.68f)
                        lineTo(8.93f, 9.6f)
                        lineTo(7.5f, 11f)
                        lineTo(12f, 15.5f)
                        close()
                        moveTo(12f, 22f)
                        quadTo(9.93f, 22f, 8.1f, 21.21f)
                        quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                        quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                        reflectiveQuadTo(2f, 12f)
                        quadTo(2f, 9.92f, 2.79f, 8.1f)
                        quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                        quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                        quadTo(9.93f, 2f, 12f, 2f)
                        reflectiveQuadToRelative(3.9f, 0.79f)
                        reflectiveQuadToRelative(3.17f, 2.14f)
                        quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                        quadTo(22f, 9.92f, 22f, 12f)
                        reflectiveQuadToRelative(-0.79f, 3.9f)
                        reflectiveQuadToRelative(-2.14f, 3.17f)
                        quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                        reflectiveQuadTo(12f, 22f)
                        close()
                        moveToRelative(0f, -2f)
                        quadToRelative(3.35f, 0f, 5.68f, -2.32f)
                        reflectiveQuadTo(20f, 12f)
                        reflectiveQuadTo(17.68f, 6.32f)
                        reflectiveQuadTo(12f, 4f)
                        reflectiveQuadTo(6.33f, 6.32f)
                        reflectiveQuadTo(4f, 12f)
                        reflectiveQuadToRelative(2.33f, 5.68f)
                        reflectiveQuadTo(12f, 20f)
                        close()
                        moveToRelative(0f, -8f)
                        close()
                    }
                }
                .build()
        return _expand_circle_down!!
    }

private var _expand_circle_down: ImageVector? = null

@Suppress("CheckReturnValue")
val calendar_today: ImageVector
    get() {
        if (_calendar_today != null) {
            return _calendar_today!!
        }
        _calendar_today =
            ImageVector.Builder(
                name = "calendar_today",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(5f, 22f)
                        quadTo(4.18f, 22f, 3.59f, 21.41f)
                        reflectiveQuadTo(3f, 20f)
                        verticalLineTo(6f)
                        quadTo(3f, 5.18f, 3.59f, 4.59f)
                        reflectiveQuadTo(5f, 4f)
                        horizontalLineTo(6f)
                        verticalLineTo(2f)
                        horizontalLineTo(8f)
                        verticalLineTo(4f)
                        horizontalLineToRelative(8f)
                        verticalLineTo(2f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(4f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(21f, 5.18f, 21f, 6f)
                        verticalLineTo(20f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 22f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 20f)
                        horizontalLineTo(19f)
                        verticalLineTo(10f)
                        horizontalLineTo(5f)
                        verticalLineTo(20f)
                        close()
                        moveTo(5f, 8f)
                        horizontalLineTo(19f)
                        verticalLineTo(6f)
                        horizontalLineTo(5f)
                        verticalLineTo(8f)
                        close()
                        moveTo(5f, 8f)
                        verticalLineTo(6f)
                        verticalLineTo(8f)
                        close()
                    }
                }
                .build()
        return _calendar_today!!
    }

private var _calendar_today: ImageVector? = null

@Suppress("CheckReturnValue")
val check_box: ImageVector
    get() {
        if (_check_box != null) {
            return _check_box!!
        }
        _check_box =
            ImageVector.Builder(
                name = "check_box",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(10.6f, 16.2f)
                        lineTo(17.65f, 9.15f)
                        lineToRelative(-1.4f, -1.4f)
                        lineTo(10.6f, 13.4f)
                        lineTo(7.75f, 10.55f)
                        lineToRelative(-1.4f, 1.4f)
                        lineTo(10.6f, 16.2f)
                        close()
                        moveTo(5f, 21f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        verticalLineTo(5f)
                        quadTo(3f, 4.17f, 3.59f, 3.59f)
                        reflectiveQuadTo(5f, 3f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 5f)
                        verticalLineTo(19f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 21f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 19f)
                        horizontalLineTo(19f)
                        verticalLineTo(5f)
                        horizontalLineTo(5f)
                        verticalLineTo(19f)
                        close()
                        moveTo(5f, 5f)
                        verticalLineTo(19f)
                        verticalLineTo(5f)
                        close()
                    }
                }
                .build()
        return _check_box!!
    }

private var _check_box: ImageVector? = null

@Suppress("CheckReturnValue")
val check_box_outline_blank: ImageVector
    get() {
        if (_check_box_outline_blank != null) {
            return _check_box_outline_blank!!
        }
        _check_box_outline_blank =
            ImageVector.Builder(
                name = "check_box_outline_blank",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(5f, 21f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        verticalLineTo(5f)
                        quadTo(3f, 4.17f, 3.59f, 3.59f)
                        reflectiveQuadTo(5f, 3f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 5f)
                        verticalLineTo(19f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 21f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 19f)
                        horizontalLineTo(19f)
                        verticalLineTo(5f)
                        horizontalLineTo(5f)
                        verticalLineTo(19f)
                        close()
                    }
                }
                .build()
        return _check_box_outline_blank!!
    }

private var _check_box_outline_blank: ImageVector? = null