package com.example.familyhealth.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.format.DateFormat
import com.example.familyhealth.data.local.MedicationEntity
import java.io.IOException
import java.util.Calendar

object MedicationPdfExporter {

    fun export(
        context: Context,
        uri: Uri,
        meds: List<MedicationEntity>
    ): Boolean {
        if (meds.isEmpty()) return false

        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 18f
        }
        val headerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
        }

        var currentY = 60f
        var pageNumber = 1

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            val now = Calendar.getInstance().timeInMillis
            val dateStr = DateFormat.format("dd/MM/yyyy HH:mm", now).toString()

            canvas.drawText("FamilyHealth - Informe de medicación", 40f, 40f, titlePaint)
            canvas.drawText("Generado: $dateStr", 40f, 55f, headerPaint)

            currentY = 80f
            return page
        }

        var page = newPage()
        val canvas = { page.canvas }

        meds.forEachIndexed { index, m ->
            if (currentY > pageHeight - 80) {
                pdf.finishPage(page)
                pageNumber++
                page = newPage()
            }

            val nameLine = "${index + 1}. ${m.name}  (${m.dosage})"
            val freqLine = "   Frecuencia: ${m.frequency}"
            val dateLine = "   Desde: ${m.startDate}   Hasta: ${m.endDate}"
            val takenLine = "   Estado: ${if (m.taken) "Tomada / plan completado" else "Pendiente"}"

            canvas().drawText(nameLine, 40f, currentY, textPaint)
            currentY += 14f
            canvas().drawText(freqLine, 40f, currentY, textPaint)
            currentY += 14f
            canvas().drawText(dateLine, 40f, currentY, textPaint)
            currentY += 14f
            canvas().drawText(takenLine, 40f, currentY, textPaint)
            currentY += 20f
        }

        pdf.finishPage(page)

        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                pdf.writeTo(out)
            }
            pdf.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            pdf.close()
            false
        }
    }
}
