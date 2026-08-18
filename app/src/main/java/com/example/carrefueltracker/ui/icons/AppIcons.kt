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
 */

@Suppress("CheckReturnValue")
public val arrow_back: ImageVector
    get() {
        if (_arrow_back != null) {
            return _arrow_back!!
        }
        _arrow_back =
            ImageVector.Builder(
                name = "arrow_back",
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
                        moveTo(7.83f, 13f)
                        lineToRelative(5.6f, 5.6f)
                        lineTo(12f, 20f)
                        lineTo(4f, 12f)
                        lineTo(12f, 4f)
                        lineToRelative(1.43f, 1.4f)
                        lineTo(7.83f, 11f)
                        horizontalLineTo(20f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(7.83f)
                        close()
                    }
                }
                .build()
        return _arrow_back!!
    }

private var _arrow_back: ImageVector? = null

@Suppress("CheckReturnValue")
public val check: ImageVector
    get() {
        if (_check != null) {
            return _check!!
        }
        _check =
            ImageVector.Builder(
                name = "check",
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
                        moveTo(9.55f, 18f)
                        lineTo(3.85f, 12.3f)
                        lineTo(5.28f, 10.88f)
                        lineToRelative(4.28f, 4.28f)
                        lineTo(18.73f, 5.97f)
                        lineTo(20.15f, 7.4f)
                        lineTo(9.55f, 18f)
                        close()
                    }
                }
                .build()
        return _check!!
    }

private var _check: ImageVector? = null

@Suppress("CheckReturnValue")
public val overview: ImageVector
    get() {
        if (_overview != null) {
            return _overview!!
        }
        _overview =
            ImageVector.Builder(
                name = "overview",
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
                        moveTo(19.68f, 20.38f)
                        lineToRelative(0.7f, -0.7f)
                        lineTo(18.5f, 17.8f)
                        verticalLineTo(15f)
                        horizontalLineToRelative(-1f)
                        verticalLineToRelative(3.2f)
                        lineToRelative(2.18f, 2.18f)
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
                        verticalLineToRelative(6.7f)
                        quadTo(20.53f, 11.48f, 20.03f, 11.31f)
                        reflectiveQuadTo(19f, 11.08f)
                        verticalLineTo(5f)
                        horizontalLineTo(5f)
                        verticalLineTo(19f)
                        horizontalLineToRelative(6.05f)
                        quadToRelative(0.07f, 0.55f, 0.24f, 1.05f)
                        reflectiveQuadTo(11.68f, 21f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 18f)
                        quadToRelative(0f, 0.27f, 0f, 0.51f)
                        reflectiveQuadTo(5f, 19f)
                        verticalLineTo(5f)
                        verticalLineToRelative(6.07f)
                        quadTo(5f, 11.02f, 5f, 11.01f)
                        reflectiveQuadTo(5f, 11f)
                        reflectiveQuadToRelative(0f, 2.05f)
                        reflectiveQuadTo(5f, 18f)
                        close()
                        moveTo(7f, 17f)
                        horizontalLineToRelative(4.08f)
                        quadToRelative(0.07f, -0.52f, 0.24f, -1.03f)
                        quadTo(11.48f, 15.48f, 11.68f, 15f)
                        horizontalLineTo(7f)
                        verticalLineToRelative(2f)
                        close()
                        moveTo(7f, 13f)
                        horizontalLineToRelative(6.1f)
                        quadToRelative(0.8f, -0.75f, 1.79f, -1.25f)
                        reflectiveQuadTo(17f, 11.08f)
                        verticalLineTo(11f)
                        horizontalLineTo(7f)
                        verticalLineToRelative(2f)
                        close()
                        moveTo(7f, 9f)
                        horizontalLineTo(17f)
                        verticalLineTo(7f)
                        horizontalLineTo(7f)
                        verticalLineTo(9f)
                        close()
                        moveTo(18f, 23f)
                        quadToRelative(-2.07f, 0f, -3.54f, -1.46f)
                        reflectiveQuadTo(13f, 18f)
                        reflectiveQuadToRelative(1.46f, -3.54f)
                        reflectiveQuadTo(18f, 13f)
                        reflectiveQuadToRelative(3.54f, 1.46f)
                        reflectiveQuadTo(23f, 18f)
                        reflectiveQuadToRelative(-1.46f, 3.54f)
                        reflectiveQuadTo(18f, 23f)
                        close()
                    }
                }
                .build()
        return _overview!!
    }

private var _overview: ImageVector? = null

@Suppress("CheckReturnValue")
public val add: ImageVector
    get() {
        if (_add != null) {
            return _add!!
        }
        _add =
            ImageVector.Builder(
                name = "add",
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
                        moveTo(11f, 13f)
                        horizontalLineTo(5f)
                        verticalLineTo(11f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(5f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(6f)
                        horizontalLineToRelative(6f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(13f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(11f)
                        verticalLineTo(13f)
                        close()
                    }
                }
                .build()
        return _add!!
    }

private var _add: ImageVector? = null

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