package com.finora.domain.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.finora.data.local.BookEntity
import com.finora.data.local.CashEntryEntity
import com.finora.data.local.PersonalExpenseEntity
import com.finora.domain.mess.MemberSettlement
import com.finora.domain.time.dateLabel
import com.finora.domain.time.monthLabel
import java.io.File

data class MessReportRecord(
    val date: Long,
    val member: String,
    val type: String,
    val amount: Long = 0,
    val units: Double = 0.0,
    val note: String = ""
)

object ReportExporter {
    fun shareCsv(
        context: Context,
        book: BookEntity,
        monthTimestamp: Long,
        entries: List<CashEntryEntity>,
        settlements: List<MemberSettlement>,
        messRecords: List<MessReportRecord>
    ) {
        val reportDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportDir, "finora-${monthTimestamp}-${book.id}.csv")
        file.bufferedWriter().use { writer ->
            writer.appendLine(csv(listOf("Finora", book.name, monthLabel(monthTimestamp))))
            writer.appendLine(csv(listOf("type", "date", "category", "amount (BDT)", "note")))
            entries.forEach { entry ->
                writer.appendLine(
                    csv(
                        listOf(
                            if (entry.type == "IN") "Cash In" else "Cash Out",
                            dateLabel(entry.date),
                            entry.category,
                            String.format("%.2f", entry.amount / 100.0),
                            entry.note
                        )
                    )
                )
            }
            writer.appendLine()
            writer.appendLine(csv(listOf("Shared mess ledger", "date", "member", "type", "amount (BDT)", "meal units", "note")))
            messRecords.forEach { row ->
                writer.appendLine(
                    csv(
                        listOf(
                            "Mess record",
                            dateLabel(row.date),
                            row.member,
                            row.type,
                            if (row.amount > 0) String.format("%.2f", row.amount / 100.0) else "",
                            if (row.units > 0) row.units.toString() else "",
                            row.note
                        )
                    )
                )
            }
            writer.appendLine()
            writer.appendLine(csv(listOf("Member statement", "Meals", "Meal cost (BDT)", "Deposits (BDT)", "Bazar spent (BDT)", "Balance (BDT)")))
            settlements.forEach { row ->
                writer.appendLine(
                    csv(
                        listOf(
                            row.memberName,
                            row.meals.toString(),
                            String.format("%.2f", row.mealCost / 100.0),
                            String.format("%.2f", row.deposits / 100.0),
                            String.format("%.2f", row.bazarSpending / 100.0),
                            String.format("%.2f", row.balance / 100.0)
                        )
                    )
                )
            }
        }
        share(context, file, "text/csv", "Finora monthly report")
    }

    fun sharePdf(
        context: Context,
        book: BookEntity,
        monthTimestamp: Long,
        income: Long,
        expenses: Long,
        settlements: List<MemberSettlement>
    ) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 23f; isFakeBoldText = true; color = 0xFF17243B.toInt() }
        val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 13f; color = 0xFF33435D.toInt() }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF087E8B.toInt(); strokeWidth = 2f }
        canvas.drawText("Finora · ${book.name}", 42f, 58f, heading)
        canvas.drawText(monthLabel(monthTimestamp), 42f, 84f, normal)
        canvas.drawLine(42f, 104f, 553f, 104f, accent)
        canvas.drawText("Cash in: ${formatBdt(income)}", 42f, 137f, normal)
        canvas.drawText("Cash out: ${formatBdt(expenses)}", 42f, 160f, normal)
        canvas.drawText("Net movement: ${formatBdt(income - expenses)}", 42f, 183f, normal)
        canvas.drawText("Mess member statement", 42f, 228f, heading.apply { textSize = 16f })
        var y = 258f
        canvas.drawText("Member", 42f, y, normal)
        canvas.drawText("Meals", 250f, y, normal)
        canvas.drawText("Share", 330f, y, normal)
        canvas.drawText("Gets back / owes", 422f, y, normal)
        y += 22f
        settlements.take(20).forEach { member ->
            val amount = formatBdt(kotlin.math.abs(member.balance))
            val status = if (member.balance >= 0) "Gets back" else "Owes"
            canvas.drawText(member.memberName.take(25), 42f, y, normal)
            canvas.drawText(member.meals.toString(), 250f, y, normal)
            canvas.drawText(formatBdt(member.mealCost), 330f, y, normal)
            canvas.drawText("$status $amount", 422f, y, normal)
            y += 21f
        }
        if (settlements.size > 20) canvas.drawText("Additional members are included in the CSV report.", 42f, y + 8f, normal)
        document.finishPage(page)
        val reportDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportDir, "finora-${monthTimestamp}-${book.id}.pdf")
        file.outputStream().use(document::writeTo)
        document.close()
        share(context, file, "application/pdf", "Finora monthly report")
    }

    private fun share(context: Context, file: File, mime: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun csv(cells: List<String>): String = cells.joinToString(",") { cell ->
        "\"${cell.replace("\"", "\"\"").replace("\n", " ")}\""
    }

    private fun formatBdt(amount: Long): String = "BDT %.2f".format(amount / 100.0)
}
