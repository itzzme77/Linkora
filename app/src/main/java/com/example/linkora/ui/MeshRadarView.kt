package com.example.linkora.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Radar View with concentric range rings, rotating sweep line, and dynamic peer blips.
 */
class MeshRadarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#33006C50")
    }

    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val centerNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#006C50")
    }

    private val peerBlipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#10B981")
    }

    private val peerHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#4410B981")
    }

    private var sweepAngle = 0f
    private var peerCount = 0
    private var animator: ValueAnimator? = null

    init {
        startAnimation()
    }

    fun setPeerCount(count: Int) {
        peerCount = count
        invalidate()
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                sweepAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = minOf(cx, cy) - 24f

        if (maxRadius <= 0) return

        // 1. Draw Concentric Circles
        for (i in 1..4) {
            val r = maxRadius * (i / 4f)
            canvas.drawCircle(cx, cy, r, ringPaint)
        }

        // 2. Draw Crosshairs
        canvas.drawLine(cx - maxRadius, cy, cx + maxRadius, cy, ringPaint)
        canvas.drawLine(cx, cy - maxRadius, cx, cy + maxRadius, ringPaint)

        // 3. Draw Radar Sweep Shader
        val shader = SweepGradient(
            cx, cy,
            intArrayOf(Color.TRANSPARENT, Color.parseColor("#44006C50"), Color.parseColor("#9910B981")),
            floatArrayOf(0f, 0.7f, 1f)
        )
        val matrix = Matrix()
        matrix.setRotate(sweepAngle, cx, cy)
        shader.setLocalMatrix(matrix)
        sweepPaint.shader = shader
        canvas.drawCircle(cx, cy, maxRadius, sweepPaint)

        // 4. Center Local Node
        canvas.drawCircle(cx, cy, 14f, centerNodePaint)

        // 5. Draw Dynamic Peer Blips around the radar
        if (peerCount > 0) {
            val angleStep = (2 * Math.PI / peerCount)
            for (i in 0 until peerCount) {
                val blipAngle = (i * angleStep) + Math.toRadians(45.0)
                val blipDist = maxRadius * (0.45f + (0.35f * (i % 2)))
                val bx = (cx + blipDist * cos(blipAngle)).toFloat()
                val by = (cy + blipDist * sin(blipAngle)).toFloat()

                canvas.drawCircle(bx, by, 18f, peerHaloPaint)
                canvas.drawCircle(bx, by, 9f, peerBlipPaint)
            }
        }
    }
}
