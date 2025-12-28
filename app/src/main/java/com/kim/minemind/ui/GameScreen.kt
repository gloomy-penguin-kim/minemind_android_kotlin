package com.kim.minemind.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kim.minemind.core.Action
import com.kim.minemind.ui.state.GameViewModel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi


import androidx.compose.ui.graphics.TransformOrigin

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

import com.kim.minemind.core.TopMenuAction
import com.kim.minemind.ui.state.CellUI

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.max
import kotlin.math.min


// https://material-theme.com/docs/reference/color-palette/

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.kim.minemind.core.TapMode

private const val TAG = "ui.GameViewModel"


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameTopBar(
    moves: Int,
    timeText: String,
    onMenu: (TopMenuAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("MineMind", style = MaterialTheme.typography.titleMedium)
                Text("⏱ $timeText", style = MaterialTheme.typography.bodyMedium)
                Text("Moves: $moves", style = MaterialTheme.typography.bodyMedium)
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("New") },
                    onClick = { expanded = false; onMenu(TopMenuAction.NEW) }
                )
                DropdownMenuItem(
                    text = { Text("Save") },
                    onClick = { expanded = false; onMenu(TopMenuAction.SAVE) }
                )
                DropdownMenuItem(
                    text = { Text("Load") },
                    onClick = { expanded = false; onMenu(TopMenuAction.LOAD) }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = { expanded = false; onMenu(TopMenuAction.SETTINGS) }
                )
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameBottomBar(
    tapMode: TapMode,
    onTapMode: (TapMode) -> Unit,

    onInfo: () -> Unit,
    onUndo: () -> Unit,
    onStep: () -> Unit,
    onAuto: () -> Unit,
    onVerify: () -> Unit,
    onEnumerate: () -> Unit,
) {
    var optionsExpanded by remember { mutableStateOf(false) }

    BottomAppBar {

        // Tap-mode chooser (no Info here)
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = tapMode == TapMode.OPEN,
                onClick = { onTapMode(TapMode.OPEN) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                label = { Text("Open") }
            )
            SegmentedButton(
                selected = tapMode == TapMode.FLAG,
                onClick = { onTapMode(TapMode.FLAG) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                label = { Text("Flag") }
            )
            SegmentedButton(
                selected = tapMode == TapMode.CHORD,
                onClick = { onTapMode(TapMode.CHORD) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                label = { Text("Chord") }
            )
        }

        Spacer(Modifier.weight(1f))

        // Optional: keep Undo visible (you said it’s most important)
        TextButton(onClick = onUndo) { Text("Undo") }

        // Options menu
        Box {
            TextButton(onClick = { optionsExpanded = true }) { Text("Options") }

            DropdownMenu(
                expanded = optionsExpanded,
                onDismissRequest = { optionsExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Info") },
                    onClick = { optionsExpanded = false; onInfo() }
                )
                DropdownMenuItem(
                    text = { Text("Undo") },
                    onClick = { optionsExpanded = false; onUndo() }
                )
                DropdownMenuItem(
                    text = { Text("Step") },
                    onClick = { optionsExpanded = false; onStep() }
                )
                DropdownMenuItem(
                    text = { Text("Auto") },
                    onClick = { optionsExpanded = false; onAuto() }
                )
                DropdownMenuItem(
                    text = { Text("Verify") },
                    onClick = { optionsExpanded = false; onVerify() }
                )
                DropdownMenuItem(
                    text = { Text("Enumerate") },
                    onClick = { optionsExpanded = false; onEnumerate() }
                )
            }
        }
    }
}




@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameScreen(vm: GameViewModel) {
    val ui by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            GameTopBar(
                moves = ui.moves,
                timeText = "00:41", // ui.timerText
                onMenu = { action -> vm.handleTopMenu(action) }
            )
        },
        bottomBar = {
            GameBottomBar(
                tapMode = ui.tapMode,
                onTapMode = vm::setTapMode,
                onInfo = vm::info,
                onUndo = vm::undo,
                onStep = vm::step,
                onAuto = vm::auto,
                onVerify = vm::verify,
                onEnumerate = vm::enumerate,
            )
        }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(12.dp)) {
            if (ui.gameOver) {
                Text(
                    if (ui.win) "You won!" else "Boom. You lost.",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .border(4.dp, Color(0xFF282A36), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                ZoomPanBoard {
                    BoardFrame(
                        rows = ui.rows,
                        cols = ui.cols,
                        cells = ui.cells,
                        onCell = { gid ->
                            when (ui.tapMode) {
                                TapMode.OPEN  -> vm.dispatch(Action.OPEN, gid)
                                TapMode.FLAG  -> vm.dispatch(Action.FLAG, gid)
                                TapMode.CHORD -> vm.dispatch(Action.CHORD, gid)
                                TapMode.INFO  -> vm.handleInfo(gid)
                            }
                        },
                        onCellLongPress = { gid -> vm.dispatch(Action.FLAG, gid) }
                    )
                }
            }
        }
    }
}


@Composable
fun BoardFrame(
    rows: Int,
    cols: Int,
    cells: List<CellUI>,
    onCell: (Int) -> Unit,
    onCellLongPress: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val cellSize = 34.dp

    val boardW = cellSize * cols
    val boardH = cellSize * rows

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val minScale = 0.6f
    val maxScale = 3.0f

    // how much background you want to be able to reveal on either side
    val slackDp: Dp = 120.dp

    BoxWithConstraints(
        modifier = Modifier
            .border(4.dp, Color(0xFF282A36), shape)
            .clip(shape)
            .background(Color(0xFF111318))
    ) {
        val viewportW = constraints.maxWidth.toFloat()
        val viewportH = constraints.maxHeight.toFloat()

        val density = LocalDensity.current
        val boardWpx = with(density) { boardW.toPx() }
        val boardHpx = with(density) { boardH.toPx() }
        val slackPx = with(density) { slackDp.toPx() }

        fun clampOffset(newOffset: androidx.compose.ui.geometry.Offset, newScale: Float)
                : androidx.compose.ui.geometry.Offset {

            val scaledW = boardWpx * newScale
            val scaledH = boardHpx * newScale

            // If board is smaller than viewport, allow it to float around centered (still with slack).
            // If board is bigger, allow symmetric overscroll via slackPx.
            val baseMinX = viewportW - scaledW
            val baseMinY = viewportH - scaledH

            val minX = if (scaledW <= viewportW) {
                ((viewportW - scaledW) / 2f) - slackPx
            } else {
                baseMinX - viewportW
            }
            val maxX = if (scaledW <= viewportW) {
                ((viewportW - scaledW) / 2f) + slackPx
            } else {
                0f + viewportW
            }

            val minY = if (scaledH <= viewportH) {
                ((viewportH - scaledH) / 2f) - slackPx
            } else {
                baseMinY - viewportH
            }
            val maxY = if (scaledH <= viewportH) {
                ((viewportH - scaledH) / 2f) + slackPx
            } else {
                0f + viewportH
            }

            return androidx.compose.ui.geometry.Offset(
                x = newOffset.x.coerceIn(minX, maxX),
                y = newOffset.y.coerceIn(minY, maxY),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                        val newOffset = offset + pan
                        scale = newScale
                        offset = clampOffset(newOffset, newScale)
                    }
                }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                userScrollEnabled = false,
                modifier = Modifier
                    .requiredSize(boardW, boardH)
                    .graphicsLayer {
                        // IMPORTANT: makes clamp math behave intuitively
                        transformOrigin = TransformOrigin(0f, 0f)

                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {

                items(cells, key = { it.gid }) { cell ->

                    val bg = when {
                        cell.isRevealed and !cell.isMine ->
                            Color(0xFF4D515D)
                        cell.isRevealed and cell.isMine ->
                            // Color(0xFF9B859D) purple
                            Color(0xFF61AEEF)
                        cell.isFlagged ->
                            // Color(0xFF9B859D) purple
                            Color(0xFF61AEEF)
                        else ->
                            Color(0xFF282C34)
                    }

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .padding(0.dp)
                            .border(1.dp, Color(0xFF282C34))
                            .background(bg)
                            .combinedClickable(
                                onClick = { onCell(cell.gid) },
                                onLongClick = { onCellLongPress(cell.gid) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val txt = when {
                            cell.isFlagged && !cell.isRevealed -> "F"
                            cell.isRevealed && (cell.adjacentMines == -1) -> "@"
                            cell.isRevealed && (cell.adjacentMines == 0) -> ""
                            cell.isRevealed && (cell.adjacentMines >= 1) -> cell.adjacentMines.toString()

                            cell.conflict -> "C"
                            cell.forcedFlag -> "X"
                            cell.forcedOpen -> "O"

                            (cell.probability ?: -1.0f) <  0.0f  -> ""
                            (cell.probability ?: -1.0f) <= 0.05f -> "0"
                            (cell.probability ?: -1.0f) <= 0.15f -> "."
                            (cell.probability ?: -1.0f) <= 0.25f -> ","
                            (cell.probability ?: -1.0f) <= 0.35f -> ":"
                            (cell.probability ?: -1.0f) <= 0.45f -> "-"
                            (cell.probability ?: -1.0f) <= 0.55f -> "~"
                            (cell.probability ?: -1.0f) <= 0.65f -> "="
                            (cell.probability ?: -1.0f) <= 0.75f -> "+"
                            (cell.probability ?: -1.0f) <= 0.85f -> "*"
                            (cell.probability ?: -1.0f) <= 0.95f -> "#"
                            (cell.probability ?: -1.0f) >  0.95f -> "X"
                            else -> ""
                        }
                        var textColor = Color(0xFFFFFFFF)
                        if (cell.probability != null) {
                            textColor = Color(0xFF9B859D) // purple
                            textColor = Color(0xFF61AEEF) // blue
                        }
                        if (cell.conflict) {
                            textColor = Color(0xFFc792ea)
                        }
                        if (cell.forcedFlag or cell.forcedOpen) {
                            textColor = Color(0xFFD3DAE3)
                        }
                        Text(txt, color = textColor)
                    }
                }
            }
        }
    }
}



//private fun visibleGids(cam: Camera, cols: Int): List<Int> {
//    val out = ArrayList<Int>(cam.viewRows * cam.viewCols)
//    for (r in cam.topRow until (cam.topRow + cam.viewRows)) {
//        for (c in cam.leftCol until (cam.leftCol + cam.viewCols)) {
//            out.add(r * cols + c)
//        }
//    }
//    return out
//}

@Composable
fun BoardView(
    rows: Int,
    cols: Int,
    cells: List<CellUI>,
    onCell: (Int) -> Unit,
    onCellLongPress: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val cellSize = 34.dp

    val boardW = cellSize * cols
    val boardH = cellSize * rows

    // pan/zoom state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // tune these
    val minScale = 0.6f
    val maxScale = 3.0f

    BoxWithConstraints(
        modifier = Modifier
            .border(4.dp, Color(0xFF282A36), shape)
            .clip(shape)                 // IMPORTANT: clip to border/frame
            .background(Color(0xFF111318))
    ) {
        val viewportW = constraints.maxWidth.toFloat()
        val viewportH = constraints.maxHeight.toFloat()

        // convert board size to px for clamping
        val density = LocalDensity.current
        val boardWpx = with(density) { boardW.toPx() }
        val boardHpx = with(density) { boardH.toPx() }

        fun clampOffset(newOffset: androidx.compose.ui.geometry.Offset, newScale: Float)
                : androidx.compose.ui.geometry.Offset {

            val scaledW = boardWpx * newScale
            val scaledH = boardHpx * newScale

            // We allow panning, but keep *some* of the board visible.
            // If board smaller than viewport, center it.
            val minX = if (scaledW <= viewportW) (viewportW - scaledW) / 2f else viewportW - scaledW
            val maxX = if (scaledW <= viewportW) (viewportW - scaledW) / 2f else 0f

            val minY = if (scaledH <= viewportH) (viewportH - scaledH) / 2f else viewportH - scaledH
            val maxY = if (scaledH <= viewportH) (viewportH - scaledH) / 2f else 0f

            return androidx.compose.ui.geometry.Offset(
                x = newOffset.x.coerceIn(minX, maxX),
                y = newOffset.y.coerceIn(minY, maxY),
            )
        }

        // gesture layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                        val newOffset = offset + pan
                        scale = newScale
                        offset = clampOffset(newOffset, newScale)
                    }
                }
        ) {
            // transformed content (grid)
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                userScrollEnabled = false, // we pan manually
                modifier = Modifier
                    // CRITICAL: give the grid its natural size so it doesn't squish
                    .requiredSize(boardW, boardH)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(cells, key = { it.gid }) { cell ->

                    val bg = when {
                        cell.isRevealed and !cell.isMine ->
                            Color(0xFF4D515D)
                        cell.isRevealed and cell.isMine ->
                            // Color(0xFF9B859D) purple
                            Color(0xFF61AEEF)
                        cell.isFlagged ->
                            // Color(0xFF9B859D) purple
                            Color(0xFF61AEEF)
                        else ->
                            Color(0xFF282C34)
                    }

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .padding(0.dp)
                            .border(1.dp, Color(0xFF282C34))
                            .background(bg)
                            .combinedClickable(
                                onClick = { onCell(cell.gid) },
                                onLongClick = { onCellLongPress(cell.gid) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val txt = when {
                            cell.isFlagged && !cell.isRevealed -> "F"
                            cell.isRevealed && (cell.adjacentMines == -1) -> "@"
                            cell.isRevealed && (cell.adjacentMines == 0) -> ""
                            cell.isRevealed && (cell.adjacentMines >= 1) -> cell.adjacentMines.toString()

                            cell.conflict -> "C"
                            cell.forcedFlag -> "X"
                            cell.forcedOpen -> "O"

                            (cell.probability ?: -1.0f) <  0.0f  -> ""
                            (cell.probability ?: -1.0f) <= 0.05f -> "0"
                            (cell.probability ?: -1.0f) <= 0.15f -> "."
                            (cell.probability ?: -1.0f) <= 0.25f -> ","
                            (cell.probability ?: -1.0f) <= 0.35f -> ":"
                            (cell.probability ?: -1.0f) <= 0.45f -> "-"
                            (cell.probability ?: -1.0f) <= 0.55f -> "~"
                            (cell.probability ?: -1.0f) <= 0.65f -> "="
                            (cell.probability ?: -1.0f) <= 0.75f -> "+"
                            (cell.probability ?: -1.0f) <= 0.85f -> "*"
                            (cell.probability ?: -1.0f) <= 0.95f -> "#"
                            (cell.probability ?: -1.0f) >  0.95f -> "X"

                            else -> ""
                        }
                        var textColor = Color(0xFFFFFFFF)
                        if (cell.probability != null) {
                            textColor = Color(0xFF9B859D) // purple
                            textColor = Color(0xFF61AEEF) // blue
                        }
                        if (cell.conflict) {
                            textColor = Color(0xFFc792ea)
                        }
                        if (cell.forcedFlag or cell.forcedOpen) {
                            textColor = Color(0xFFD3DAE3)
                        }
                        Text(txt, color = textColor)
                    }
                }
            }
        }
    }
}


@Composable
fun CellView(cell: CellUI, onCell: (Int)->Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color(0xFF44475A))
            .clickable { onCell(cell.gid) },
        contentAlignment = Alignment.Center
    ) {
        Text(cell.adjacentMines.takeIf { it > 0 }?.toString() ?: "",
            color = Color(0xFF8BE9FD))
    }
}


@Composable
fun ZoomPanBoard(
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val minScale = 0.6f
    val maxScale = 3.0f

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(minScale, maxScale)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
                translationY = offsetY
                // rotationZ = -2.5f // optional “slight angle”
            }
    ) {
        content()
    }
}



private fun probToBg(
    p: Float,                 // 0.0 .. 1.0
    base: Color,              // background for 0%
    hot: Color,               // background for 100%
): Color {
    val t = p.coerceIn(0f, 1f)
    return lerp(base, hot, t)
}