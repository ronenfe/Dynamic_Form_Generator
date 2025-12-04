package net.festinger.dynamicformgenerator.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import net.festinger.dynamicformgenerator.data.FieldType
import net.festinger.dynamicformgenerator.data.FormFieldSchema
import java.io.ByteArrayOutputStream
import java.io.File
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
    val context = LocalContext.current

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
                
                // Helper function to map icon string to Vector
                fun getIcon(name: String?): @Composable (() -> Unit)? {
                    if (name == null) return null
                    val imageVector = when (name) {
                        "tag" -> Icons.Default.Info
                        "height" -> Icons.Default.KeyboardArrowUp
                        "email" -> Icons.Default.Email
                        "phone" -> Icons.Default.Phone
                        "warning" -> Icons.Default.Warning
                        "link" -> Icons.Default.Share
                        "lock" -> Icons.Default.Lock
                        "my_location" -> Icons.Default.LocationOn
                        else -> null
                    } ?: return null
                    return { Icon(imageVector, contentDescription = null) }
                }

                // Helper function to map input type string to KeyboardOptions
                fun getKeyboardOptions(): KeyboardOptions {
                    val type = when (field.uiInputType) {
                        "email" -> KeyboardType.Email
                        "phone" -> KeyboardType.Phone
                        "number", "numberPassword" -> KeyboardType.Number
                        "textUri" -> KeyboardType.Uri
                        else -> KeyboardType.Text
                    }
                    return KeyboardOptions(keyboardType = type)
                }
                
                // Helper for visual transformation
                fun getVisualTransformation(): VisualTransformation {
                    return if (field.uiInputType == "numberPassword") PasswordVisualTransformation() 
                    else VisualTransformation.None
                }

                Column {
                    when (field.type) {
                        FieldType.STRING, FieldType.NUMBER -> {
                            // Special case for Slider
                            if (field.type == FieldType.NUMBER && field.uiWidget == "slider") {
                                Text(text = "$labelText: ${(currentValue as? Number)?.toString() ?: ""}")
                                Slider(
                                    value = (currentValue.toString().toFloatOrNull() ?: field.min?.toFloat() ?: 0f),
                                    onValueChange = { onDataChanged(field.key, it.toDouble()) },
                                    valueRange = (field.min?.toFloat() ?: 0f)..(field.max?.toFloat() ?: 10f),
                                    steps = if (field.uiStep != null) ((field.max!! - field.min!!) / field.uiStep).toInt() - 1 else 0
                                )
                            } else {
                                OutlinedTextField(
                                    value = (currentValue as? String) ?: "",
                                    onValueChange = { onDataChanged(field.key, it) },
                                    label = { Text(labelText) },
                                    placeholder = if (field.uiPlaceholder != null) { { Text(field.uiPlaceholder) } } else null,
                                    isError = isError,
                                    leadingIcon = getIcon(field.uiIcon),
                                    supportingText = if (isError) { { Text(errorMessage) } } else null,
                                    keyboardOptions = if (field.type == FieldType.NUMBER) KeyboardOptions(keyboardType = KeyboardType.Number) else getKeyboardOptions(),
                                    visualTransformation = getVisualTransformation(),
                                    minLines = field.uiLines ?: 1,
                                    maxLines = field.uiMaxLines ?: 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        FieldType.BOOLEAN -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = labelText)
                                if (field.uiWidget == "switch") {
                                    Switch(
                                        checked = (currentValue as? Boolean) ?: false,
                                        onCheckedChange = { onDataChanged(field.key, it) }
                                    )
                                } else {
                                    Checkbox(
                                        checked = (currentValue as? Boolean) ?: false,
                                        onCheckedChange = { onDataChanged(field.key, it) }
                                    )
                                }
                            }
                        }
                        FieldType.ENUM_DROPDOWN -> {
                            val options = field.options ?: emptyList()
                            val selectedText = (currentValue as? String) ?: ""

                            if (field.uiWidget == "radio") {
                                Text(labelText, style = MaterialTheme.typography.bodyLarge)
                                options.forEach { option ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = (option == selectedText),
                                                onClick = { onDataChanged(field.key, option) }
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (option == selectedText),
                                            onClick = { onDataChanged(field.key, option) }
                                        )
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            } else {
                                var expanded by remember { mutableStateOf(false) }
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
                                                val formatStr = field.uiFormat ?: "yyyy-MM-dd"
                                                val format = SimpleDateFormat(formatStr, Locale.getDefault())
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = currentLoc,
                                    onValueChange = { onDataChanged(field.key, it) },
                                    label = { Text(labelText) },
                                    isError = isError,
                                    leadingIcon = getIcon(field.uiIcon),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    onDataChanged(field.key, "40.7128° N, 74.0060° W")
                                }) {
                                    Text(field.uiButtonLabel ?: "Locate")
                                }
                            }
                        }
                        FieldType.IMAGE -> {
                            val hasImage = currentValue != null
                            var tempImageUri by remember { mutableStateOf<Uri?>(null) }

                            val cameraLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.TakePicture()
                            ) { success ->
                                if (success && tempImageUri != null) {
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(tempImageUri!!)
                                        val bitmap = BitmapFactory.decodeStream(inputStream)
                                        val outputStream = ByteArrayOutputStream()
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                                        val byteArray = outputStream.toByteArray()
                                        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
                                        onDataChanged(field.key, base64String)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            val permissionLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { isGranted ->
                                if (isGranted) {
                                    val file = File.createTempFile("img_", ".jpg", context.cacheDir)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    tempImageUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
                                }
                            }

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
                                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Text(if (hasImage) "Retake" else field.uiButtonLabel ?: "Camera")
                                }
                            }
                        }
                    }
                    
                    // Render Helper Text below the field
                    if (field.uiHelp != null && !isError) {
                        Text(
                            text = field.uiHelp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                        )
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
