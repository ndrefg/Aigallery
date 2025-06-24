package com.google.ai.edge.gallery.data

data class UserPromptOverride(
    val id: String, // Original enum name, e.g., "FREE_FORM"
    val fullPromptTemplate: String?, // User-defined full prompt template string
    val examplePrompts: List<String>? // User-defined list of example prompts
)
