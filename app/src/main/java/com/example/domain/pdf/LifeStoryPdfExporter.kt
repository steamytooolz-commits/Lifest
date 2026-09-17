package com.example.domain.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.Event
import com.example.data.model.Life
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LifeStoryPdfExporter {

    fun exportLifeToPdf(context: Context, life: Life, events: List<Event>): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(20, 30, 55)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(80, 90, 115)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(35, 40, 50)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.rgb(20, 25, 35)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val accentLinePaint = Paint().apply {
            color = Color.rgb(88, 101, 242)
            strokeWidth = 2f
        }

        var y = 50f
        val marginX = 40f

        // Document Header
        canvas.drawText("AI LIFE SIMULATOR — CHRONICLE", marginX, y, titlePaint)
        y += 18f
        val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Official Life Memoir of ${life.playerName} • Generated on $dateStr", marginX, y, subtitlePaint)
        y += 10f
        canvas.drawLine(marginX, y, 555f, y, accentLinePaint)
        y += 24f

        // Life Profile Summary
        canvas.drawText("BIOGRAPHICAL OVERVIEW", marginX, y, boldBodyPaint)
        y += 16f
        canvas.drawText("Name: ${life.playerName}", marginX, y, bodyPaint)
        canvas.drawText("Country: ${life.country}", marginX + 180, y, bodyPaint)
        canvas.drawText("Age: ${life.age} years", marginX + 340, y, bodyPaint)
        y += 14f

        canvas.drawText("Career: ${life.career ?: "Self-Employed / Independent"}", marginX, y, bodyPaint)
        canvas.drawText("Net Worth: $${String.format("%,.2f", life.money)}", marginX + 180, y, bodyPaint)
        canvas.drawText("Karma: ${life.stats.karma.toInt()}/100", marginX + 340, y, bodyPaint)
        y += 14f

        canvas.drawText("Health: ${life.stats.health.toInt()}%", marginX, y, bodyPaint)
        canvas.drawText("Happiness: ${life.stats.happiness.toInt()}%", marginX + 180, y, bodyPaint)
        canvas.drawText("Civic Index: ${life.stats.civicIndex.toInt()}/100", marginX + 340, y, bodyPaint)
        y += 20f

        // Personality & Genetics Traits
        canvas.drawText("TRAITS & INHERITED ATTRIBUTES", marginX, y, boldBodyPaint)
        y += 14f
        val traitsText = if (life.traits.isEmpty()) "None recorded" else life.traits.joinToString(", ")
        canvas.drawText(traitsText, marginX, y, bodyPaint)
        y += 22f

        // Life Chronicles & Key Events
        canvas.drawText("RECORDED LIFE CHRONICLES (${events.size} Events)", marginX, y, boldBodyPaint)
        y += 8f
        canvas.drawLine(marginX, y, 555f, y, accentLinePaint)
        y += 16f

        val eventsToShow = events.take(24) // Top chronological milestones on page 1
        if (eventsToShow.isEmpty()) {
            canvas.drawText("No major incidents or recorded milestones yet in this timeline.", marginX, y, bodyPaint)
            y += 14f
        } else {
            for (ev in eventsToShow) {
                if (y > 780f) break
                val eventDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(ev.timestamp))
                canvas.drawText("[$eventDate • ${ev.type}]", marginX, y, boldBodyPaint)
                y += 12f
                val desc = if (ev.description.length > 85) ev.description.take(82) + "..." else ev.description
                canvas.drawText(desc, marginX + 10, y, bodyPaint)
                y += 14f
            }
        }

        // Footer
        canvas.drawText("AI Life Simulator Engine • Verified Authentic Simulated Timeline", marginX, 810f, subtitlePaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache or files dir
        val outputDir = File(context.filesDir, "life_chronicles").apply { mkdirs() }
        val outputFile = File(outputDir, "${life.playerName.replace(" ", "_")}_Life_Story.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }
}
