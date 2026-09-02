package com.example.weldcalc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val structureType: String,
    val profile: String,
    val material: String,
    val height: Float,
    val width: Float,
    val length: Float,
    val angle: Float,
    val stepCount: Int,
    val totalLength: Float,
    val unit: String,
    val weight: Float,
    val cost: Float,
    val thumbnailPath: String,
    val pdfPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
