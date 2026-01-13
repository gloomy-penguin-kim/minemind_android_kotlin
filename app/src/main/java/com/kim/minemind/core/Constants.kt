package com.kim.minemind.core

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.descriptors.StructureKind

enum class CellType(val num: Int ) {
    MINE(-1),
    UNKNOWN(0),
    SAFE(1)
}

enum class Action { OPEN, FLAG, CHORD, AUTO;

    companion object }
enum class MoveKind(val text: String) {
    USER("USER"),
    RULE("RULE"),
    AUTO("AUTO");
}

enum class TapMode { OPEN, FLAG, CHORD, INFO, HINT }

enum class TopMenuAction { AUTO, STEP, VERIFY, ENUMERATE, SAVE, LOAD, NEW, SETTINGS, HELP, ABOUT  }

data class ProbabilityBucket(
    val glyph: String,
    val min: Float,   // inclusive
    val max: Float    // inclusive
)

private val PROBABILITY_BUCKETS = listOf(
    ProbabilityBucket("",   -1f,   -0.0001f),
    ProbabilityBucket("O",   0.0f,   0.05f),
    ProbabilityBucket(".",   0.05f,  0.15f),
    ProbabilityBucket(",",   0.15f,  0.25f),
    ProbabilityBucket(":",   0.25f,  0.35f),
    ProbabilityBucket("~",   0.35f,  0.45f),
    ProbabilityBucket("+",   0.45f,  0.55f),
    ProbabilityBucket("*",   0.55f,  0.65f),
    ProbabilityBucket("#",   0.65f,  0.75f),
    ProbabilityBucket("%",   0.75f,  0.85f),
    ProbabilityBucket("&",   0.85f,  0.95f),
    ProbabilityBucket("F",   0.95f,  1.0f),
)

fun probabilityToGlyph(p: Float?): String {
    val v = p ?: -1f
    return PROBABILITY_BUCKETS.firstOrNull { v >= it.min && v <= it.max }?.glyph ?: ""
}

fun probabilityBucketFor(p: Float?): ProbabilityBucket? {
    val v = p ?: return null
    return PROBABILITY_BUCKETS.firstOrNull { v >= it.min && v <= it.max }
}

