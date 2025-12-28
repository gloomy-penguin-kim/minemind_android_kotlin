package com.kim.minemind.core

enum class CellType(val num: Int ) {
    MINE(-1),
    UNKNOWN(0),
    SAFE(1) }
enum class Action { OPEN, FLAG, CHORD, INVALID }
enum class MoveKind(val text: String) {
    USER("USER"),
    STEP("STEP"),
    RULE("RULE"),
    AUTO("AUTO");
}

enum class TapMode { OPEN, FLAG, CHORD, INFO }

enum class TopMenuAction { AUTO, STEP, VERIFY, ENUMERATE, SAVE, LOAD, NEW, SETTINGS }