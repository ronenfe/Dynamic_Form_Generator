package net.festinger.dynamicformgenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FormViewModel = FormViewModel()
) {
    val schema by viewModel.schema
    val formData = viewModel.formData
    val validationErrors by viewModel.validationErrors
    val submissionResult by viewModel.submissionResult
    val showSchemaDialog by viewModel.showSchemaDialog

    val rawJson by viewModel.rawSchemaJson

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { viewModel.loadRandomJson() }
                    ) {
                        Text("Generate JSON", textAlign = TextAlign.Center)
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = rawJson.isNotEmpty(),
                        onClick = { viewModel.parseJson() }
                    ) {
                        Text("Parse Schema", textAlign = TextAlign.Center)
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
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
                if (rawJson.isNotEmpty()) {
                    Text("JSON Loaded. Click 'Parse Schema' to render form.")
                } else {
                    Text("Click 'Generate JSON' to start.")
                }
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

        // --- BOTTOM SHEETS INSTEAD OF DIALOGS ---

        // 1. Submission Result Sheet
        if (submissionResult != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.submissionResult.value = null }
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 48.dp) // Extra padding for bottom nav bars
                ) {
                    Text("Form Submission JSON", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Content Area
                    Column(modifier = Modifier
                        .heightIn(max = 400.dp) // Limit height so it doesn't cover whole screen
                        .verticalScroll(rememberScrollState())
                    ) {
                        Text(submissionResult ?: "")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.submissionResult.value = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }

        // 2. Schema Source Sheet
        if (showSchemaDialog) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.showSchemaDialog.value = false }
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 48.dp)
                ) {
                    Text("Generated Source JSON", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                    ) {
                        Text(rawJson)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.showSchemaDialog.value = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
