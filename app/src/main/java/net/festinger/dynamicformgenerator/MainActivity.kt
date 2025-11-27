package net.festinger.dynamicformgenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import net.festinger.dynamicformgenerator.ui.theme.DynamicFormGeneratorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DynamicFormGeneratorTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: FormViewModel = FormViewModel() // Inject ViewModel
) {
    // Read state from ViewModel
    val schema by viewModel.schema
    val formData = viewModel.formData
    val validationErrors by viewModel.validationErrors
    val submissionResult by viewModel.submissionResult
    var showSchemaDialog by viewModel.showSchemaDialog

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { viewModel.generateNewForm() }
                    ) {
                        Text("Generate")
                    }

                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        enabled = schema.isNotEmpty(),
                        onClick = { showSchemaDialog = true }
                    ) {
                        Text("View Schema", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }

                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        enabled = schema.isNotEmpty(),
                        onClick = { viewModel.submitForm() }
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Dynamic Form Generator",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (schema.isEmpty()) {
                Text("Click 'Generate' to start.")
            } else {
                DynamicForm(
                    schema = schema,
                    formData = formData,
                    validationErrors = validationErrors,
                    onDataChanged = { key, value ->
                        viewModel.onDataChanged(key, value)
                    }
                )
            }
        }

        // Dialogs logic handles pure UI, data comes from VM
        if (submissionResult != null) {
            AlertDialog(
                onDismissRequest = { viewModel.submissionResult.value = null },
                confirmButton = {
                    TextButton(onClick = { viewModel.submissionResult.value = null }) { Text("Close") }
                },
                title = { Text("Form Submission JSON") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(submissionResult ?: "")
                    }
                }
            )
        }

        if (showSchemaDialog) {
            AlertDialog(
                onDismissRequest = { showSchemaDialog = false },
                confirmButton = {
                    TextButton(onClick = { showSchemaDialog = false }) { Text("Close") }
                },
                title = { Text("Current Schema JSON") },
                text = {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(gson.toJson(schema))
                    }
                }
            )
        }
    }
}
