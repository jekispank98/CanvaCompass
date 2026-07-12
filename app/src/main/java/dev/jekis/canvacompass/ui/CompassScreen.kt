package dev.jekis.canvacompass.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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

    val animatedAzimuth by animateFloatAsState(
        targetValue = uiState.azimuth,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CompassRotation"
    )
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2

            val azimuthInRadians = Math.toRadians(-animatedAzimuth.toDouble())

            val northX = (centerX + radius * sin(azimuthInRadians)).toFloat()
            val northY = (centerY - radius * cos(azimuthInRadians)).toFloat()


            drawCircle(
                color = Color.Gray,
                radius = radius,
                center = Offset(x = centerX, y = centerY),
                style = Stroke(width = 4F)
            )

            drawCircle(
                color = Color.Red,
                radius = 20f,
                center = Offset(northX, northY)
            )
        }
    }



}