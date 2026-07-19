package dev.jekis.canvacompass.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.withTranslation
import kotlin.math.cos
import kotlin.math.sin

class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var animatedAzimuth: Float = 0f
    private var continuousAzimuth: Float = 0f
    private var isInitialized = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rectF = RectF()

    private var animator: ValueAnimator? = null

    fun setAzimuth(newAzimuth: Float) {
        if (!isInitialized) {
            continuousAzimuth = newAzimuth
            animatedAzimuth = newAzimuth
            isInitialized = true
            invalidate()
            return
        }
        val delta = (newAzimuth - continuousAzimuth) % 360f
        val shortestDelta = when {
            delta > 180f -> delta - 360f
            delta < -180f -> delta + 360f
            else -> delta
        }
        continuousAzimuth += shortestDelta

        animator?.cancel()
        animator = ValueAnimator.ofFloat(animatedAzimuth, continuousAzimuth).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedAzimuth = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val padding = 16f * resources.displayMetrics.density
        val radius = (Math.min(width, height) / 2f) - padding

        if (radius <= 0) return

        val azimuthInRadians = Math.toRadians(-animatedAzimuth.toDouble())

        val northX = (centerX + radius * sin(azimuthInRadians)).toFloat()
        val northY = (centerY - radius * cos(azimuthInRadians)).toFloat()

        /* Outer contour */
        paint.reset()
        paint.isAntiAlias = true
        paint.color = Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(centerX, centerY, radius, paint)

        /* North label */
        paint.style = Paint.Style.FILL
        paint.color = Color.RED
        canvas.drawCircle(northX, northY, 20f, paint)

        /* Outer scale (ticks) */
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        val roundScaleLineLength = 20f
        for (angle in 0 until 360) {
            val angleInRadians = Math.toRadians(angle.toDouble())
            val startX = centerX + (radius * cos(angleInRadians)).toFloat()
            val startY = centerY - (radius * sin(angleInRadians)).toFloat()
            val endX = centerX + ((radius - roundScaleLineLength) * cos(angleInRadians)).toFloat()
            val endY = centerY - ((radius - roundScaleLineLength) * sin(angleInRadians)).toFloat()
            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        /* Segmented black and white ring */
        val segmentedArcWidth = 20f
        val arcRadius = radius - roundScaleLineLength - (segmentedArcWidth / 2f)
        rectF.set(centerX - arcRadius, centerY - arcRadius, centerX + arcRadius, centerY + arcRadius)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = segmentedArcWidth
        var isEven = true
        for (angle in 360 downTo 10 step 15) {
            paint.color = if (isEven) Color.BLACK else Color.WHITE
            canvas.drawArc(rectF, angle.toFloat(), -15f, false, paint)
            isEven = !isEven
        }

        /* Inner rings and background */
        val innerGraySegmentWidth = 15f
        val innerGrayArcRadius = radius - roundScaleLineLength - segmentedArcWidth - (innerGraySegmentWidth / 2f)
        rectF.set(centerX - innerGrayArcRadius, centerY - innerGrayArcRadius, centerX + innerGrayArcRadius, centerY + innerGrayArcRadius)
        paint.color = Color.LTGRAY
        paint.strokeWidth = innerGraySegmentWidth
        canvas.drawArc(rectF, 0f, 360f, false, paint)

        val thirtyDegreeSegmentRadius = innerGrayArcRadius - (innerGraySegmentWidth / 2)
        val innerBlackArcRadius = thirtyDegreeSegmentRadius
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, innerBlackArcRadius, paint)

        /* Numerical degree values */
        paint.color = Color.WHITE
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textPadding = 15f
        val roundScaleLineWidth = 2f

        for (angle in 0 until 360 step 30) {
            val textVal = angle.toString()
            val visualAngle = 90f - angle.toFloat()
            val angleInRadians = Math.toRadians(visualAngle.toDouble())

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = roundScaleLineWidth
            paint.color = Color.WHITE
            val startX = centerX + (thirtyDegreeSegmentRadius * cos(angleInRadians)).toFloat()
            val startY = centerY - (thirtyDegreeSegmentRadius * sin(angleInRadians)).toFloat()
            val endX = centerX + ((thirtyDegreeSegmentRadius - roundScaleLineLength) * cos(angleInRadians)).toFloat()
            val endY = centerY - ((thirtyDegreeSegmentRadius - roundScaleLineLength) * sin(angleInRadians)).toFloat()
            canvas.drawLine(startX, startY, endX, endY, paint)

            val textRadius = thirtyDegreeSegmentRadius - roundScaleLineLength - textPadding
            val textX = centerX + (textRadius * cos(angleInRadians)).toFloat()
            val textY = centerY - (textRadius * sin(angleInRadians)).toFloat()

            paint.style = Paint.Style.FILL
            val textWidth = paint.measureText(textVal)
            val textHeightOffset = (paint.descent() + paint.ascent()) / 2f

            canvas.withTranslation(textX, textY) {
                rotate(angle.toFloat())
                drawText(textVal, -textWidth / 2f, -textHeightOffset, paint)
            }
        }

        /* Gray divider */
        val grayDividerWidth = 30f
        val textHeightApprox = paint.textSize
        val textRadius = thirtyDegreeSegmentRadius - roundScaleLineLength - textPadding
        val textInnerBoundary = textRadius - (textHeightApprox / 2f) - 4f
        val grayDividerRadius = textInnerBoundary - (grayDividerWidth / 2f)
        rectF.set(centerX - grayDividerRadius, centerY - grayDividerRadius, centerX + grayDividerRadius, centerY + grayDividerRadius)
        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = grayDividerWidth
        canvas.drawArc(rectF, 0f, 360f, false, paint)

        val thinGrayLineRadius = grayDividerRadius - grayDividerWidth
        paint.strokeWidth = 2f
        canvas.drawCircle(centerX, centerY, thinGrayLineRadius, paint)

        drawRotatingPart(canvas, centerX, centerY, thinGrayLineRadius, animatedAzimuth)
    }

    private fun drawRotatingPart(canvas: Canvas, centerX: Float, centerY: Float, previousElementRadius: Float, azimuthAngle: Float) {
        val lettersMap = mapOf(0.0 to "N", 90.0 to "E", 180.0 to "S", 270.0 to "W")
        val interLettersMap = mapOf(45.0 to "NE", 135.0 to "SE", 225.0 to "SW", 315.0 to "NW")
        val minorLettersMap = mapOf(
            22.5 to "NNE", 67.5 to "ENE", 112.5 to "ESE", 157.5 to "SSE",
            202.5 to "SSW", 247.5 to "WSW", 292.5 to "WNW", 337.5 to "NNW"
        )

        val textPadding = 15f
        val textSpace = 10f
        val textRadius = previousElementRadius - textPadding - textSpace

        val baseArrowRadius = textRadius - textPadding - 15f
        val innerWhiteRingRadius = baseArrowRadius * 0.62f
        val innerGrayRingRadius = innerWhiteRingRadius - 35f

        /* Inner decorative rings */
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        canvas.drawCircle(centerX, centerY, innerWhiteRingRadius, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.GRAY
        canvas.drawCircle(centerX, centerY, innerGrayRingRadius, paint)

        /* Letter designations (NE, SE, SW, NW) */
        paint.color = Color.GRAY
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val innerLetterRadius = innerWhiteRingRadius + 25f
        interLettersMap.forEach { (degree, text) ->
            val angleInRadians = Math.toRadians(-azimuthAngle.toDouble() + degree)
            val textX = centerX + (innerLetterRadius * sin(angleInRadians)).toFloat()
            val textY = centerY - (innerLetterRadius * cos(angleInRadians)).toFloat()

            val textWidth = paint.measureText(text)
            val textHeightOffset = (paint.descent() + paint.ascent()) / 2f

            canvas.withTranslation(textX, textY) {
                rotate(-azimuthAngle + degree.toFloat())
                drawText(text, -textWidth / 2f, -textHeightOffset, paint)
            }
        }

        /* Minor letter designations (NNE, ENE...) */
        paint.color = Color.WHITE
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8f, resources.displayMetrics)
        val minorLetterRadius = innerGrayRingRadius + 15f
        minorLettersMap.forEach { (degree, text) ->
            val angleInRadians = Math.toRadians(-azimuthAngle.toDouble() + degree)
            val textX = centerX + (minorLetterRadius * sin(angleInRadians)).toFloat()
            val textY = centerY - (minorLetterRadius * cos(angleInRadians)).toFloat()

            val textWidth = paint.measureText(text)
            val textHeightOffset = (paint.descent() + paint.ascent()) / 2f

            canvas.withTranslation(textX, textY) {
                rotate(-azimuthAngle + degree.toFloat())
                drawText(text, -textWidth / 2f, -textHeightOffset, paint)
            }
        }

        /* Auxiliary arrows */
        val intermediateDegrees = listOf(45.0, 135.0, 225.0, 315.0)
        val interHalfBase = 24f
        intermediateDegrees.forEach { interDegree ->
            val interAngle = Math.toRadians(-azimuthAngle.toDouble() + interDegree)
            val interTipX = centerX + (innerWhiteRingRadius * sin(interAngle)).toFloat()
            val interTipY = centerY - (innerWhiteRingRadius * cos(interAngle)).toFloat()

            val idx = (interHalfBase * cos(interAngle)).toFloat()
            val idy = (interHalfBase * sin(interAngle)).toFloat()

            val interLeftBottomX = centerX - idx
            val interLeftBottomY = centerY - idy
            val interRightBottomX = centerX + idx
            val interRightBottomY = centerY + idy

            path.reset()
            path.moveTo(centerX, centerY)
            path.lineTo(interLeftBottomX, interLeftBottomY)
            path.lineTo(interTipX, interTipY)
            path.close()
            paint.color = Color.DKGRAY
            canvas.drawPath(path, paint)

            path.reset()
            path.moveTo(centerX, centerY)
            path.lineTo(interRightBottomX, interRightBottomY)
            path.lineTo(interTipX, interTipY)
            path.close()
            paint.color = Color.LTGRAY
            canvas.drawPath(path, paint)
        }

        /* Main arrows and cardinal points (N, E, S, W) */
        var textColor = Color.RED
        lettersMap.forEach { (degree, text) ->
            paint.color = textColor
            paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val angleInRadians = Math.toRadians(-azimuthAngle.toDouble() + degree)
            val textX = centerX + (textRadius * sin(angleInRadians)).toFloat()
            val textY = centerY - (textRadius * cos(angleInRadians)).toFloat()

            val textWidth = paint.measureText(text)
            val textHeightOffset = (paint.descent() + paint.ascent()) / 2f

            canvas.withTranslation(textX, textY) {
                rotate(-azimuthAngle)
                drawText(text, -textWidth / 2f, -textHeightOffset, paint)
            }
            textColor = Color.GRAY

            val arrowSpace = 15f
            val arrowRadius = textRadius - textPadding - arrowSpace
            val lineAngle = Math.toRadians(-azimuthAngle.toDouble() + degree)
            val arrowBaseLength = 96f
            val halfBase = arrowBaseLength / 2f

            val dx = (halfBase * cos(lineAngle)).toFloat()
            val dy = (halfBase * sin(lineAngle)).toFloat()

            val arrowLeftBottomX = centerX - dx
            val arrowLeftBottomY = centerY - dy
            val arrowRightBottomX = centerX + dx
            val arrowRightBottomY = centerY + dy
            val arrowTopX = centerX + (arrowRadius * sin(lineAngle)).toFloat()
            val arrowTopY = centerY - (arrowRadius * cos(lineAngle)).toFloat()

            path.reset()
            path.moveTo(centerX, centerY)
            path.lineTo(arrowLeftBottomX, arrowLeftBottomY)
            path.lineTo(arrowTopX, arrowTopY)
            path.close()
            paint.color = Color.LTGRAY
            canvas.drawPath(path, paint)

            path.reset()
            path.moveTo(centerX, centerY)
            path.lineTo(arrowRightBottomX, arrowRightBottomY)
            path.lineTo(arrowTopX, arrowTopY)
            path.close()
            paint.color = Color.DKGRAY
            canvas.drawPath(path, paint)
        }

        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        canvas.drawCircle(centerX, centerY, 64f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.DKGRAY
        paint.strokeWidth = 8f
        canvas.drawCircle(centerX, centerY, 64f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.DKGRAY
        canvas.drawCircle(centerX, centerY, 24f, paint)
    }
}
