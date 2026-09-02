package com.example.weldcalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btnNewCalculation = findViewById<MaterialButton>(R.id.btnNewCalculation)
        val calculators = findViewById<MaterialButton>(R.id.btnCalculators)
        val history = findViewById<MaterialButton>(R.id.btnHistory)
        val settings = findViewById<MaterialButton>(R.id.btnSettings)

        btnNewCalculation.setOnClickListener {
            val intent = Intent(this, SelectStructureActivity::class.java)
            startActivity(intent)
        }

        calculators.setOnClickListener {
            Toast.makeText(this, "Калькуляторы — скоро", Toast.LENGTH_SHORT).show()
        }

        history.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        settings.setOnClickListener {
            Toast.makeText(this, "Настройки — скоро", Toast.LENGTH_SHORT).show()
        }
    }
}