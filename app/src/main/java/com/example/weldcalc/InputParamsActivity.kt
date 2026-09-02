package com.example.weldcalc

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import kotlin.math.PI

class InputParamsActivity : AppCompatActivity() {

    private var lastPdfFile: File? = null
    private lateinit var tvResultLength: TextView
    private lateinit var tvResultWeight: TextView
    private lateinit var tvResultCost: TextView
    private lateinit var tvResultMaterial: TextView
    private lateinit var btnOpenPdf: MaterialButton
    private lateinit var btnExportPdf: MaterialButton
    private var isCalculated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_params)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val structureTypeName = intent.getStringExtra("structureType") ?: "RECTANGLE"
        val structureType = try {
            StructureType.valueOf(structureTypeName)
        } catch (_: Exception) {
            StructureType.RECTANGLE
        }
        val structureDisplayName = intent.getStringExtra("structureName") ?: structureType.name

        val tvStructure = findViewById<TextView>(R.id.tvStructureName)
        tvStructure.text = structureDisplayName

        val etLength = findViewById<TextInputEditText>(R.id.etLength)
        val etWidth = findViewById<TextInputEditText>(R.id.etWidth)
        val etHeight = findViewById<TextInputEditText>(R.id.etHeight)
        val etThickness = findViewById<TextInputEditText>(R.id.etThickness)
        val etQuantity = findViewById<TextInputEditText>(R.id.etQuantity)
        val etPrice = findViewById<TextInputEditText>(R.id.etPrice)
        val spinnerMaterial = findViewById<Spinner>(R.id.spinnerMaterial)
        val spinnerProfile = findViewById<Spinner>(R.id.spinnerProfile)
        val radioPriceUnit = findViewById<RadioGroup>(R.id.radioPriceUnit)
        val canvasView = findViewById<RCView>(R.id.canvasView)

        // Доп.поля для лестницы
        val extraLadderGroup = findViewById<View>(R.id.extraLadderGroup)
        val etStepWidth = findViewById<TextInputEditText>(R.id.etStepWidth)
        val etStepCount = findViewById<TextInputEditText>(R.id.etStepCount)

        // Доп.поля для произвольной формы
        val extraCustomGroup = findViewById<View>(R.id.extraCustomGroup)
        val etSideCount = findViewById<TextInputEditText>(R.id.etSideCount)
        val etAngle = findViewById<TextInputEditText>(R.id.etAngle)

        tvResultLength = findViewById(R.id.tvResultLength)
        tvResultWeight = findViewById(R.id.tvResultWeight)
        tvResultCost = findViewById(R.id.tvResultCost)
        tvResultMaterial = findViewById(R.id.tvResultMaterial)
        btnOpenPdf = findViewById(R.id.btnOpenPdf)
        btnExportPdf = findViewById(R.id.btnExportPdf)

        // PDF кнопки заблокированы до расчёта
        btnOpenPdf.isEnabled = false
        btnExportPdf.isEnabled = false
        btnOpenPdf.alpha = 0.5f
        btnExportPdf.alpha = 0.5f

        val materials = arrayOf("Сталь", "Нержавейка", "Алюминий", "Медь")
        val materialAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, materials)
        materialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMaterial.adapter = materialAdapter

        val profiles = ProfileType.values()
        val profileNames = profiles.map { it.displayName }
        val profileAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, profileNames)
        profileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProfile.adapter = profileAdapter

        val layoutThickness = findViewById<View>(R.id.layoutThickness)
        val layoutPrice = findViewById<TextInputLayout>(R.id.layoutPrice)

        radioPriceUnit.setOnCheckedChangeListener { _, checkedId ->
            layoutPrice.hint = if (checkedId == R.id.rbPerKg) "Цена за кг (₽)" else "Цена за метр (₽)"
        }

        spinnerProfile.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = profiles[position]
                layoutThickness.visibility = if (selected == ProfileType.CUSTOM) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Автоподсчёт ступеней при изменении высоты
        fun autoCalcSteps() {
            val h = etHeight.text.toString().toFloatOrNull() ?: return
            if (h <= 0f) return
            val idealRise = 180f
            val steps = (h / idealRise).toInt().coerceIn(3, 30)
            etStepCount.setText(steps.toString())
        }
        etHeight.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (structureType == StructureType.LADDER) autoCalcSteps()
            }
        })

        // Показываем/скрываем доп.поля в зависимости от типа конструкции
        fun updateExtraFields() {
            when (structureType) {
                StructureType.LADDER -> {
                    extraLadderGroup.visibility = View.VISIBLE
                    extraCustomGroup.visibility = View.GONE
                    autoCalcSteps()
                }
                StructureType.CUSTOM -> {
                    extraLadderGroup.visibility = View.GONE
                    extraCustomGroup.visibility = View.VISIBLE
                }
                else -> {
                    extraLadderGroup.visibility = View.GONE
                    extraCustomGroup.visibility = View.GONE
                }
            }
        }
        updateExtraFields()

        canvasView.visibility = View.GONE

        val btnCalculate = findViewById<MaterialButton>(R.id.btnCalculate)
        btnCalculate.setOnClickListener {
            val length = etLength.text.toString().toFloatOrNull() ?: 0f
            val width = etWidth.text.toString().toFloatOrNull() ?: 0f
            val height = etHeight.text.toString().toFloatOrNull() ?: 0f
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
            val pricePerUnit = etPrice.text.toString().toFloatOrNull() ?: 0f
            val material = spinnerMaterial.selectedItem.toString()
            val selectedProfile = profiles[spinnerProfile.selectedItemPosition]
            val priceIsPerKg = radioPriceUnit.checkedRadioButtonId == R.id.rbPerKg

            val weightPerUnit = if (selectedProfile == ProfileType.CUSTOM) {
                etThickness.text.toString().toFloatOrNull() ?: 0f
            } else {
                selectedProfile.weightPerMeter
            }

            val ladderStepCount = etStepCount.text.toString().toIntOrNull() ?: 10
            val (totalLength, unit) = calculateTotal(
                structureType, length, width, height, quantity, selectedProfile,
                stepWidth = 300f,
                stepCount = ladderStepCount,
                sideCount = etSideCount.text.toString().toIntOrNull() ?: 4,
                angle = etStepWidth.text.toString().toFloatOrNull() ?: 42f
            )

            val weight = totalLength * weightPerUnit
            val cost = if (priceIsPerKg) weight * pricePerUnit else totalLength * pricePerUnit
            val priceLabel = if (priceIsPerKg) "за кг" else "за $unit"

            tvResultMaterial.text = "$material | ${selectedProfile.displayName}"
            tvResultLength.text = "Длина: ${formatValue(totalLength)} $unit"
            tvResultWeight.text = "Вес: ${"%.2f".format(weight)} кг"
            tvResultCost.text = "Итого: ${"%.2f".format(cost)} ₽ ($priceLabel)"

            findViewById<View>(R.id.resultsContainer).visibility = View.VISIBLE

            // Схема
            canvasView.visibility = View.VISIBLE
            val ladderAngle = etStepWidth.text.toString().toFloatOrNull() ?: 42f
            canvasView.setDimensions(length, height.coerceAtLeast(width), structureType, ladderAngle, ladderStepCount)

            // Разблокируем PDF
            isCalculated = true
            btnOpenPdf.isEnabled = true
            btnExportPdf.isEnabled = true
            btnOpenPdf.alpha = 1.0f
            btnExportPdf.alpha = 1.0f

            val pdfFile = createPdf(
                structureName = structureDisplayName,
                structureType = structureType,
                material = material,
                profile = selectedProfile.displayName,
                length = length, width = width, height = height,
                quantity = quantity, totalLength = totalLength, unit = unit,
                cost = cost, weight = weight, priceLabel = priceLabel,
                schemaView = canvasView
            )
            lastPdfFile = pdfFile
            Toast.makeText(this, "PDF сохранён", Toast.LENGTH_SHORT).show()
        }

        btnOpenPdf.setOnClickListener {
            val pdfFile = lastPdfFile ?: return@setOnClickListener
            openPdf(pdfFile)
        }

        btnExportPdf.setOnClickListener {
            val pdfFile = lastPdfFile ?: return@setOnClickListener
            sharePdf(pdfFile)
        }
    }

    private fun calculateTotal(
        type: StructureType, length: Float, width: Float, height: Float,
        quantity: Int, profile: ProfileType,
        stepWidth: Float = 300f, stepCount: Int = 10,
        sideCount: Int = 4, angle: Float = 90f
    ): Pair<Float, String> {
        val isArea = profile == ProfileType.SHEET_2 || profile == ProfileType.SHEET_3

        return when (type) {
            StructureType.RECTANGLE -> Pair(2 * (length + width) * quantity, "м")
            StructureType.SQUARE -> Pair(4 * length * quantity, "м")
            StructureType.FRAME -> Pair((2 * length + 4 * height) * quantity, "м")
            StructureType.LADDER -> {
                val angleRad = (angle * PI / 180f).toFloat()
                val sinA = sin(angleRad.toDouble()).toFloat()
                val tanA = tan(angleRad.toDouble()).toFloat()

                val kosourLength = height / sinA
                val treadDepth = height / (stepCount * tanA)
                val riserHeight = height / stepCount

                val kosours = 2 * kosourLength
                val treads = stepCount * treadDepth
                val risers = stepCount * riserHeight

                Pair((kosours + treads + risers) * quantity / 1000f, "м")
            }
            StructureType.GATE -> Pair((2 * (length + height) + height) * quantity, "м")
            StructureType.GRID -> {
                if (isArea) Pair(length * width * quantity, "м²")
                else {
                    val frame = 2 * (length + width)
                    val horizontals = (width / 200f).toInt() * length
                    val verticals = (length / 200f).toInt() * width
                    Pair((frame + horizontals + verticals) * quantity, "м")
                }
            }
            StructureType.TRUSS -> {
                val chord = 2 * length
                val diagonals = ((length / 300f).toInt() + 1) * height
                Pair((chord + diagonals) * quantity, "м")
            }
            StructureType.CUSTOM -> {
                // Многоугольник: sideCount сторон, angle = угол между сторонами
                // perimeter = sideCount * sideLength (равносторонний)
                val sideLength = length  // длина одной стороны
                Pair(sideCount * sideLength * quantity, "м")
            }
        }
    }

    private fun formatValue(value: Float): String {
        return if (value == value.toInt().toFloat()) value.toInt().toString()
        else "%.2f".format(value)
    }

    private fun openPdf(pdfFile: File) {
        val pdfUri = androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.provider", pdfFile)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try { startActivity(intent) }
        catch (e: Exception) { Toast.makeText(this, "Нет приложения для PDF", Toast.LENGTH_SHORT).show() }
    }

    private fun sharePdf(pdfFile: File) {
        val pdfUri = androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.provider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri)
        shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(shareIntent, "Отправить PDF"))
    }

    private fun createPdf(
        structureName: String, structureType: StructureType, material: String, profile: String,
        length: Float, width: Float, height: Float,
        quantity: Int, totalLength: Float, unit: String,
        cost: Float, weight: Float, priceLabel: String,
        schemaView: View
    ): File {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK; textSize = 20f; typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val subtitlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY; textSize = 13f
        }
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1A5276"); textSize = 14f; typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val dividerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY; strokeWidth = 1f
        }

        var pageNum = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var canvas = page.canvas
        var y = 50f
        val leftMargin = 40f
        val valueX = 280f

        fun checkNewPage(neededSpace: Float) {
            if (y + neededSpace > pageHeight - 50f) {
                pdf.finishPage(page)
                pageNum++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
                canvas = page.canvas
                y = 50f
            }
        }

        canvas.drawText("СМЕТА ПРОЕКТА", leftMargin, y, titlePaint)
        y += 25f
        canvas.drawText(structureName, leftMargin, y, subtitlePaint)
        y += 15f
        canvas.drawLine(leftMargin, y, pageWidth - leftMargin, y, dividerPaint)
        y += 25f

        fun row(label: String, value: String) {
            canvas.drawText(label, leftMargin, y, subtitlePaint)
            canvas.drawText(value, valueX, y, headerPaint)
            y += 20f
        }

        row("Материал:", material)
        row("Профиль:", profile)
        row("Длина:", "${length.toInt()} мм")
        row("Ширина:", "${width.toInt()} мм")
        row("Высота:", "${height.toInt()} мм")
        row("Кол-во:", "$quantity шт.")
        y += 10f
        canvas.drawLine(leftMargin, y, pageWidth - leftMargin, y, dividerPaint)
        y += 25f

        canvas.drawText("ОБЩИЙ ИТОГ", leftMargin, y, titlePaint)
        y += 25f
        row("Длина металла:", "${formatValue(totalLength)} $unit")
        row("Вес:", "${"%.2f".format(weight)} кг")

        canvas.drawText("Стоимость:", leftMargin, y, subtitlePaint)
        val costPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1A5276"); textSize = 16f; typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("${"%.2f".format(cost)} ₽ ($priceLabel)", valueX, y, costPaint)
        y += 35f

        canvas.drawLine(leftMargin, y, pageWidth - leftMargin, y, dividerPaint)
        y += 25f

        checkNewPage(400f)
        canvas.drawText("СХЕМА КОНСТРУКЦИИ", leftMargin, y, headerPaint)
        y += 20f

        val schemaBitmap = getBitmapFromView(schemaView)
        val bmpW = schemaBitmap.width
        val bmpH = schemaBitmap.height

        if (structureType == StructureType.LADDER) {
            val maxW = pageWidth - 80f
            val maxH = 500f
            val ratio = minOf(maxW / bmpW, maxH / bmpH)
            val scaledW = (bmpW * ratio).toInt()
            val scaledH = (bmpH * ratio).toInt()
            val scaled = Bitmap.createScaledBitmap(schemaBitmap, scaledW, scaledH, true)
            checkNewPage(scaledH + 20f)
            canvas.drawBitmap(scaled, (pageWidth - scaledW) / 2f, y, null)
            y += scaledH + 20f
        } else {
            val topRowH = (bmpH * 0.55f).toInt()
            val botRowH = bmpH - topRowH
            val halfW = bmpW / 2

            val frontBmp = Bitmap.createBitmap(schemaBitmap, 0, 0, halfW, topRowH)
            val sideBmp = Bitmap.createBitmap(schemaBitmap, halfW, 0, halfW, topRowH)
            val topBmp = Bitmap.createBitmap(schemaBitmap, 0, topRowH, bmpW, botRowH)

            val viewPairs = arrayOf(
                Pair("АНФАС", frontBmp),
                Pair("ПРОФИЛЬ", sideBmp),
                Pair("ВИД СВЕРХУ", topBmp)
            )

            val maxViewW = pageWidth - 80f
            val maxViewH = 200f

            for ((name, viewBmp) in viewPairs) {
                val ratio = minOf(maxViewW / viewBmp.width, maxViewH / viewBmp.height)
                val scaledW = (viewBmp.width * ratio).toInt()
                val scaledH = (viewBmp.height * ratio).toInt()
                val scaled = Bitmap.createScaledBitmap(viewBmp, scaledW, scaledH, true)

                checkNewPage(scaledH + 35f)
                canvas.drawText(name, leftMargin, y, subtitlePaint)
                y += 5f
                canvas.drawLine(leftMargin, y, leftMargin + subtitlePaint.measureText(name), y, dividerPaint)
                y += 10f
                canvas.drawBitmap(scaled, (pageWidth - scaledW) / 2f, y, null)
                y += scaledH + 20f
            }
        }

        pdf.finishPage(page)

        val file = File("${filesDir.path}/smeta_${System.currentTimeMillis()}.pdf")
        pdf.writeTo(FileOutputStream(file))
        pdf.close()
        return file
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val w = if (view.width > 0) view.width else 800
        val h = if (view.height > 0) view.height else 600
        val scale = 2
        val bitmap = Bitmap.createBitmap(w * scale, h * scale, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(scale.toFloat(), scale.toFloat())
        view.layout(0, 0, w, h)
        view.draw(canvas)
        return bitmap
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
