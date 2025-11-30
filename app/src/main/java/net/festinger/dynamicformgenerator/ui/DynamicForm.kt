package net.festinger.dynamicformgenerator.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.festinger.dynamicformgenerator.data.FieldType
import net.festinger.dynamicformgenerator.data.FormFieldSchema
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicForm(
    schema: List<FormFieldSchema>,
    formData: MutableMap<String, Any?>,
    validationErrors: Map<String, String>,
    onDataChanged: (String, Any?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        schema.forEach { field ->
            key(field.key) {
                val currentValue = formData[field.key]
                val errorMessage = validationErrors[field.key]
                val isError = errorMessage != null

                // Add asterisk if required
                val labelText = if (field.required) "${field.label} *" else field.label

                when (field.type) {
                    FieldType.STRING, FieldType.NUMBER -> {
                        OutlinedTextField(
                            value = (currentValue as? String) ?: "",
                            onValueChange = { onDataChanged(field.key, it) },
                            label = { Text(labelText) },
                            isError = isError,
                            supportingText = if (isError) { { Text(errorMessage) } } else null,
                            keyboardOptions = if (field.type == FieldType.NUMBER)
                                KeyboardOptions(keyboardType = KeyboardType.Number)
                            else KeyboardOptions.Default,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    FieldType.BOOLEAN -> {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = (currentValue as? Boolean) ?: false,
                                    onCheckedChange = { onDataChanged(field.key, it) }
                                )
                                Text(text = labelText)
                            }
                            if (isError) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                    FieldType.ENUM_DROPDOWN -> {
                        var expanded by remember { mutableStateOf(false) }
                        val options = field.options ?: emptyList()
                        val selectedText = (currentValue as? String) ?: ""

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(labelText) },
                                isError = isError,
                                supportingText = if (isError) { { Text(errorMessage) } } else null,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            onDataChanged(field.key, option)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    FieldType.DATE -> {
                        var showDatePicker by remember { mutableStateOf(false) }
                        val datePickerState = rememberDatePickerState()
                        val selectedDateStr = (currentValue as? String) ?: ""

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showDatePicker = false
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = Date(millis)
                                            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            onDataChanged(field.key, format.format(date))
                                        }
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedDateStr,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(labelText) },
                                isError = isError,
                                supportingText = if (isError) { { Text(errorMessage) } } else null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                        }
                    }
                    FieldType.TIME -> {
                        var showTimePicker by remember { mutableStateOf(false) }
                        val timeState = rememberTimePickerState()
                        val selectedTimeStr = (currentValue as? String) ?: ""

                        if (showTimePicker) {
                            TimePickerDialog(
                                onDismissRequest = { showTimePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showTimePicker = false
                                        val formatted = String.format(Locale.getDefault(), "%02d:%02d", timeState.hour, timeState.minute)
                                        onDataChanged(field.key, formatted)
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                                }
                            ) {
                                TimePicker(state = timeState)
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedTimeStr,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(labelText) },
                                isError = isError,
                                supportingText = if (isError) { { Text(errorMessage) } } else null,
                                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
                        }
                    }
                    FieldType.MULTI_SELECT -> {
                        var showDialog by remember { mutableStateOf(false) }
                        val rawList = currentValue as? List<*>
                        val currentSelection = rawList?.filterIsInstance<String>() ?: emptyList()

                        val options = field.options ?: emptyList()

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = { TextButton(onClick = { showDialog = false }) { Text("Done") } },
                                title = { Text("Select ${field.label}") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        options.forEach { option ->
                                            val isSelected = currentSelection.contains(option)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val newList = if (isSelected) currentSelection - option else currentSelection + option
                                                        onDataChanged(field.key, newList)
                                                    }
                                                    .padding(vertical = 8.dp)
                                            ) {
                                                Checkbox(checked = isSelected, onCheckedChange = null)
                                                Text(text = option, modifier = Modifier.padding(start = 8.dp))
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = currentSelection.joinToString(", "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(labelText) },
                                isError = isError,
                                supportingText = if (isError) { { Text(errorMessage) } } else null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showDialog = true })
                        }
                    }
                    FieldType.GPS_LOCATION -> {
                        val currentLoc = (currentValue as? String) ?: ""
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = currentLoc,
                                    onValueChange = { onDataChanged(field.key, it) },
                                    label = { Text(labelText) },
                                    isError = isError,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    onDataChanged(field.key, "40.7128° N, 74.0060° W")
                                }) {
                                    Text("Locate")
                                }
                            }
                            if (isError) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                    FieldType.IMAGE -> {
                        val hasImage = currentValue != null
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (hasImage) "Image Captured" else "Capture $labelText",
                                    color = if (hasImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Button(onClick = {
                                    onDataChanged(field.key, "file:///storage/emulated/0/DCIM/img_123.jpg")
                                }) {
                                    Text(if (hasImage) "Retake" else "Camera")
                                }
                            }
                            if (isError) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() }
    )
}
