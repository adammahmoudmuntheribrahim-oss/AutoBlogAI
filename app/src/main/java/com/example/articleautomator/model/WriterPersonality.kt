package com.example.articleautomator.model

enum class WriterPersonality(val displayName: String, val instruction: String) {
    CRITIC("الناقد", "Write the article in a critical and analytical style, highlighting potential problems and improvements. Add a personal critical opinion and a cautious future prediction."),
    OPTIMIST("المتفائل", "Write the article in an optimistic and inspiring style, focusing on opportunities and positive outcomes. Include a hopeful personal opinion and an optimistic future prediction."),
    REALIST("الواقعي", "Write the article in a neutral, realistic style with balanced facts and logical reasoning. Provide a reasoned personal opinion and a realistic future outlook.")
}
