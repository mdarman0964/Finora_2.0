package com.finora.domain.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.finora.data.local.BookEntity
import com.finora.data.local.CashEntryEntity
import com.finora.data.local.NoteEntity
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
    fun sharePersonalPdf(
        context: Context,
        monthTimestamp: Long,
        entries: List<CashEntryEntity>,
        income: Long,
        expenses: Long
    ) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 22f; isFakeBoldText = true; color = 0xFF142D46.toInt() }
        val subHeading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f; isFakeBoldText = true; color = 0xFF28335C.toInt() }
        val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = 0xFF33435D.toInt() }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF087E8B.toInt(); strokeWidth = 2f }

        canvas.drawText("Finora · Personal Report (পার্সোনাল রিপোর্ট)", 42f, 54f, heading)
        canvas.drawText(monthLabel(monthTimestamp), 42f, 78f, normal)
        canvas.drawLine(42f, 96f, 553f, 96f, accent)

        canvas.drawText("Total Cash In: ${formatBdt(income)}", 42f, 126f, normal)
        canvas.drawText("Total Cash Out: ${formatBdt(expenses)}", 42f, 146f, normal)
        canvas.drawText("Net Balance: ${formatBdt(income - expenses)}", 42f, 166f, normal)

        canvas.drawText("Personal Transactions (${entries.size})", 42f, 206f, subHeading)
        var y = 232f
        canvas.drawText("Date", 42f, y, subHeading.apply { textSize = 12f })
        canvas.drawText("Type", 130f, y, subHeading)
        canvas.drawText("Category", 220f, y, subHeading)
        canvas.drawText("Amount", 370f, y, subHeading)
        canvas.drawText("Note", 460f, y, subHeading)
        y += 18f

        entries.take(24).forEach { entry ->
            canvas.drawText(dateLabel(entry.date), 42f, y, normal)
            canvas.drawText(if (entry.type == "IN") "Cash In" else "Cash Out", 130f, y, normal)
            canvas.drawText(entry.category.take(18), 220f, y, normal)
            canvas.drawText(formatBdt(entry.amount), 370f, y, normal)
            canvas.drawText(entry.note.take(15), 460f, y, normal)
            y += 20f
        }
        if (entries.size > 24) {
            canvas.drawText("... and ${entries.size - 24} more entries.", 42f, y + 8f, normal)
        }

        document.finishPage(page)
        val file = saveAndGetFile(context, "finora-personal-${monthTimestamp}.pdf", document)
        share(context, file, "application/pdf", "Finora Personal Monthly Report")
    }

    fun shareBusinessPdf(
        context: Context,
        monthTimestamp: Long,
        entries: List<CashEntryEntity>,
        revenue: Long,
        expense: Long
    ) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 22f; isFakeBoldText = true; color = 0xFF142D46.toInt() }
        val subHeading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f; isFakeBoldText = true; color = 0xFF28335C.toInt() }
        val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = 0xFF33435D.toInt() }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF7C4DFF.toInt(); strokeWidth = 2f }

        canvas.drawText("Finora · Business Report (বিজনেস রিপোর্ট)", 42f, 54f, heading)
        canvas.drawText(monthLabel(monthTimestamp), 42f, 78f, normal)
        canvas.drawLine(42f, 96f, 553f, 96f, accent)

        canvas.drawText("Total Sales & Revenue: ${formatBdt(revenue)}", 42f, 126f, normal)
        canvas.drawText("Total Operating Expenses: ${formatBdt(expense)}", 42f, 146f, normal)
        val profit = revenue - expense
        canvas.drawText("Net Profit / Margin: ${formatBdt(profit)}", 42f, 166f, normal)

        canvas.drawText("Business Transactions (${entries.size})", 42f, 206f, subHeading)
        var y = 232f
        canvas.drawText("Date", 42f, y, subHeading.apply { textSize = 12f })
        canvas.drawText("Type", 130f, y, subHeading)
        canvas.drawText("Category", 220f, y, subHeading)
        canvas.drawText("Amount", 370f, y, subHeading)
        canvas.drawText("Note / Party", 460f, y, subHeading)
        y += 18f

        entries.take(24).forEach { entry ->
            canvas.drawText(dateLabel(entry.date), 42f, y, normal)
            canvas.drawText(if (entry.type == "IN") "Revenue" else "Expense", 130f, y, normal)
            canvas.drawText(entry.category.take(18), 220f, y, normal)
            canvas.drawText(formatBdt(entry.amount), 370f, y, normal)
            canvas.drawText(entry.note.take(15), 460f, y, normal)
            y += 20f
        }
        if (entries.size > 24) {
            canvas.drawText("... and ${entries.size - 24} more entries.", 42f, y + 8f, normal)
        }

        document.finishPage(page)
        val file = saveAndGetFile(context, "finora-business-${monthTimestamp}.pdf", document)
        share(context, file, "application/pdf", "Finora Business Monthly Report")
    }

    fun shareBachelorPdf(
        context: Context,
        monthTimestamp: Long,
        settlements: List<MemberSettlement>,
        totalBazar: Long,
        totalMeals: Double,
        mealRate: Double
    ) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 22f; isFakeBoldText = true; color = 0xFF142D46.toInt() }
        val subHeading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f; isFakeBoldText = true; color = 0xFF28335C.toInt() }
        val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = 0xFF33435D.toInt() }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF087E8B.toInt(); strokeWidth = 2f }

        canvas.drawText("Finora · Bachelor Mess Report (ব্যাচেলর মেস রিপোর্ট)", 42f, 54f, heading)
        canvas.drawText(monthLabel(monthTimestamp), 42f, 78f, normal)
        canvas.drawLine(42f, 96f, 553f, 96f, accent)

        canvas.drawText("Total Shared Bazar: ${formatBdt(totalBazar)}", 42f, 126f, normal)
        canvas.drawText("Total Meals: ${"%.1f".format(totalMeals)}", 42f, 146f, normal)
        val rateCurrency = if (java.util.Locale.getDefault().language == "bn") "BDT" else "$"
        canvas.drawText("Calculated Meal Rate: $rateCurrency %.2f".format(mealRate), 42f, 166f, normal)

        canvas.drawText("Mess Member Statement", 42f, 206f, subHeading)
        var y = 232f
        canvas.drawText("Member", 42f, y, subHeading.apply { textSize = 12f })
        canvas.drawText("Meals", 180f, y, subHeading)
        canvas.drawText("Meal Cost", 250f, y, subHeading)
        canvas.drawText("Deposit", 340f, y, subHeading)
        canvas.drawText("Gets back / Owes", 430f, y, subHeading)
        y += 18f

        settlements.take(24).forEach { member ->
            val status = if (member.balance >= 0) "Gets back" else "Owes"
            val balAmount = formatBdt(kotlin.math.abs(member.balance))
            canvas.drawText(member.memberName.take(20), 42f, y, normal)
            canvas.drawText(member.meals.toString(), 180f, y, normal)
            canvas.drawText(formatBdt(member.mealCost), 250f, y, normal)
            canvas.drawText(formatBdt(member.deposits), 340f, y, normal)
            canvas.drawText("$status $balAmount", 430f, y, normal)
            y += 20f
        }

        document.finishPage(page)
        val file = saveAndGetFile(context, "finora-bachelor-${monthTimestamp}.pdf", document)
        share(context, file, "application/pdf", "Finora Bachelor Mess Monthly Report")
    }

    fun shareNotebookPdf(
        context: Context,
        monthTimestamp: Long,
        notes: List<NoteEntity>
    ) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 22f; isFakeBoldText = true; color = 0xFF142D46.toInt() }
        val subHeading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f; isFakeBoldText = true; color = 0xFF28335C.toInt() }
        val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = 0xFF33435D.toInt() }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF00E5FF.toInt(); strokeWidth = 2f }

        canvas.drawText("Finora · Financial Notebook Report (নোটবুক রিপোর্ট)", 42f, 54f, heading)
        canvas.drawText(monthLabel(monthTimestamp), 42f, 78f, normal)
        canvas.drawLine(42f, 96f, 553f, 96f, accent)

        val totalAmount = notes.mapNotNull { it.amount }.sum()
        canvas.drawText("Total Notes Recorded: ${notes.size}", 42f, 126f, normal)
        if (totalAmount > 0) {
            canvas.drawText("Total Tracked Amounts / Dues: ${formatBdt(totalAmount)}", 42f, 146f, normal)
        }

        canvas.drawText("Notes & Memos", 42f, 186f, subHeading)
        var y = 212f
        canvas.drawText("Date", 42f, y, subHeading.apply { textSize = 12f })
        canvas.drawText("Category", 120f, y, subHeading)
        canvas.drawText("Title", 220f, y, subHeading)
        canvas.drawText("Amount", 370f, y, subHeading)
        canvas.drawText("Details", 460f, y, subHeading)
        y += 18f

        notes.take(24).forEach { note ->
            canvas.drawText(dateLabel(note.date), 42f, y, normal)
            canvas.drawText(note.category.take(14), 120f, y, normal)
            canvas.drawText(note.title.take(18), 220f, y, normal)
            canvas.drawText(if (note.amount != null && note.amount > 0) formatBdt(note.amount) else "—", 370f, y, normal)
            canvas.drawText(note.content.take(15), 460f, y, normal)
            y += 20f
        }

        document.finishPage(page)
        val file = saveAndGetFile(context, "finora-notebook-${monthTimestamp}.pdf", document)
        share(context, file, "application/pdf", "Finora Notebook Monthly Report")
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
        val file = saveAndGetFile(context, "finora-${monthTimestamp}-${book.id}.pdf", document)
        share(context, file, "application/pdf", "Finora monthly report")
    }

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
        val currCode = if (java.util.Locale.getDefault().language == "bn") "BDT" else "USD"
        file.bufferedWriter().use { writer ->
            writer.appendLine(csv(listOf("Finora", book.name, monthLabel(monthTimestamp))))
            writer.appendLine(csv(listOf("type", "date", "category", "amount ($currCode)", "note", "section")))
            entries.forEach { entry ->
                writer.appendLine(
                    csv(
                        listOf(
                            if (entry.type == "IN") "Cash In" else "Cash Out",
                            dateLabel(entry.date),
                            entry.category,
                            String.format("%.2f", entry.amount / 100.0),
                            entry.note,
                            entry.section
                        )
                    )
                )
            }
            writer.appendLine()
            writer.appendLine(csv(listOf("Shared mess ledger", "date", "member", "type", "amount ($currCode)", "meal units", "note")))
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
            writer.appendLine(csv(listOf("Member statement", "Meals", "Meal cost ($currCode)", "Deposits ($currCode)", "Bazar spent ($currCode)", "Balance ($currCode)")))
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

    private fun saveAndGetFile(context: Context, filename: String, document: PdfDocument): File {
        val reportDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportDir, filename)
        file.outputStream().use(document::writeTo)
        document.close()
        return file
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

    private fun formatBdt(amount: Long): String {
        val isBangla = java.util.Locale.getDefault().language == "bn"
        return if (isBangla) "BDT %.2f".format(amount / 100.0) else "$ %.2f".format(amount / 100.0)
    }
}
