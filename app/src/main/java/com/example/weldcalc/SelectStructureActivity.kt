package com.example.weldcalc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SelectStructureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_structure)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Выберите конструкцию"

        val btnFrame = findViewById<MaterialButton>(R.id.btnFrame)
        val btnSquare = findViewById<MaterialButton>(R.id.btnSquare)
        val btnCarcass = findViewById<MaterialButton>(R.id.btnCarcass)
        val btnStairs = findViewById<MaterialButton>(R.id.btnStairs)
        val btnGate = findViewById<MaterialButton>(R.id.btnGate)
        val btnGrid = findViewById<MaterialButton>(R.id.btnGrid)
        val btnTruss = findViewById<MaterialButton>(R.id.btnTruss)
        val btnCustom = findViewById<MaterialButton>(R.id.btnCustom)

        val buttons = mapOf(
            btnFrame to StructureType.FRAME,
            btnSquare to StructureType.SQUARE,
            btnCarcass to StructureType.TRUSS,
            btnStairs to StructureType.LADDER,
            btnGate to StructureType.GATE,
            btnGrid to StructureType.GRID,
            btnTruss to StructureType.TRUSS,
            btnCustom to StructureType.CUSTOM
        )

        val displayNames = mapOf(
            StructureType.FRAME to "Прямоугольная рама",
            StructureType.SQUARE to "Квадрат",
            StructureType.TRUSS to "Ферма",
            StructureType.LADDER to "Лестница",
            StructureType.GATE to "Ворота",
            StructureType.GRID to "Решетка",
            StructureType.CUSTOM to "Произвольная"
        )

        buttons.forEach { (button, type) ->
            button.setOnClickListener {
                val intent = Intent(this, InputParamsActivity::class.java)
                intent.putExtra("structureType", type.name)
                intent.putExtra("structureName", displayNames[type] ?: type.name)
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}