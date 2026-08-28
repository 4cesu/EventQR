package com.thedavelopers.eventqr.features.idprinting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.thedavelopers.eventqr.features.registrations.RegistrationNumberFormatter
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Renders CR80-ratio ID cards onto an A4 bond-paper page and opens the system
 * print dialog via [PrintManager].
 *
 * Layout proportions are driven by [IdCardLayoutConfig] so both the print
 * output and the Organizer Preview remain in sync.
 *
 * Rendering strategy: each card's content is rasterised onto an offscreen
 * bitmap at [IdCardLayoutConfig.RENDER_SCALE]× to produce crisp text when the
 * PDF is printed, then composited onto the final PDF page at its physical size.
 *
 * Multi-up: a card grid can be drawn (see [UpCount]) with thin dashed cut
 * guides and corner crop marks around every cell so cards can be cut apart.
 * Cells beyond the supplied data list are left blank and never duplicated.
 */
object AndroidIdPrinter {

    private const val FIELD_ATTENDEE_ID = IdCardLayoutConfig.FIELD_ATTENDEE_ID
    private const val FIELD_ROLE = IdCardLayoutConfig.FIELD_ROLE
    private const val FIELD_EVENT_NAME = IdCardLayoutConfig.FIELD_EVENT_NAME
    private const val FIELD_EVENT_DATE = IdCardLayoutConfig.FIELD_EVENT_DATE

    /** Common sheet layouts. One physical sheet = copies cards (rows × cols). */
    enum class UpCount(val cols: Int, val rows: Int, val copies: Int, val label: String) {
        ONE(1, 1, 1, "1-up"),
        TWO(1, 2, 2, "2-up"),
        FOUR(2, 2, 4, "4-up"),
        EIGHT(2, 4, 8, "8-up");

        companion object {
            fun fromLabel(label: String): UpCount = entries.firstOrNull { it.label == label } ?: ONE
        }
    }

    data class CardData(
        val attendeeName: String,
        val eventName: String = "",
        val registrationNumber: Int? = null,
        val role: String = "",
        val eventDate: String = "",
        val visibleFields: List<String> = emptyList(),
        val qrValue: String = "",
    )

    fun print(context: Context, jobName: String, data: CardData, upCount: UpCount = UpCount.ONE) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(jobName, IdCardPrintAdapter(data, upCount), null)
    }

    /**
     * Renders a whole A4 sheet (grid) into a preview [Bitmap] for a print
     * confirmation dialog. The same attendee fills every cell to approximate
     * what a reprint-single-attendee sheet will look like.
     */
    fun renderGridPreview(data: CardData, upCount: UpCount, targetWidthPx: Int): Bitmap {
        val pageScale = targetWidthPx.toFloat() / IdCardLayoutConfig.PAGE_W_PT
        val targetHeight = (IdCardLayoutConfig.PAGE_H_PT * pageScale).toInt()
        val bitmap = Bitmap.createBitmap(targetWidthPx, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.scale(pageScale, pageScale)
        val cards = List(upCount.copies) { data }
        drawPageGrid(canvas, cards, upCount)
        return bitmap
    }

    // ------------------------------------------------------------------
    // Physical dimensions derived from IdCardLayoutConfig
    // ------------------------------------------------------------------

    private val cardW = IdCardLayoutConfig.CR80_WIDTH_IN * 72f // 153pt
    private val cardH = IdCardLayoutConfig.CR80_HEIGHT_IN * 72f // 243pt
    private val margin = cardW * IdCardLayoutConfig.MARGIN_RATIO // ~10.2pt
    private val innerW = cardW - 2 * margin // ~129pt
    private val bleed = cardW * IdCardLayoutConfig.CUT_BLEED_RATIO // ~6pt

    // Font sizes derived from ratios × card height
    private val labelFont = cardH * IdCardLayoutConfig.LABEL_FONT_RATIO
    private val nameFont = cardH * IdCardLayoutConfig.NAME_FONT_RATIO
    private val eventNameFont = cardH * IdCardLayoutConfig.EVENT_NAME_FONT_RATIO
    private val roleFont = cardH * IdCardLayoutConfig.ROLE_FONT_RATIO
    private val idFont = cardH * IdCardLayoutConfig.ID_FONT_RATIO

    // Spacing derived from ratios × card height
    private val qrSpacing = cardH * IdCardLayoutConfig.QR_SPACING_RATIO
    private val fieldSpacing = cardH * IdCardLayoutConfig.FIELD_SPACING_RATIO
    private val qrSize = cardW * IdCardLayoutConfig.QR_SIZE_RATIO

    private val textMuted = IdCardLayoutConfig.COLOR_MUTED
    private val textDark = IdCardLayoutConfig.COLOR_TEXT
    private val lineLight = IdCardLayoutConfig.COLOR_BORDER_LIGHT

    /** Page margin / cell gap (points) used to centre the grid on A4. */
    private const val PAGE_MARGIN_PT = 30f
    private const val CELL_GAP_PT = 16f

    // ------------------------------------------------------------------
    // Grid layout
    // ------------------------------------------------------------------

    private class GridLayout(
        val cols: Int,
        val rows: Int,
        val scale: Float,
        val originX: Float,
        val originY: Float,
        val gapX: Float,
        val gapY: Float,
        val cellW: Float,
        val cellH: Float,
    ) {
        fun cellLeft(col: Int) = originX + col * (cellW + gapX)
        fun cellTop(row: Int) = originY + row * (cellH + gapY)
    }

    /**
     * Computes the largest card scale that fits [cols] × [rows] within the A4
     * page (bounded by [PAGE_MARGIN_PT] and separated by [CELL_GAP_PT]) while
     * never scaling a card above its native CR80 size.
     */
    private fun computeLayout(cols: Int, rows: Int): GridLayout {
        val pageW = IdCardLayoutConfig.PAGE_W_PT
        val pageH = IdCardLayoutConfig.PAGE_H_PT
        val availW = pageW - 2 * PAGE_MARGIN_PT
        val availH = pageH - 2 * PAGE_MARGIN_PT
        val scaleW = (availW - CELL_GAP_PT * (cols - 1)) / (cols * cardW)
        val scaleH = (availH - CELL_GAP_PT * (rows - 1)) / (rows * cardH)
        val scale = min(scaleW, min(scaleH, 1f))
        val cellW = cardW * scale
        val cellH = cardH * scale
        val gridW = cols * cellW + (cols - 1) * CELL_GAP_PT
        val gridH = rows * cellH + (rows - 1) * CELL_GAP_PT
        return GridLayout(
            cols = cols,
            rows = rows,
            scale = scale,
            originX = (pageW - gridW) / 2f,
            originY = (pageH - gridH) / 2f,
            gapX = CELL_GAP_PT,
            gapY = CELL_GAP_PT,
            cellW = cellW,
            cellH = cellH,
        )
    }

    // ------------------------------------------------------------------
    // Print adapter
    // ------------------------------------------------------------------

    private class IdCardPrintAdapter(
        private val data: CardData,
        private val upCount: UpCount,
    ) : PrintDocumentAdapter() {

        override fun onWrite(
            pages: Array<PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback,
        ) {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                IdCardLayoutConfig.PAGE_W_PT,
                IdCardLayoutConfig.PAGE_H_PT,
                1,
            ).create()
            val page = doc.startPage(pageInfo)
            val cards = List(upCount.copies) { data }
            drawPageGrid(page.canvas, cards, upCount)
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
    }

    // ------------------------------------------------------------------
    // Page composition: grid of cut guides + high-res card bitmaps
    // ------------------------------------------------------------------

    /**
     * Draws every cell (cut guides always, card content only for non-null
     * entries). Cells with no data are left blank — never duplicated/stretched.
     */
    private fun drawPageGrid(canvas: Canvas, cards: List<CardData?>, upCount: UpCount) {
        val layout = computeLayout(upCount.cols, upCount.rows)
        for (row in 0 until layout.rows) {
            for (col in 0 until layout.cols) {
                val index = row * layout.cols + col
                val cellLeft = layout.cellLeft(col)
                val cellTop = layout.cellTop(row)
                drawCellGuides(canvas, cellLeft, cellTop, layout.cellW, layout.cellH)

                val data = cards.getOrNull(index) ?: continue
                val bitmap = renderCardBitmap(data)
                canvas.drawBitmap(
                    bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    RectF(cellLeft, cellTop, cellLeft + layout.cellW, cellTop + layout.cellH),
                    null,
                )
                bitmap.recycle()
            }
        }
    }

    private fun renderCardBitmap(data: CardData): Bitmap {
        val scale = IdCardLayoutConfig.RENDER_SCALE.toFloat()
        val bmpW = (cardW * scale).toInt()
        val bmpH = (cardH * scale).toInt()
        val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val offCanvas = Canvas(bitmap)
        offCanvas.scale(scale, scale)
        drawCardContent(offCanvas, data)
        return bitmap
    }

    // ------------------------------------------------------------------
    // Per-cell cut guides (drawn at PDF native resolution — thin strokes only)
    // ------------------------------------------------------------------

    private fun drawCellGuides(canvas: Canvas, cellLeft: Float, cellTop: Float, cellW: Float, cellH: Float) {
        val cutLeft = cellLeft - bleed
        val cutTop = cellTop - bleed
        val cutRight = cellLeft + cellW + bleed
        val cutBottom = cellTop + cellH + bleed

        val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineLight
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }
        canvas.drawRect(cutLeft, cutTop, cutRight, cutBottom, dashPaint)

        val tickLen = 8f
        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineLight
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        canvas.drawLine(cutLeft, cutTop - tickLen, cutLeft, cutTop + tickLen, tickPaint)
        canvas.drawLine(cutLeft - tickLen, cutTop, cutLeft + tickLen, cutTop, tickPaint)
        canvas.drawLine(cutRight, cutTop - tickLen, cutRight, cutTop + tickLen, tickPaint)
        canvas.drawLine(cutRight - tickLen, cutTop, cutRight + tickLen, cutTop, tickPaint)
        canvas.drawLine(cutLeft, cutBottom - tickLen, cutLeft, cutBottom + tickLen, tickPaint)
        canvas.drawLine(cutLeft - tickLen, cutBottom, cutLeft + tickLen, cutBottom, tickPaint)
        canvas.drawLine(cutRight, cutBottom - tickLen, cutRight, cutBottom + tickLen, tickPaint)
        canvas.drawLine(cutRight - tickLen, cutBottom, cutRight + tickLen, cutBottom, tickPaint)
    }

    // ------------------------------------------------------------------
    // Card content (drawn onto the scaled offscreen canvas at full CR80 size)
    // ------------------------------------------------------------------

    private fun drawCardContent(canvas: Canvas, data: CardData) {
        val cardBg = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorder = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRoundRect(RectF(0f, 0f, cardW, cardH), 5f, 5f, cardBg)
        canvas.drawRoundRect(RectF(0f, 0f, cardW, cardH), 5f, 5f, cardBorder)

        val centerX = cardW / 2f
        var y = margin

        val qrLeft = (cardW - qrSize) / 2f
        if (data.qrValue.isNotBlank()) {
            val qrBitmap = renderQrBitmap(data.qrValue, (qrSize * IdCardLayoutConfig.RENDER_SCALE).toInt())
            val src = Rect(0, 0, qrBitmap.width, qrBitmap.height)
            canvas.drawBitmap(qrBitmap, src, RectF(qrLeft, y, qrLeft + qrSize, y + qrSize), null)
            qrBitmap.recycle()
        } else {
            val qrBg = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            val qrBorder = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 0.7f }
            canvas.drawRoundRect(RectF(qrLeft, y, qrLeft + qrSize, y + qrSize), 3f, 3f, qrBg)
            canvas.drawRoundRect(RectF(qrLeft, y, qrLeft + qrSize, y + qrSize), 3f, 3f, qrBorder)
            val qrText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textDark; textSize = idFont; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
            }
            canvas.drawText("QR CODE", centerX, y + qrSize / 2f + idFont / 2f, qrText)
        }
        y += qrSize + qrSpacing

        if (data.visibleFields.contains(FIELD_EVENT_NAME) && data.eventName.isNotBlank()) {
            y = drawLabel(canvas, "EVENT NAME", centerX, y)
            y = drawValue(canvas, data.eventName.uppercase(), eventNameFont, centerX, y)
            y += fieldSpacing
        }

        y = drawLabel(canvas, "ATTENDEE NAME", centerX, y)
        y = drawValue(canvas, data.attendeeName, nameFont, centerX, y)
        y += fieldSpacing

        if (data.visibleFields.contains(FIELD_ROLE) && data.role.isNotBlank()) {
            y = drawLabel(canvas, "ROLE", centerX, y)
            y = drawValue(canvas, data.role, roleFont, centerX, y)
            y += fieldSpacing
        }

        if (data.visibleFields.contains(FIELD_ATTENDEE_ID) && data.registrationNumber != null) {
            val formatted = RegistrationNumberFormatter.format(data.registrationNumber) ?: "N/A"
            y = drawLabel(canvas, "ATTENDEE ID", centerX, y)
            y = drawValue(canvas, formatted, idFont, centerX, y)
            y += fieldSpacing
        }

        if (data.visibleFields.contains(FIELD_EVENT_DATE) && data.eventDate.isNotBlank()) {
            y = drawLabel(canvas, "EVENT DATE", centerX, y)
            y = drawValue(canvas, data.eventDate, idFont, centerX, y)
        }
    }

    // ------------------------------------------------------------------
    // Drawing helpers
    // ------------------------------------------------------------------

    private fun drawLabel(canvas: Canvas, text: String, centerX: Float, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textMuted; textSize = labelFont; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text.uppercase(), centerX, y + labelFont, paint)
        return y + labelFont + labelFont * 0.3f
    }

    private fun drawValue(canvas: Canvas, text: String, size: Float, centerX: Float, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark; textSize = size; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        val truncated = if (paint.measureText(text) > innerW) {
            var end = text.length
            while (end > 0 && paint.measureText(text.substring(0, end) + "...") > innerW) end--
            text.substring(0, end) + "..."
        } else text
        canvas.drawText(truncated, centerX, y + size, paint)
        return y + size + size * 0.25f
    }

    private fun renderQrBitmap(value: String, pixelSize: Int): Bitmap {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q)
        val matrix = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, pixelSize, pixelSize, hints)
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until matrix.width) {
            for (row in 0 until matrix.height) {
                bitmap.setPixel(x, row, if (matrix[x, row]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
