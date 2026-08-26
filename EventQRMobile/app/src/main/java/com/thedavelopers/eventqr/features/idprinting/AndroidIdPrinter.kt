package com.thedavelopers.eventqr.features.idprinting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.thedavelopers.eventqr.features.registrations.RegistrationNumberFormatter
import java.io.FileOutputStream

/**
 * Renders a CR80-ratio ID card onto an A4 bond-paper page and opens the
 * system print dialog via [PrintManager].
 *
 * The ID block is sized at actual CR80 dimensions (2.125in x 3.375in) and
 * centered on the A4 sheet. A dashed cut-guide border surrounds the card
 * so staff can trim with scissors and insert into an ID holder.
 *
 * Locked fields (always shown): QR_CODE, ATTENDEE_NAME.
 * Optional fields (toggled by organizer): ATTENDEE_ID, ROLE, EVENT_NAME, EVENT_DATE.
 */
object AndroidIdPrinter {

    // A4 at 72 pt/in: 210mm = 8.268in, 297mm = 11.693in
    private const val PAGE_W_PT = 595
    private const val PAGE_H_PT = 842

    // CR80 actual dimensions: 2.125in x 3.375in
    private const val CARD_W_PT = 153f  // 2.125 * 72
    private const val CARD_H_PT = 243f  // 3.375 * 72

    // Cut-guide: 6pt bleed outside the card border
    private const val CUT_BLEED_PT = 6f
    private const val CUT_W = CARD_W_PT + 2 * CUT_BLEED_PT
    private const val CUT_H = CARD_H_PT + 2 * CUT_BLEED_PT

    // Card content margin (inside the card border)
    private const val CARD_MARGIN_PT = 12f
    private const val CARD_INNER_W = CARD_W_PT - 2 * CARD_MARGIN_PT

    private const val FIELD_ATTENDEE_ID = "ATTENDEE_ID"
    private const val FIELD_ROLE = "ROLE"
    private const val FIELD_EVENT_NAME = "EVENT_NAME"
    private const val FIELD_EVENT_DATE = "EVENT_DATE"

    data class CardData(
        val attendeeName: String,
        val eventName: String = "",
        val registrationNumber: Int? = null,
        val role: String = "",
        val eventDate: String = "",
        val visibleFields: List<String> = emptyList(),
    )

    fun print(context: Context, jobName: String, data: CardData) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(jobName, IdCardPrintAdapter(data), null)
    }

    private class IdCardPrintAdapter(private val data: CardData) : PrintDocumentAdapter() {

        override fun onWrite(
            pages: Array<PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback,
        ) {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W_PT, PAGE_H_PT, 1).create()
            val page = doc.startPage(pageInfo)
            drawPage(page.canvas)
            doc.finishPage(page)

            try {
                FileOutputStream(destination.fileDescriptor).use { doc.writeTo(it) }
            } finally {
                doc.close()
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal,
            callback: LayoutResultCallback,
            extras: Bundle?,
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder("id_card.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()
            callback.onLayoutFinished(info, true)
        }

        private fun drawPage(canvas: Canvas) {
            // Center the cut-guide box on the A4 page
            val cutLeft = (PAGE_W_PT - CUT_W) / 2f
            val cutTop = (PAGE_H_PT - CUT_H) / 2f

            // Dashed cut-guide border
            val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF9CA3AF.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 0.6f
                pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
            }
            canvas.drawRect(cutLeft, cutTop, cutLeft + CUT_W, cutTop + CUT_H, dashPaint)

            // Small corner ticks for scissor alignment
            val tickLen = 8f
            val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF9CA3AF.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
            }
            // Top-left
            canvas.drawLine(cutLeft, cutTop - tickLen, cutLeft, cutTop + tickLen, tickPaint)
            canvas.drawLine(cutLeft - tickLen, cutTop, cutLeft + tickLen, cutTop, tickPaint)
            // Top-right
            canvas.drawLine(cutLeft + CUT_W, cutTop - tickLen, cutLeft + CUT_W, cutTop + tickLen, tickPaint)
            canvas.drawLine(cutLeft + CUT_W - tickLen, cutTop, cutLeft + CUT_W + tickLen, cutTop, tickPaint)
            // Bottom-left
            canvas.drawLine(cutLeft, cutTop + CUT_H - tickLen, cutLeft, cutTop + CUT_H + tickLen, tickPaint)
            canvas.drawLine(cutLeft - tickLen, cutTop + CUT_H, cutLeft + tickLen, cutTop + CUT_H, tickPaint)
            // Bottom-right
            canvas.drawLine(cutLeft + CUT_W, cutTop + CUT_H - tickLen, cutLeft + CUT_W, cutTop + CUT_H + tickLen, tickPaint)
            canvas.drawLine(cutLeft + CUT_W - tickLen, cutTop + CUT_H, cutLeft + CUT_W + tickLen, cutTop + CUT_H, tickPaint)

            // Card background + solid border
            val cardLeft = cutLeft + CUT_BLEED_PT
            val cardTop = cutTop + CUT_BLEED_PT
            val cardRect = RectF(cardLeft, cardTop, cardLeft + CARD_W_PT, cardTop + CARD_H_PT)

            val cardBg = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val cardBorder = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(cardRect, 5f, 5f, cardBg)
            canvas.drawRoundRect(cardRect, 5f, 5f, cardBorder)

            // Draw card content inside the card rect
            drawCardContent(canvas, cardLeft, cardTop)
        }

        private fun drawCardContent(canvas: Canvas, cardLeft: Float, cardTop: Float) {
            val centerX = cardLeft + CARD_W_PT / 2f
            val mutedColor = 0xFF6B7280.toInt()
            val textColor = 0xFF111827.toInt()
            var y = cardTop + CARD_MARGIN_PT + 12f

            // QR placeholder
            val qrSize = 48f
            val qrLeft = cardLeft + (CARD_W_PT - qrSize) / 2f
            val qrBg = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            val qrBorder = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 0.7f }
            canvas.drawRoundRect(RectF(qrLeft, y, qrLeft + qrSize, y + qrSize), 3f, 3f, qrBg)
            canvas.drawRoundRect(RectF(qrLeft, y, qrLeft + qrSize, y + qrSize), 3f, 3f, qrBorder)
            val qrText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor; textSize = 6.5f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
            }
            canvas.drawText("QR CODE", centerX, y + qrSize / 2f + 2.2f, qrText)
            y += qrSize + 7f

            // Event Name (optional)
            if (data.visibleFields.contains(FIELD_EVENT_NAME) && data.eventName.isNotBlank()) {
                y = drawLabel(canvas, "EVENT NAME", mutedColor, centerX, y)
                y = drawValue(canvas, data.eventName.uppercase(), 7.5f, textColor, centerX, y)
                y += 3f
            }

            // Attendee Name (locked)
            y = drawLabel(canvas, "ATTENDEE NAME", mutedColor, centerX, y)
            y = drawValue(canvas, data.attendeeName, 10f, textColor, centerX, y)
            y += 2f

            // Role (optional)
            if (data.visibleFields.contains(FIELD_ROLE) && data.role.isNotBlank()) {
                y = drawLabel(canvas, "ROLE", mutedColor, centerX, y)
                y = drawValue(canvas, data.role, 6.5f, textColor, centerX, y)
                y += 2f
            }

            // Attendee ID (optional)
            if (data.visibleFields.contains(FIELD_ATTENDEE_ID) && data.registrationNumber != null) {
                val formatted = RegistrationNumberFormatter.format(data.registrationNumber) ?: "N/A"
                y = drawLabel(canvas, "ATTENDEE ID", mutedColor, centerX, y)
                y = drawValue(canvas, formatted, 6f, textColor, centerX, y)
                y += 2f
            }

            // Event Date (optional)
            if (data.visibleFields.contains(FIELD_EVENT_DATE) && data.eventDate.isNotBlank()) {
                y = drawLabel(canvas, "EVENT DATE", mutedColor, centerX, y)
                y = drawValue(canvas, data.eventDate, 6f, textColor, centerX, y)
            }
        }

        private fun drawLabel(canvas: Canvas, text: String, color: Int, centerX: Float, y: Float): Float {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; textSize = 4.8f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
            }
            canvas.drawText(text.uppercase(), centerX, y + 4.8f, paint)
            return y + 6.5f
        }

        private fun drawValue(canvas: Canvas, text: String, sizeSp: Float, color: Int, centerX: Float, y: Float): Float {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; textSize = sizeSp; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
            }
            val truncated = if (paint.measureText(text) > CARD_INNER_W) {
                var end = text.length
                while (end > 0 && paint.measureText(text.substring(0, end) + "...") > CARD_INNER_W) end--
                text.substring(0, end) + "..."
            } else text
            canvas.drawText(truncated, centerX, y + sizeSp, paint)
            return y + sizeSp + 2.5f
        }
    }
}
