package dev.jekis.canvacompass.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jekis.canvacompass.presentation.CompassViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(
    viewModel: CompassViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modifier = Modifier

    CompassScreenContent(
        modifier = modifier, azimuth = uiState.azimuth
    )
}

@Composable
fun CompassScreenContent(
    modifier: Modifier = Modifier, azimuth: Float
) {
    var continuousAzimuth by remember { mutableFloatStateOf(azimuth) }
    LaunchedEffect(azimuth) {
        val delta = (azimuth - continuousAzimuth) % 360f
        val shortestDelta = when {
            delta > 180f -> delta - 360f
            delta < -180f -> delta + 360f
            else -> delta
        }
        continuousAzimuth += shortestDelta
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = continuousAzimuth,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CompassRotation"
    )
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val textMeasurer = rememberTextMeasurer()
        val rotatingTextMeasurer = rememberTextMeasurer()

        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val padding = 16f
            val radius = (size.minDimension / 2) - padding

            val azimuthInRadians = Math.toRadians(-animatedAzimuth.toDouble())

            val northX = (centerX + radius * sin(azimuthInRadians)).toFloat()
            val northY = (centerY - radius * cos(azimuthInRadians)).toFloat()

            /* Outer contour */
            drawCircle(
                color = Color.Gray,
                radius = radius,
                center = Offset(x = centerX, y = centerY),
                style = Stroke(width = 4F)
            )

            /* North label */
            drawCircle(
                color = Color.Red, radius = 20f, center = Offset(northX, northY)
            )

            /* Outer scale (ticks) */
            val roundScaleLineLength = 20f
            val roundScaleLineWidth = 2f
            for (angle in 0 until 360) {
                val angleInRadians = Math.toRadians(angle.toDouble())
                val start = Offset(
                    x = centerX + (radius * cos(angleInRadians)).toFloat(),
                    y = centerY - (radius * sin(angleInRadians)).toFloat()
                )
                val end = Offset(
                    x = centerX + ((radius - roundScaleLineLength) * cos(angleInRadians)).toFloat(),
                    y = centerY - ((radius - roundScaleLineLength) * sin(angleInRadians)).toFloat()
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = roundScaleLineWidth,
                    start = start,
                    end = end,
                )
            }

            /* Segmented black and white ring */
            var isEven = true
            val segmentedArcWidth = 20f
            val arcRadius = radius - roundScaleLineLength - (segmentedArcWidth / 2f)
            for (angle in 360 downTo 10 step 15) {
                val segmentStartAngle = angle.toFloat()
                val segmentSweepAngle = -15f

                drawArc(
                    color = if (isEven) Color.Black else Color.White,
                    startAngle = segmentStartAngle,
                    sweepAngle = segmentSweepAngle,
                    useCenter = false,
                    style = Stroke(width = segmentedArcWidth),
                    topLeft = Offset(centerX - arcRadius, centerY - arcRadius),
                    size = Size(arcRadius * 2f, arcRadius * 2f)
                )
                isEven = !isEven
            }

            /* Inner rings and background */
            val innerGraySegmentWidth = 15f
            val innerGrayArcRadius =
                radius - roundScaleLineLength - segmentedArcWidth - (innerGraySegmentWidth / 2f)
            val innerStartAngle = 0f
            val innerSweepAngle = 360f

            drawArc(
                color = Color.LightGray,
                startAngle = innerStartAngle,
                sweepAngle = innerSweepAngle,
                useCenter = false,
                style = Stroke(width = innerGraySegmentWidth),
                topLeft = Offset(centerX - innerGrayArcRadius, centerY - innerGrayArcRadius),
                size = Size(innerGrayArcRadius * 2f, innerGrayArcRadius * 2f)
            )
            val thirtyDegreeSegmentRadius = innerGrayArcRadius - (innerGraySegmentWidth / 2)
            val innerBlackArcRadius = thirtyDegreeSegmentRadius


            drawArc(
                color = Color.Black,
                startAngle = innerStartAngle,
                sweepAngle = innerSweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - innerBlackArcRadius, centerY - innerBlackArcRadius),
                size = Size(innerBlackArcRadius * 2f, innerBlackArcRadius * 2f)
            )

            /* Numerical degree values */
            val textPadding = 15f

            for (angle in 0 until 360 step 30) {

                val textVal = angle.toString()

                val visualAngle = 90f - angle.toFloat()
                val angleInRadians = Math.toRadians(visualAngle.toDouble())

                val start = Offset(
                    x = centerX + (thirtyDegreeSegmentRadius * cos(angleInRadians)).toFloat(),
                    y = centerY - (thirtyDegreeSegmentRadius * sin(angleInRadians)).toFloat()
                )
                val end = Offset(
                    x = centerX + ((thirtyDegreeSegmentRadius - roundScaleLineLength) * cos(
                        angleInRadians
                    )).toFloat(),
                    y = centerY - ((thirtyDegreeSegmentRadius - roundScaleLineLength) * sin(
                        angleInRadians
                    )).toFloat()
                )

                drawLine(
                    color = Color.White,
                    strokeWidth = roundScaleLineWidth,
                    start = start,
                    end = end,
                )
                val textLayoutResult = textMeasurer.measure(
                    text = textVal, style = TextStyle(
                        color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                )

                val textWidth = textLayoutResult.size.width
                val textHeight = textLayoutResult.size.height

                val textRadius = thirtyDegreeSegmentRadius - roundScaleLineLength - textPadding
                val textX = centerX + (textRadius * cos(angleInRadians)).toFloat()
                val textY = centerY - (textRadius * sin(angleInRadians)).toFloat()

                val rotationDegrees = angle.toFloat()

                withTransform({
                    translate(left = textX, top = textY)
                    rotate(degrees = rotationDegrees, pivot = Offset.Zero)
                }) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(x = -textWidth / 2f, y = -textHeight / 2f)
                    )
                }
            }

            /* Gray divider */
            val grayDividerWidth = 30f
            val dummyTextLayout = textMeasurer.measure(
                text = "360", style = TextStyle(
                    color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold
                )
            )
            val textHeight = dummyTextLayout.size.height.toFloat()
            val textRadius = thirtyDegreeSegmentRadius - roundScaleLineLength - textPadding
            val textInnerBoundary = textRadius - (textHeight / 2f) - 4f
            val grayDividerRadius = textInnerBoundary - (grayDividerWidth / 2f)
            drawArc(
                color = Color.LightGray,
                startAngle = innerStartAngle,
                sweepAngle = innerSweepAngle,
                useCenter = false,
                style = Stroke(width = grayDividerWidth),
                topLeft = Offset(centerX - grayDividerRadius, centerY - grayDividerRadius),
                size = Size(grayDividerRadius * 2f, grayDividerRadius * 2f)

            )

            val thinGrayLineRadius = grayDividerRadius - grayDividerWidth
            val thinGrayLineWidth = 2f
            drawArc(
                color = Color.LightGray,
                startAngle = innerStartAngle,
                sweepAngle = innerSweepAngle,
                useCenter = false,
                style = Stroke(width = thinGrayLineWidth),
                topLeft = Offset(
                    x = centerX - thinGrayLineRadius, y = centerY - thinGrayLineRadius
                ),
                size = Size(thinGrayLineRadius * 2f, thinGrayLineRadius * 2f)
            )
            rotatingPart(
                rotatingTextMeasurer = rotatingTextMeasurer,
                canvasScope = this,
                previousElementRadius = thinGrayLineRadius,
                azimuthAngle = animatedAzimuth
            )
        }
    }
}

/* The rotating part of compass */
private fun rotatingPart(
    rotatingTextMeasurer: TextMeasurer,
    canvasScope: DrawScope,
    previousElementRadius: Float,
    azimuthAngle: Float
) {
    val lettersMap = mapOf<Double, String>(
        0.0 to "N", 90.0 to "E", 180.0 to "S", 270.0 to "W"
    )

    val interLettersMap = mapOf(45.0 to "NE", 135.0 to "SE", 225.0 to "SW", 315.0 to "NW")

    val minorLettersMap = mapOf(
        22.5 to "NNE",
        67.5 to "ENE",
        112.5 to "ESE",
        157.5 to "SSE",
        202.5 to "SSW",
        247.5 to "WSW",
        292.5 to "WNW",
        337.5 to "NNW"
    )

    val textPadding = 15f
    val textSpace = 10f
    val textRadius = previousElementRadius - textPadding - textSpace

    val baseArrowRadius = textRadius - textPadding - 15f
    val innerWhiteRingRadius = baseArrowRadius * 0.62f
    val innerGrayRingRadius = innerWhiteRingRadius - 35f

    /* Inner decorative rings */
    canvasScope.drawArc(
        color = Color.White,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = true,
        style = Stroke(width = 2f),
        topLeft = Offset(
            canvasScope.center.x - innerWhiteRingRadius, canvasScope.center.y - innerWhiteRingRadius
        ),
        size = Size(innerWhiteRingRadius * 2, innerWhiteRingRadius * 2)
    )


    canvasScope.drawArc(
        color = Color.Gray, startAngle = 0f, sweepAngle = 360f, useCenter = true, topLeft = Offset(
            canvasScope.center.x - innerGrayRingRadius, canvasScope.center.y - innerGrayRingRadius
        ), size = Size(innerGrayRingRadius * 2, innerGrayRingRadius * 2)
    )

    /* Letter designations (NE, SE, SW, NW) */
    val innerLetterRadius = innerWhiteRingRadius + 25f
    interLettersMap.forEach { degree, text ->
        val textLayoutResult = rotatingTextMeasurer.measure(
            text = text, style = TextStyle(
                color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Normal
            )
        )

        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        val angleInRadians = Math.toRadians(-azimuthAngle.toDouble() + degree)
        val textX =
            (canvasScope.size.width / 2) + (innerLetterRadius * sin(angleInRadians)).toFloat()
        val textY =
            (canvasScope.size.height / 2) - (innerLetterRadius * cos(angleInRadians)).toFloat()

        canvasScope.withTransform(
            {
                translate(left = textX, top = textY)
                rotate(degrees = -azimuthAngle + degree.toFloat(), pivot = Offset.Zero)
            }) {
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x = -textWidth / 2f, y = -textHeight / 2f)
            )
        }
    }

    /* Minor letter designations (NNE, ENE...) */
    val minorLetterRadius = innerGrayRingRadius + 15f
    minorLettersMap.forEach { degree, text ->
        val textLayoutResult = rotatingTextMeasurer.measure(
            text = text,
            style = TextStyle(
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Normal
            )
        )

        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        val angleInRadians = Math.toRadians(-azimuthAngle.toDouble() + degree)
        val textX =
            (canvasScope.size.width / 2) + (minorLetterRadius * sin(angleInRadians)).toFloat()
        val textY =
            (canvasScope.size.height / 2) - (minorLetterRadius * cos(angleInRadians)).toFloat()

        canvasScope.withTransform(
            {
                translate(left = textX, top = textY)
                rotate(degrees = -azimuthAngle + degree.toFloat(), pivot = Offset.Zero)
            }) {
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x = -textWidth / 2f, y = -textHeight / 2f)
            )
        }
    }


    /* Auxiliary arrows */
    val intermediateDegrees = listOf(45.0, 135.0, 225.0, 315.0)
    val interHalfBase = 24f

    intermediateDegrees.forEach { interDegree ->
        val interAngle = Math.toRadians(-azimuthAngle.toDouble() + interDegree)
        val interTipX =
            (canvasScope.size.width / 2) + (innerWhiteRingRadius * sin(interAngle)).toFloat()
        val interTipY =
            (canvasScope.size.height / 2) - (innerWhiteRingRadius * cos(interAngle)).toFloat()

        val idx = (interHalfBase * cos(interAngle)).toFloat()
        val idy = (interHalfBase * sin(interAngle)).toFloat()

        val interLeftBottomX = (canvasScope.size.width / 2) - idx
        val interLeftBottomY = (canvasScope.size.height / 2) - idy
        val interRightBottomX = (canvasScope.size.width / 2) + idx
        val interRightBottomY = (canvasScope.size.height / 2) + idy

        val leftPath = Path().apply {
            moveTo(canvasScope.center.x, canvasScope.center.y)
            lineTo(interLeftBottomX, interLeftBottomY)
            lineTo(interTipX, interTipY)
            close()
        }
        canvasScope.drawPath(path = leftPath, color = Color.DarkGray)

        val rightPath = Path().apply {
            moveTo(canvasScope.center.x, canvasScope.center.y)
            lineTo(interRightBottomX, interRightBottomY)
            lineTo(interTipX, interTipY)
            close()
        }
        canvasScope.drawPath(path = rightPath, color = Color.LightGray)
    }

    /* Main arrows and cardinal points (N, E, S, W) */
    var color = Color.Red

    lettersMap.forEach { (degree, text) ->
        val textLayoutResult = rotatingTextMeasurer.measure(
            text = text, style = TextStyle(
                color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold
            )
        )

        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        val angleInRadians = Math.toRadians(-azimuthAngle.toDouble() + degree)
        val textX = (canvasScope.size.width / 2) + (textRadius * sin(angleInRadians)).toFloat()
        val textY = (canvasScope.size.height / 2) - (textRadius * cos(angleInRadians)).toFloat()

        canvasScope.withTransform(
            {
                translate(left = textX, top = textY)
                rotate(degrees = -azimuthAngle, pivot = Offset.Zero)
            }) {
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x = -textWidth / 2f, y = -textHeight / 2f)
            )
        }
        color = Color.Gray

        val arrowSpace = 15f
        val arrowRadius = textRadius - textPadding - arrowSpace
        val lineAngle = Math.toRadians(-azimuthAngle.toDouble() + degree)
        val arrowBaseLength = 96f
        val halfBase = arrowBaseLength / 2f

        val dx = (halfBase * cos(lineAngle)).toFloat()
        val dy = (halfBase * sin(lineAngle)).toFloat()

        val arrowLeftBottomX = (canvasScope.size.width / 2) - dx
        val arrowLeftBottomY = (canvasScope.size.height / 2) - dy

        val arrowRightBottomX = (canvasScope.size.width / 2) + dx
        val arrowRightBottomY = (canvasScope.size.height / 2) + dy

        val arrowTopX = (canvasScope.size.width / 2) + (arrowRadius * sin(lineAngle)).toFloat()
        val arrowTopY = (canvasScope.size.height / 2) - (arrowRadius * cos(lineAngle)).toFloat()


        val arrowLeftPath = Path().apply {
            moveTo(
                x = canvasScope.center.x, y = canvasScope.center.y
            )

            lineTo(x = arrowLeftBottomX, y = arrowLeftBottomY)
            lineTo(x = arrowTopX, y = arrowTopY)
            close()
        }

        canvasScope.drawPath(
            path = arrowLeftPath, color = Color.LightGray
        )

        val arrowRightPath = Path().apply {
            moveTo(
                x = canvasScope.center.x, y = canvasScope.center.y
            )

            lineTo(x = arrowRightBottomX, y = arrowRightBottomY)
            lineTo(x = arrowTopX, y = arrowTopY)
            close()
        }

        canvasScope.drawPath(
            path = arrowRightPath,
            color = Color.DarkGray,
        )

        canvasScope.drawCircle(
            color = Color.LightGray,
            radius = 64f
        )

        canvasScope.drawCircle(
            color = Color.DarkGray,
            radius = 64f,
            style = Stroke(8f)
        )

        canvasScope.drawCircle(
            color = Color.DarkGray,
            radius = 24f,
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewCompassScreen() {
    val previewAzimuth = 15F
    CompassScreenContent(
        modifier = Modifier, azimuth = previewAzimuth
    )
}