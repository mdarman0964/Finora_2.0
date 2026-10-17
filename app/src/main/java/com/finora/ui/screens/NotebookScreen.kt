package com.finora.ui.screens

import android.content.ActivityNotFoundException
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.R
import com.finora.data.local.NoteEntity
import com.finora.domain.export.ReportExporter
import com.finora.domain.time.dateLabel
import com.finora.domain.time.monthBounds
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.FinoraCard
import com.finora.ui.components.categoryDisplayName
import com.finora.ui.components.formatAmount
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun NotebookScreen(
    viewModel: FinoraViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()

    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }

    val categories = listOf("All", "Budget", "Dues", "Memo", "General")
    val now = System.currentTimeMillis()
    val monthBounds = monthBounds(now)
    val thisMonthNotes = notes.filter { it.date in monthBounds }

    val visibleNotes = notes.filter { note ->
        val matchesCategory = selectedCategory == "All" || note.category.equals(selectedCategory, ignoreCase = true)
        val query = search.trim()
        val matchesSearch = query.isBlank() || note.title.contains(query, true) || note.content.contains(query, true) || note.category.contains(query, true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    editingNote = null
                    showEditor = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_note")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_note))
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.open_menu))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(R.string.section_notebook),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(stringResource(R.string.notebook_title), style = MaterialTheme.typography.headlineSmall)
                    }
                }
                IconButton(
                    onClick = { editingNote = null; showEditor = true },
                    modifier = Modifier.size(44.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_note), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        item {
            FinoraCard {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val countStr = if (banglaDigits) com.finora.domain.time.toBanglaDigits("${notes.size}") else "${notes.size}"
                        Text("$countStr ${stringResource(R.string.section_notebook)}", style = MaterialTheme.typography.titleMedium)
                    }
                    Button(
                        onClick = {
                            try {
                                ReportExporter.shareNotebookPdf(context, now, thisMonthNotes)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, R.string.share_unavailable, Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.export_pdf), modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_entries)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(if (cat == "All") stringResource(R.string.filter_all) else categoryDisplayName(cat)) }
                    )
                }
            }
        }

        if (visibleNotes.isEmpty()) {
            item {
                FinoraCard {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(if (search.isBlank()) R.string.empty_notes_title else R.string.no_results), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(if (search.isBlank()) R.string.empty_notes_body else R.string.try_another_search), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (search.isBlank()) {
                            Button(onClick = { editingNote = null; showEditor = true }) { Text(stringResource(R.string.add_note)) }
                        }
                    }
                }
            }
        } else {
            items(visibleNotes, key = { it.id }) { note ->
                NoteCard(note, banglaDigits) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    editingNote = note
                    showEditor = true
                }
            }
        }
    }
    }

    if (showEditor) {
        NoteEditorDialog(
            note = editingNote,
            onDismiss = { showEditor = false; editingNote = null },
            onSave = { updated ->
                viewModel.saveNote(updated)
                showEditor = false
                editingNote = null
            },
            onDelete = { id ->
                viewModel.deleteNote(id)
                showEditor = false
                editingNote = null
            }
        )
    }
}

@Composable
private fun NoteCard(note: NoteEntity, banglaDigits: Boolean, onClick: () -> Unit) {
    FinoraCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (note.amount != null && note.amount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            formatAmount(note.amount, banglaDigits),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            if (note.content.isNotBlank()) {
                Text(note.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(categoryDisplayName(note.category), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(dateLabel(note.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun NoteEditorDialog(
    note: NoteEntity?,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    onDelete: (Int) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "General") }
    var amountStr by remember { mutableStateOf(note?.amount?.let { BigDecimal(it).divide(BigDecimal(100)).stripTrailingZeros().toPlainString() } ?: "") }
    var categoryMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categories = listOf("Budget", "Dues", "Memo", "General")
    val parsedAmount = amountStr.trim().replace(",", "").toBigDecimalOrNull()
        ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (note == null) R.string.add_note else R.string.edit_note)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.note_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val currSymbol = if (java.util.Locale.getDefault().language == "bn") "৳" else "$"
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("${stringResource(R.string.amount_bdt)} (${stringResource(R.string.note_optional)})") },
                    leadingIcon = { Text(currSymbol, modifier = Modifier.padding(start = 8.dp), color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Column {
                    Text(stringResource(R.string.category), style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(categoryDisplayName(category))
                    }
                    DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(categoryDisplayName(cat)) }, onClick = { category = cat; categoryMenu = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(stringResource(R.string.note_content)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        NoteEntity(
                            id = note?.id ?: 0,
                            title = title.trim(),
                            content = content.trim(),
                            category = category,
                            amount = parsedAmount,
                            date = note?.date ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (note != null) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text(stringResource(R.string.delete_entry), color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )

    if (showDeleteConfirm && note != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_note_confirm)) },
            text = { Text(stringResource(R.string.delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { onDelete(note.id); showDeleteConfirm = false }) {
                    Text(stringResource(R.string.delete_entry), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
