package com.example.weldcalc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class HistoryActivity : AppCompatActivity() {

    private lateinit var adapter: ProjectAdapter
    private lateinit var repository: ProjectRepository
    private lateinit var tvEmptyState: TextView
    private lateinit var recyclerProjects: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val db = AppDatabase.getDatabase(this)
        repository = ProjectRepository(db.projectDao())

        tvEmptyState = findViewById(R.id.tvEmptyState)
        recyclerProjects = findViewById(R.id.recyclerProjects)

        adapter = ProjectAdapter(
            onItemClick = { project -> openProject(project) },
            onDeleteClick = { project -> confirmDelete(project) }
        )

        recyclerProjects.layoutManager = LinearLayoutManager(this)
        recyclerProjects.adapter = adapter

        lifecycleScope.launch {
            repository.allProjects.collectLatest { projects ->
                adapter.submitList(projects)
                if (projects.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerProjects.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    recyclerProjects.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun openProject(project: Project) {
        val pdfFile = File(project.pdfPath)
        if (!pdfFile.exists()) {
            Toast.makeText(this, "PDF-файл не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Нет приложения для открытия PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(project: Project) {
        AlertDialog.Builder(this)
            .setTitle("Удалить проект?")
            .setMessage(project.name)
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteById(project.id)
                    deleteFiles(project)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteFiles(project: Project) {
        try {
            val thumbnailFile = File(project.thumbnailPath)
            if (thumbnailFile.exists()) thumbnailFile.delete()

            val pdfFile = File(project.pdfPath)
            if (pdfFile.exists()) pdfFile.delete()
        } catch (_: Exception) {}
    }
}
