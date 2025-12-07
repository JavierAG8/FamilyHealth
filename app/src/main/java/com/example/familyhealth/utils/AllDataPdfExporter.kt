package com.example.familyhealth.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.format.DateFormat
import com.example.familyhealth.data.local.MedicationEntity
import com.example.familyhealth.data.local.SymptomEntity
import com.example.familyhealth.viewmodel.PatientProfile
import java.io.IOException

object AllDataPdfExporter {

    fun export(
        context: Context,
        uri: Uri,
        profile: PatientProfile,
        medications: List<MedicationEntity>,
        symptoms: List<SymptomEntity>
    ): Boolean {
        val pdf = PdfDocument()
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdf.startPage(pageInfo)
        var canvas = page.canvas

        var x = 40f
        var y = 60f

        fun newLine(lines: Int = 1) {
            y += 16f * lines
            if (y > 780f) {
                pdf.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdf.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }
        }

        fun drawTitle(text: String) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 18f
            canvas.drawText(text, x, y, paint)
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            newLine(2)
        }

        fun drawSection(text: String) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            canvas.drawText(text, x, y, paint)
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            newLine()
        }

        fun drawLine(text: String) {
            canvas.drawText(text, x, y, paint)
            newLine()
        }

        fun formatDateTime(millis: Long): String =
            DateFormat.format("dd/MM/yyyy HH:mm", millis).toString()

        drawTitle("Informe clínico - FamilyHealth")

        drawSection("Datos del paciente")

        val bmiText = runCatching {
            val w = profile.weightKg ?: 0f
            val hCm = profile.heightCm ?: 0f
            if (w > 0 && hCm > 0) {
                val hM = hCm / 100f
                val bmi = w / (hM * hM)
                "IMC: %.1f".format(bmi)
            } else null
        }.getOrNull()

        drawLine("Nombre: ${profile.name.ifBlank { "-" }}")
        drawLine("Edad: ${profile.age?.toString() ?: "-"}")
        drawLine("Sexo / género: ${profile.gender.ifBlank { "-" }}")
        drawLine("Peso: ${profile.weightKg?.toString() ?: "-"} kg")
        drawLine("Altura: ${profile.heightCm?.toString() ?: "-"} cm")
        drawLine("Grupo sanguíneo: ${profile.bloodType.ifBlank { "-" }}")
        bmiText?.let { drawLine(it) }
        newLine()

        drawLine("Patologías / diagnósticos:")
        drawLine(if (profile.conditions.isBlank()) "  - Sin información" else "  ${profile.conditions}")
        newLine()

        drawLine("Alergias:")
        drawLine(if (profile.allergies.isBlank()) "  - Sin información" else "  ${profile.allergies}")
        newLine()

        drawLine("Notas / observaciones:")
        drawLine(if (profile.notes.isBlank()) "  - Sin información" else "  ${profile.notes}")
        newLine(2)

        drawSection("Tratamientos y medicación")

        if (medications.isEmpty()) {
            drawLine("- No hay medicación registrada.")
        } else {
            medications.forEachIndexed { index, m ->
                drawLine("${index + 1}. ${m.name}")
                drawLine("   Dosis: ${m.dosage}")
                drawLine("   Frecuencia: ${m.frequency}")
                if (m.startDate.isNotBlank() || m.endDate.isNotBlank()) {
                    drawLine("   Desde: ${m.startDate.ifBlank { "-" }}  Hasta: ${m.endDate.ifBlank { "-" }}")
                }
                drawLine("   Estado: ${if (m.taken) "Marcada como tomada" else "Pendiente"}")
                newLine()
            }
        }
        newLine(2)

        drawSection("Registro de síntomas")

        if (symptoms.isEmpty()) {
            drawLine("- No hay síntomas registrados.")
        } else {
            symptoms.forEachIndexed { index, s ->
                drawLine("${index + 1}. ${formatDateTime(s.dateTime)}")
                drawLine("   Intensidad: ${s.intensity}/10")
                drawLine("   Descripción: ${s.description}")
                if (s.tags.isNotBlank()) {
                    drawLine("   Etiquetas: ${s.tags}")
                }
                newLine()
            }
        }

        pdf.finishPage(page)

        return try {
            val out = context.contentResolver.openOutputStream(uri) ?: return false
            out.use { stream ->
                pdf.writeTo(stream)
            }
            pdf.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                pdf.close()
            } catch (_: Exception) {}
            false
        }
    }
}
