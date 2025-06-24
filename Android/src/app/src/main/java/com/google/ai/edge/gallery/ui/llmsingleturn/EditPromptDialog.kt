package com.google.ai.edge.gallery.ui.llmsingleturn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.data.UserPromptOverride

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromptDialog(
    promptTemplateType: PromptTemplateType,
    userPromptOverride: UserPromptOverride?,
    onDismissRequest: () -> Unit,
    onSaveRequest: (UserPromptOverride) -> Unit
) {
    var fullPromptTemplateInput by remember {
        mutableStateOf(userPromptOverride?.fullPromptTemplate ?: "") // Placeholder for now
    }
    val examplePromptsState = remember {
        mutableStateListOf<String>().also { list ->
            (userPromptOverride?.examplePrompts ?: promptTemplateType.examplePrompts).forEach { list.add(it) }
        }
    }

    // Determine the initial fullPromptTemplateInput based on override or default
    // This is a simplified representation. Actual genFullPrompt logic is more complex.
    // For now, we'll assume a simple template string can be edited.
    // A more robust solution would involve parsing the existing genFullPrompt.
    LaunchedEffect(promptTemplateType, userPromptOverride) {
        if (userPromptOverride?.fullPromptTemplate != null) {
            fullPromptTemplateInput = userPromptOverride.fullPromptTemplate
        } else {
            // Attempt to create a representative template string from genFullPrompt.
            // This is a placeholder. A real solution needs a way to deconstruct/represent genFullPrompt.
            // For example, if genFullPrompt is: buildAnnotatedString { append("Rewrite: "); append(userInput) }
            // We might store "Rewrite: ${userInput}"
            // For FREE_FORM, the template is just the user input.
            if (promptTemplateType == PromptTemplateType.FREE_FORM) {
                fullPromptTemplateInput = "\${userInput}" // Special case for free form
            } else {
                 // Heuristic: Try to extract the static parts of the prompt.
                 // This is highly dependent on how genFullPrompt is structured.
                 // For now, let's leave it blank if not overridden and not FREE_FORM,
                 // requiring the user to define it if they want to edit.
                 // Or, we can try a simple heuristic for prompts with fixed prefix/suffix.
                val sampleOutput = promptTemplateType.genFullPrompt("SAMPLE_USER_INPUT", mapOf()).text
                if (sampleOutput.contains("SAMPLE_USER_INPUT")) {
                    fullPromptTemplateInput = sampleOutput.replace("SAMPLE_USER_INPUT", "\${userInput}")
                } else {
                    fullPromptTemplateInput = "" // Default to blank if complex or not easily templated
                }
            }
        }
    }


    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Edit Prompt: ${promptTemplateType.label}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Full Prompt Template Editor (Simplified)
                Text("Prompt Template:", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = fullPromptTemplateInput,
                    onValueChange = { fullPromptTemplateInput = it },
                    label = { Text("Full prompt template (\${userInput} for user input)") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 200.dp),
                    supportingText = { Text("Use \${userInput} for the main text. Other variables like \${tone} might be available depending on the template.")}
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Example Prompts Editor
                Text("Example Prompts:", style = MaterialTheme.typography.titleMedium)
                examplePromptsState.forEachIndexed { index, example ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = example,
                            onValueChange = { examplePromptsState[index] = it },
                            label = { Text("Example ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { examplePromptsState.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove example")
                        }
                    }
                }
                Button(
                    onClick = { examplePromptsState.add("") },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add example")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Example")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newOverride = UserPromptOverride(
                        id = promptTemplateType.name,
                        fullPromptTemplate = fullPromptTemplateInput.ifBlank { null }, // Store null if empty
                        examplePrompts = examplePromptsState.toList()
                    )
                    onSaveRequest(newOverride)
                    onDismissRequest()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
