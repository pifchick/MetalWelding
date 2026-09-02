package com.example.weldcalc

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class RCView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var widthMm: Float = 0f
    private var heightMm: Float = 0f
    private var structureType: StructureType = StructureType.RECTANGLE
    private var angle: Float = 42f
    private var stepCount: Int = 10

    private val linePaint = Paint().apply {
        color = Color.parseColor("#90CAF9")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val thinLinePaint = Paint().apply {
        color = Color.parseColor("#90CAF9")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.parseColor("#1AFFFFFF")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dimPaint = Paint().apply {
        color = Color.parseColor("#60FFFFFF")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val dimTextPaint = Paint().apply {
        color = Color.parseColor("#B0BEC5")
        textSize = 16f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = Color.parseColor("#78909C")
        textSize = 14f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#90CAF9")
        textSize = 13f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val fillProfilePaint = Paint().apply {
        color = Color.parseColor("#30FFFFFF")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val engDimPaint = Paint().apply {
        color = Color.parseColor("#FFD54F")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val engDimTextPaint = Paint().apply {
        color = Color.parseColor("#FFD54F")
        textSize = 18f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    fun setDimensions(w: Float, h: Float, type: StructureType, angleDeg: Float = 42f, steps: Int = 10) {
        widthMm = w
        heightMm = h
        structureType = type
        angle = angleDeg
        stepCount = steps
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (widthMm == 0f || heightMm == 0f) {
            canvas.drawText("Нажмите «Рассчитать»", width / 2f, height / 2f, labelPaint)
            return
        }

        if (structureType == StructureType.LADDER) {
            drawLadderProfile(canvas)
        } else {
            drawStandardViews(canvas)
        }
    }

    private fun drawStandardViews(canvas: Canvas) {
        val totalW = width.toFloat()
        val totalH = height.toFloat()

        val halfW = totalW / 2f
        val topRowH = totalH * 0.55f
        val botRowH = totalH * 0.45f
        val topPadding = 28f
        val sidePadding = 10f
        val gap = 6f

        val availTopW = halfW - sidePadding * 2 - gap / 2
        val availTopH = topRowH - topPadding - 20f
        val availBotW = totalW - sidePadding * 2
        val availBotH = botRowH - topPadding - 20f

        val scaleTop = min(availTopW / widthMm, availTopH / heightMm)
        val rectW = widthMm * scaleTop
        val rectH = heightMm * scaleTop

        drawFrontView(canvas, sidePadding, halfW - gap / 2, topPadding, availTopH, rectW, rectH)
        drawSideView(canvas, halfW + gap / 2, totalW - sidePadding, topPadding, availTopH, rectW, rectH)

        val botScale = min(availBotW / widthMm, availBotH / heightMm)
        val botRectW = widthMm * botScale
        val botRectH = heightMm * botScale

        drawTopView(canvas, sidePadding, totalW - sidePadding, topRowH + topPadding, availBotH, botRectW, botRectH)
    }

    private fun drawLadderProfile(canvas: Canvas) {
        val totalW = width.toFloat()
        val totalH = height.toFloat()
        val pad = 50f

        val angleRad = Math.toRadians(angle.toDouble())
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()
        val tanA = tan(angleRad).toFloat()

        val run = heightMm / tanA
        val rise = heightMm

        val availW = totalW - pad * 2
        val availH = totalH - pad * 2

        val scaleX = availW / (run * 1.3f)
        val scaleY = availH / (rise * 1.2f)
        val scale = min(scaleX, scaleY)

        val sRun = run * scale
        val sRise = rise * scale

        val x0 = pad + 40f
        val y0 = totalH - pad - 30f
        val x1 = x0 + sRun
        val y1 = y0 - sRise

        val strGap = 18f

        canvas.drawLine(x0, y0, x1, y1, linePaint)
        canvas.drawLine(x0 + strGap, y0, x1 + strGap, y1, linePaint)

        val floorLeft = x0 - 60f
        val floorRight = x1 + strGap + 80f
        canvas.drawLine(floorLeft, y0, floorRight, y0, linePaint)

        val wallX = x1 + strGap + 30f
        val wallTop = y1 - 60f
        canvas.drawLine(wallX, y0, wallX, wallTop, linePaint)

        val landingW = 70f
        canvas.drawLine(x1, y1, x1 + landingW, y1, linePaint)
        canvas.drawLine(x1 + landingW, y1, x1 + landingW, y1 - 12f, linePaint)
        canvas.drawLine(x1 + landingW, y1 - 12f, x1 + landingW - 20f, y1 - 12f, linePaint)

        val railOffset = 55f
        val railX0 = x0 - railOffset * cosA
        val railY0 = y0 + railOffset * sinA
        val railX1 = x1 + strGap + railOffset * cosA
        val railY1 = y1 - railOffset * sinA
        canvas.drawLine(railX0, railY0, railX1, railY1, thinLinePaint)

        val numSteps = stepCount.coerceIn(3, 30)

        val postSpacing = 5
        for (i in postSpacing until numSteps step postSpacing) {
            val t = i.toFloat() / numSteps
            val lx = x0 + (x1 - x0) * t + strGap / 2
            val ly = y0 + (y1 - y0) * t
            val rx = railX0 + (railX1 - railX0) * t
            val ry = railY0 + (railY1 - railY0) * t
            canvas.drawLine(lx, ly, rx, ry, thinLinePaint)
        }
        val risePerStep = sRise / numSteps
        val runPerStep = risePerStep / tanA

        val treadFill = Paint().apply {
            color = Color.parseColor("#4090CAF9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        for (i in 0 until numSteps) {
            val t1 = i.toFloat() / numSteps
            val t2 = (i + 1).toFloat() / numSteps

            val lx = x0 + (x1 - x0) * t1
            val ly = y0 + (y1 - y0) * t1
            val rx = lx + runPerStep
            val nlx = x0 + (x1 - x0) * t2
            val nly = y0 + (y1 - y0) * t2

            val treadPath = Path()
            treadPath.moveTo(lx, ly)
            treadPath.lineTo(rx, ly)
            treadPath.lineTo(rx, nly)
            treadPath.lineTo(nlx, nly)
            treadPath.close()
            canvas.drawPath(treadPath, treadFill)

            canvas.drawLine(lx, ly, rx, ly, thinLinePaint)
            canvas.drawLine(rx, ly, rx, nly, thinLinePaint)
            canvas.drawLine(rx, nly, nlx, nly, thinLinePaint)
        }

        val riseMm = heightMm / numSteps
        val runMm = riseMm / tanA
        val stringerLen = heightMm / sinA
        val totalRun = heightMm / tanA

        val arrowSize = 7f

        drawEngDimH(canvas, x0, y0 + 35f, x1, "${totalRun.toInt()}", arrowSize)
        drawEngDimV(canvas, wallX + 25f, y0, y1, "${heightMm.toInt()}", arrowSize)

        val dimStep = numSteps / 3
        val ds1 = dimStep.toFloat() / numSteps
        val ds2 = (dimStep + 1).toFloat() / numSteps
        val dsx1 = x0 + (x1 - x0) * ds1
        val dsy1 = y0 + (y1 - y0) * ds1
        val dsx2 = dsx1 + runPerStep
        val dsy2 = dsy1
        val nsx = x0 + (x1 - x0) * ds2
        val nsy = y0 + (y1 - y0) * ds2

        drawEngDimV(canvas, dsx2 + 25f, dsy1, nsy, "${riseMm.toInt()}", arrowSize)
        drawEngDimH(canvas, dsx1 - 8f, dsy1 + 25f, dsx2 + 8f, "${runMm.toInt()}", arrowSize)

        val angleArcR = 40f
        val arcX = x0
        val arcY = y0
        canvas.drawLine(arcX, arcY, arcX + angleArcR, arcY, engDimPaint)
        canvas.drawLine(arcX, arcY, arcX + angleArcR * cosA, arcY - angleArcR * sinA, engDimPaint)
        val arcRect = RectF(arcX, arcY - angleArcR, arcX + angleArcR, arcY)
        canvas.drawArc(arcRect, -angle.toFloat(), angle.toFloat(), false, engDimPaint)
        canvas.drawText("${angle.toInt()}°", arcX + angleArcR * 0.6f, arcY - angleArcR * 0.25f, engDimTextPaint)

        val strStartX = x0 - 20f * cosA
        val strStartY = y0 + 20f * sinA
        val strEndX = x1 + strGap + 20f * cosA
        val strEndY = y1 - 20f * sinA
        canvas.drawLine(strStartX, strStartY, strEndX, strEndY, engDimPaint)

        val perpX = -sinA * 10f
        val perpY = -cosA * 10f
        canvas.drawLine(strStartX, strStartY, strStartX + perpX, strStartY + perpY, engDimPaint)
        canvas.drawLine(strStartX, strStartY, strStartX - perpX, strStartY - perpY, engDimPaint)
        canvas.drawLine(strEndX, strEndY, strEndX + perpX, strEndY + perpY, engDimPaint)
        canvas.drawLine(strEndX, strEndY, strEndX - perpX, strEndY - perpY, engDimPaint)

        val strMidX = (strStartX + strEndX) / 2
        val strMidY = (strStartY + strEndY) / 2
        canvas.save()
        canvas.rotate(-angle, strMidX, strMidY)
        canvas.drawText("${stringerLen.toInt()}", strMidX, strMidY - 10f, engDimTextPaint)
        canvas.restore()
    }

    private fun drawEngDimH(canvas: Canvas, left: Float, y: Float, right: Float, text: String, arrow: Float) {
        canvas.drawLine(left, y, right, y, engDimPaint)
        canvas.drawLine(left, y - arrow, left, y + arrow, engDimPaint)
        canvas.drawLine(right, y - arrow, right, y + arrow, engDimPaint)
        canvas.drawText(text, (left + right) / 2, y - 8f, engDimTextPaint)
    }

    private fun drawEngDimV(canvas: Canvas, x: Float, top: Float, bottom: Float, text: String, arrow: Float) {
        canvas.drawLine(x, top, x, bottom, engDimPaint)
        canvas.drawLine(x - arrow, top, x + arrow, top, engDimPaint)
        canvas.drawLine(x - arrow, bottom, x + arrow, bottom, engDimPaint)
        canvas.save()
        canvas.rotate(-90f, x + 14f, (top + bottom) / 2)
        canvas.drawText(text, x + 14f, (top + bottom) / 2 + 6f, engDimTextPaint)
        canvas.restore()
    }

    private fun drawFrontView(canvas: Canvas, startX: Float, endX: Float, topY: Float, availH: Float, rectW: Float, rectH: Float) {
        val cx = (startX + endX) / 2
        canvas.drawText("АНФАС", cx, topY - 8f, titlePaint)

        when (structureType) {
            StructureType.RECTANGLE, StructureType.SQUARE -> {
                val left = cx - rectW / 2
                val top = topY + (availH - rectH) / 2
                canvas.drawRect(left, top, left + rectW, top + rectH, fillPaint)
                canvas.drawRect(left, top, left + rectW, top + rectH, linePaint)
                drawHorizDim(canvas, left, top + rectH, left + rectW, "${widthMm.toInt()}")
                drawVertDim(canvas, left, top, top + rectH, "${heightMm.toInt()}")
            }
            StructureType.FRAME -> {
                val left = cx - rectW / 2
                val top = topY + (availH - rectH) / 2
                canvas.drawRect(left, top, left + rectW, top + rectH, fillPaint)
                canvas.drawRect(left, top, left + rectW, top + rectH, linePaint)
                canvas.drawLine(left, topY + availH / 2, left + rectW, topY + availH / 2, linePaint)
                drawHorizDim(canvas, left, top + rectH, left + rectW, "${widthMm.toInt()}")
                drawVertDim(canvas, left, top, top + rectH, "${heightMm.toInt()}")
            }
            StructureType.GATE -> {
                val left = cx - rectW / 2
                val top = topY + (availH - rectH) / 2
                canvas.drawRect(left, top, left + rectW, top + rectH, fillPaint)
                canvas.drawRect(left, top, left + rectW, top + rectH, linePaint)
                canvas.drawLine(cx, top, cx, top + rectH, linePaint)
                drawHorizDim(canvas, left, top + rectH, left + rectW, "${widthMm.toInt()}")
                drawVertDim(canvas, left, top, top + rectH, "${heightMm.toInt()}")
            }
            StructureType.GRID -> {
                val left = cx - rectW / 2
                val top = topY + (availH - rectH) / 2
                canvas.drawRect(left, top, left + rectW, top + rectH, fillPaint)
                canvas.drawRect(left, top, left + rectW, top + rectH, linePaint)
                val cols = 5; val rows = 4
                val cw = rectW / cols; val ch = rectH / rows
                for (i in 1 until cols) canvas.drawLine(left + cw * i, top, left + cw * i, top + rectH, dimPaint)
                for (i in 1 until rows) canvas.drawLine(left, top + ch * i, left + rectW, top + ch * i, dimPaint)
                drawHorizDim(canvas, left, top + rectH, left + rectW, "${widthMm.toInt()}")
                drawVertDim(canvas, left, top, top + rectH, "${heightMm.toInt()}")
            }
            StructureType.TRUSS -> {
                val left = cx - rectW / 2
                val top = topY + (availH - rectH) / 2
                canvas.drawRect(left, top, left + rectW, top + rectH, fillPaint)
                canvas.drawRect(left, top, left + rectW, top + rectH, linePaint)
                val midY = top + rectH / 2
                canvas.drawLine(left, midY, left + rectW, midY, linePaint)
                val segs = 4; val sw = rectW / segs
                for (i in 0 until segs) {
                    val x1 = left + sw * i; val x2 = left + sw * (i + 1)
                    if (i % 2 == 0) canvas.drawLine(x1, top, x2, midY, linePaint)
                    else canvas.drawLine(x1, midY, x2, top, linePaint)
                    canvas.drawLine(x1, midY, x2, top + rectH, linePaint)
                }
                drawHorizDim(canvas, left, top + rectH, left + rectW, "${widthMm.toInt()}")
                drawVertDim(canvas, left, top, top + rectH, "${heightMm.toInt()}")
            }
            StructureType.CUSTOM -> {
                val left = cx - rectW / 2
                val top = topY + (availH - rectH) / 2
                canvas.drawRect(left, top, left + rectW, top + rectH, fillPaint)
                canvas.drawRect(left, top, left + rectW, top + rectH, linePaint)
                drawHorizDim(canvas, left, top + rectH, left + rectW, "${widthMm.toInt()}")
                drawVertDim(canvas, left, top, top + rectH, "${heightMm.toInt()}")
            }
            else -> {}
        }
    }

    private fun drawSideView(canvas: Canvas, startX: Float, endX: Float, topY: Float, availH: Float, rectW: Float, rectH: Float) {
        val cx = (startX + endX) / 2
        canvas.drawText("ПРОФИЛЬ", cx, topY - 8f, titlePaint)

        val profileW = min(rectW * 0.3f, 50f)
        val left = cx - profileW / 2
        val top = topY + (availH - rectH) / 2

        canvas.drawRect(left, top, left + profileW, top + rectH, fillProfilePaint)
        canvas.drawRect(left, top, left + profileW, top + rectH, linePaint)
        val wall = 3f
        if (profileW > wall * 2 + 4 && rectH > wall * 2 + 4) {
            canvas.drawRect(left + wall, top + wall, left + profileW - wall, top + rectH - wall, dimPaint)
        }
        drawVertDim(canvas, left - 10f, top, top + rectH, "${heightMm.toInt()}")
    }

    private fun drawTopView(canvas: Canvas, startX: Float, endX: Float, topY: Float, availH: Float, rectW: Float, rectH: Float) {
        val cx = (startX + endX) / 2
        val cy = topY + availH / 2

        canvas.drawText("ВИД СВЕРХУ", cx, topY - 8f, titlePaint)

        val topRectW = rectW
        val topRectH = rectW * 0.35f
        val left = cx - topRectW / 2
        val top = cy - topRectH / 2

        when (structureType) {
            StructureType.RECTANGLE, StructureType.SQUARE, StructureType.FRAME, StructureType.GATE -> {
                canvas.drawRect(left, top, left + topRectW, top + topRectH, fillPaint)
                canvas.drawRect(left, top, left + topRectW, top + topRectH, linePaint)
                if (structureType == StructureType.GATE) canvas.drawLine(cx, top, cx, top + topRectH, linePaint)
                if (structureType == StructureType.FRAME) canvas.drawLine(left, cy, left + topRectW, cy, linePaint)
                drawHorizDim(canvas, left, top + topRectH, left + topRectW, "${widthMm.toInt()}")
            }
            StructureType.GRID -> {
                canvas.drawRect(left, top, left + topRectW, top + topRectH, fillPaint)
                canvas.drawRect(left, top, left + topRectW, top + topRectH, linePaint)
                val cols = 5; val rows = 3
                val cw = topRectW / cols; val ch = topRectH / rows
                for (i in 1 until cols) canvas.drawLine(left + cw * i, top, left + cw * i, top + topRectH, dimPaint)
                for (i in 1 until rows) canvas.drawLine(left, top + ch * i, left + topRectW, top + ch * i, dimPaint)
                drawHorizDim(canvas, left, top + topRectH, left + topRectW, "${widthMm.toInt()}")
            }
            StructureType.TRUSS -> {
                canvas.drawRect(left, top, left + topRectW, top + topRectH, fillPaint)
                canvas.drawRect(left, top, left + topRectW, top + topRectH, linePaint)
                canvas.drawLine(left, cy, left + topRectW, cy, linePaint)
                drawHorizDim(canvas, left, top + topRectH, left + topRectW, "${widthMm.toInt()}")
            }
            StructureType.CUSTOM -> {
                canvas.drawRect(left, top, left + topRectW, top + topRectH, fillPaint)
                canvas.drawRect(left, top, left + topRectW, top + topRectH, linePaint)
                drawHorizDim(canvas, left, top + topRectH, left + topRectW, "${widthMm.toInt()}")
            }
            else -> {}
        }
    }

    private fun drawHorizDim(canvas: Canvas, left: Float, y: Float, right: Float, text: String) {
        val dy = y + 10f
        canvas.drawLine(left, dy, right, dy, dimPaint)
        canvas.drawLine(left, dy - 3f, left, dy + 3f, dimPaint)
        canvas.drawLine(right, dy - 3f, right, dy + 3f, dimPaint)
        canvas.drawText("$text мм", (left + right) / 2, dy + 14f, dimTextPaint)
    }

    private fun drawVertDim(canvas: Canvas, x: Float, top: Float, bottom: Float, text: String) {
        val dx = x - 10f
        canvas.drawLine(dx, top, dx, bottom, dimPaint)
        canvas.drawLine(dx - 3f, top, dx + 3f, top, dimPaint)
        canvas.drawLine(dx - 3f, bottom, dx + 3f, bottom, dimPaint)
        canvas.save()
        canvas.rotate(-90f, dx - 12f, (top + bottom) / 2)
        canvas.drawText("$text мм", dx - 12f, (top + bottom) / 2 + 4f, dimTextPaint)
        canvas.restore()
    }
}
