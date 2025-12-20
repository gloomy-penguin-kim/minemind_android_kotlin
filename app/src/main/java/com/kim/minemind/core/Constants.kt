package com.kim.minemind.core

enum class CellType(val num: Int ) {
    MINE(-1),
    UNKNOWN(0),
    SAFE(1) }
enum class Action { OPEN, FLAG, CHORD }
enum class MoveKind { USER, RULE, STEP }
