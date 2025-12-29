package com.kim.minemind.ui

import android.app.AlertDialog
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
import com.kim.minemind.core.probabilityBucketFor
import com.kim.minemind.ui.state.CellUI

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.max
import kotlin.math.min


// https://material-theme.com/docs/reference/color-palette/

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.kim.minemind.analysis.rules.Move
import com.kim.minemind.core.TapMode
import com.kim.minemind.core.probabilityToGlyph
import com.kim.minemind.ui.state.GameUiState

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
                DropdownMenuItem(
                    text = { Text("Help") },
                    onClick = { expanded = false; onMenu(TopMenuAction.HELP) }
                )
                DropdownMenuItem(
                    text = { Text("About") },
                    onClick = { expanded = false; onMenu(TopMenuAction.ABOUT) }
                )
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameScreen(vm: GameViewModel) {
    val ui by vm.uiState.collectAsState()
    var infoGid by remember { mutableStateOf<Int?>(null) }
    var optionsOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GameTopBar(
                moves = ui.moves,
                timeText = "00:41",
                onMenu = { vm.handleTopMenu(it) }
            )
        },
        floatingActionButton = {
            OptionsFabMenu(
                ui = ui,
                expanded = optionsOpen,
                onExpandedChange = { optionsOpen = it },

                onVerify = vm::verify,
                onEnumerate = vm::enumerate,
                onAuto = vm::auto,
                onStep = vm::step,
                onUndo = vm::undo,

                onInfo = { vm.setTapMode(TapMode.INFO) },
                onChord = { vm.setTapMode(TapMode.CHORD) },
                onFlag = { vm.setTapMode(TapMode.FLAG) },
                onOpen = { vm.setTapMode(TapMode.OPEN) },
            )
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            BoardFrame(
                ui = ui,
                onCell = { gid ->
                    when (ui.tapMode) {
                        TapMode.OPEN -> vm.dispatch(Action.OPEN, gid)
                        TapMode.FLAG -> vm.dispatch(Action.FLAG, gid)
                        TapMode.CHORD -> vm.dispatch(Action.CHORD, gid)
                        TapMode.INFO -> infoGid = gid
                    }
                },
                onCellLongPress = { gid -> vm.dispatch(Action.FLAG, gid) }
            )

            TapModeRow(
                tapMode = ui.tapMode,
                onTapMode = vm::setTapMode,
                optionsOpen = optionsOpen,
                onToggleOptions = { optionsOpen = !optionsOpen },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 4.dp)
            )

            // ✅ modal
            val gid = infoGid
            if (gid != null) {
                val cell = ui.cells.firstOrNull { it.gid == gid }
                if (cell != null) {
                    CellInfoDialog(
                        cell = cell,
                        ui = ui,
                        onDismiss = { infoGid = null }
                    )
                } else {
                    // cell missing for some reason; just close
                    infoGid = null
                }
            }

        }
    }
}

@Composable
fun CellInfoDialog(
    cell: CellUI,
    ui: GameUiState,
    onDismiss: () -> Unit
) {
    val cols = ui.cols
    val isEnumerate = ui.isEnumerate
    val rules = ui.overlay?.rules
    val conflicts = ui.overlay?.conflicts

    val (r, c) = remember(cell.gid, cols) { divmod(cell.gid, cols) } // helper below

    val probText = when (val p = cell.probability) {
        null -> "—"
        else -> "${(p * 100f).coerceIn(0f, 100f).toInt()}%"
    }

    val statusLines = buildList {
        add("Location: r=$r, c=$c (gid=${cell.gid})")

        add(
            when {
                cell.isRevealed && cell.isMine -> "Revealed: MINE"
                cell.isRevealed -> "Revealed: ${cell.adjacentMines} adjacent mines"
                ui.isVerify && cell.isFlagged && cell.isMine-> "Flagged: verified correct"
                ui.isVerify && cell.isFlagged && !cell.isMine-> "Flagged: verified INCORRECT/conflict"
                cell.isFlagged -> "Flagged"
                cell.isExploded -> "Exploded Mine"
                else -> "Hidden"
            }
        )

        if (isEnumerate) {
            add("Probability: $probText")

            val bucket = probabilityBucketFor(cell.probability)
            if (bucket != null) {
                add(
                    "Bucket: '${bucket.glyph}' " +
                            "(${(bucket.min * 100).toInt()}–${(bucket.max * 100).toInt()}%)"
                )
            }

            if (cell.conflict or (ui.isVerify && cell.isFlagged && !cell.isMine))
                add("⚠ Conflict: constraints disagree here")
            if (cell.forcedOpen)
                add("Rule Open (O): safe in all solutions")
            if (cell.forcedFlag)
                add("Rule Flag (X): mine in all solutions")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Cell Info") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                statusLines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }

                if (isEnumerate) {

                    if (rules?.contains(cell.gid) == true ) {
                        Divider()
                        Text("Matching Rules:", fontWeight = FontWeight.SemiBold)
                        rules.get(cell.gid)?.reasons?.forEach {
                            Text("\t- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (conflicts?.contains(cell.gid) == true) {
                        Divider()
                        Text("Conflicts:", fontWeight = FontWeight.SemiBold)
                        conflicts.get(cell.gid)?.forEach {
                            Text("\t- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Divider()
                Text("Legend", fontWeight = FontWeight.SemiBold)

                Text(
                    if (isEnumerate) {
                        "C = conflict\nO = forced open\nX = forced flag\n0 . , : - ~ = + * # X = probability buckets (low → high)"
                    } else {
                        "Turn on Enumerate to see probability/conflict info."
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                Divider()

                Text("Rule notes", fontWeight = FontWeight.SemiBold)
                Text(
                    "• Conflicts usually mean the current flagged/revealed state is inconsistent with at least one constraint.\n" +
                            "• Forced means every valid mine placement for that component agrees.\n" +
                            "• Probability is local to the enumerated component.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

/*
what do these two mean?  show entropy instead of raw probability

show component-level probability vs global mine-adjusted
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellInfoBottomSheet(
    cell: CellUI,
    rows: Int,
    cols: Int,
    isEnumerate: Boolean,
    onDismiss: () -> Unit
) {
    val (r, c) = remember(cell.gid, cols) { divmod(cell.gid, cols) }
    val probPct = cell.probability?.let { (it * 100f).coerceIn(0f, 100f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // you can tweak sheet shape/colors via MaterialTheme if desired
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Cell Info", style = MaterialTheme.typography.titleLarge)

            Text("r=$r, c=$c (gid=${cell.gid})")

            Text(
                when {
                    cell.isRevealed && cell.isMine -> "Revealed: MINE"
                    cell.isRevealed -> "Revealed: ${cell.adjacentMines} adjacent"
                    cell.isFlagged -> "Flagged"
                    else -> "Hidden"
                }
            )

            if (isEnumerate) {
                Text("Probability: ${probPct?.toInt()?.toString() ?: "—"}%")
                if (cell.conflict) Text("⚠ Conflict: constraints disagree here")
                if (cell.forcedOpen) Text("Forced Open (O)")
                if (cell.forcedFlag) Text("Forced Flag (X)")
            } else {
                Text("Turn on Enumerate to see probability/conflict info.")
            }

            Divider()

            Text("Legend", fontWeight = FontWeight.SemiBold)
            Text(
                "C = conflict · O = forced open · X = forced flag\n" +
                        "0 . , : - ~ = + * # X = probability buckets",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Close") }
        }
    }
}


private fun divmod(gid: Int, cols: Int): Pair<Int, Int> =
    Pair(gid / cols, gid % cols)


fun handleInfo(gid: Int) {
   // need a dialog here
}
@Composable
fun TapModeRow(
    tapMode: TapMode,
    onTapMode: (TapMode) -> Unit,
    optionsOpen: Boolean,
    onToggleOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        color = Color(0xFF21252B)
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.padding(6.dp)
        ) {
            val segmentColors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color(0xFF444a73),
                activeContentColor = Color(0xFFD3DAE3),
                inactiveContainerColor = Color(0xFF21252B),
                inactiveContentColor = Color(0xFFD3DAE3),
            )

            SegmentedButton(
                selected = tapMode == TapMode.OPEN,
                onClick = { onTapMode(TapMode.OPEN) },
                shape = SegmentedButtonDefaults.itemShape(0, 5),
                label = { Text("Open") },
                colors = segmentColors
            )
            SegmentedButton(
                selected = tapMode == TapMode.FLAG,
                onClick = { onTapMode(TapMode.FLAG) },
                shape = SegmentedButtonDefaults.itemShape(1, 5),
                label = { Text("Flag") },
                colors = segmentColors
            )
            SegmentedButton(
                selected = tapMode == TapMode.CHORD,
                onClick = { onTapMode(TapMode.CHORD) },
                shape = SegmentedButtonDefaults.itemShape(2, 5),
                label = { Text("Chord") },
                colors = segmentColors
            )
            SegmentedButton(
                selected = tapMode == TapMode.INFO,
                onClick = { onTapMode(TapMode.INFO) },
                shape = SegmentedButtonDefaults.itemShape(3, 5),
                label = { Text("Info") },
                colors = segmentColors
            )

            // 5th "Options" segment
            SegmentedButton(
                selected = optionsOpen,
                onClick = onToggleOptions,
                shape = SegmentedButtonDefaults.itemShape(4, 5),
                label = { Text("⋯") },
                colors = segmentColors
            )
        }
    }
}



@Composable
fun OptionsFabMenu(
    ui: GameUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,

    onUndo: () -> Unit,
    onStep: () -> Unit,
    onAuto: () -> Unit,
    onVerify: () -> Unit,
    onEnumerate: () -> Unit,
    onInfo: () -> Unit,
    onChord: () -> Unit,
    onFlag: () -> Unit,
    onOpen: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End) {

        AnimatedVisibility(visible = expanded) {
            Column(horizontalAlignment = Alignment.End) {
                SmallActionPill(label = if (ui.isVerify) "Verify ✓" else "Verify", selected = ui.isVerify) {
                    onExpandedChange(false); onVerify()
                }
                Spacer(Modifier.height(10.dp))
                SmallActionPill(label = if (ui.isEnumerate) "Enumerate ✓" else "Enumerate", selected = ui.isEnumerate) {
                    onExpandedChange(false); onEnumerate()
                }
                Spacer(Modifier.height(10.dp))
                SmallActionPill(label = "Auto") { onExpandedChange(false); onAuto() }
                Spacer(Modifier.height(10.dp))
                SmallActionPill(label = "Step") { onExpandedChange(false); onStep() }
                Spacer(Modifier.height(10.dp))
                SmallActionPill(label = "Undo") { onExpandedChange(false); onUndo() }
                Spacer(Modifier.height(55.dp))
            }
        }

//        FloatingActionButton(
//            onClick = { onExpandedChange(!expanded) }
//        ) {
//            Text(if (expanded) "×" else "⋯")
//        }
    }
}


@Composable
private fun SmallActionPill(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val segmentColors = SegmentedButtonDefaults.colors(
        activeContainerColor = Color(0xFF444a73),
        activeContentColor = Color(0xFFD3DAE3),
        inactiveContainerColor = Color(0xFF21252B),
        inactiveContentColor = Color(0xFFD3DAE3),
    )

    val bg = if (selected) Color(0xFF444A73) else Color(0xFF21252B)
    val fg = if (selected) Color(0xFFD3DAE3) else Color(0xFFD3DAE3)

    Surface(
        onClick = onClick,
        color = bg,
        contentColor = fg,
        border = BorderStroke(0.75.dp, Color(0xFF3A3F4B)),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp), // <= your padding control
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = label,
                modifier = Modifier.size(16.dp)
            )
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}



@Composable
fun BoardFrame(
    ui: GameUiState,
    onCell: (Int) -> Unit,
    onCellLongPress: (Int) -> Unit,
) {
    val rows = ui.rows
    val cols = ui.cols
    val cells = ui.cells

    val isVerify = ui.isVerify
    val isEnumerate = ui.isEnumerate

    val shape = RoundedCornerShape(8.dp)
    val cellSize = 34.dp

    val boardW = cellSize * cols
    val boardH = cellSize * rows

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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

        fun clampOffset(newOffset: Offset, newScale: Float)
                : Offset {

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

            return Offset(
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
                        isVerify && !cell.isMine && cell.isFlagged ->
                            // Color(0xFF9B859D) purple
                            Color(0xFFc792ea)
                        cell.isExploded ->
                            // Color(0xFF9B859D) purple
                            Color(0xFFc792ea)
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
                        var txt = ""

                        if (cell.isRevealed) {
                            if (cell.adjacentMines == 0) {
                                txt = ""
                            }
                            else if (cell.adjacentMines >= 1) {
                                txt = cell.adjacentMines.toString()
                            }
                        }
                        else {
                            if (cell.isFlagged) {
                                txt = "F"
                            }
                            else if (cell.isExploded) {
                                txt = "@"
                            }
                            else if (isEnumerate) {
                                txt = when {
                                    cell.conflict -> "C"
                                    cell.forcedFlag -> "X"
                                    cell.forcedOpen -> "O"
                                    (cell.probability ?: -1.0f) >= 0.0f -> probabilityToGlyph(cell.probability)
                                    else -> ""
                                }
                            }
                        }

                        var textColor = Color(0xFFFFFFFF)
                        if (isEnumerate && cell.probability != null) {
                            textColor = Color(0xFF9B859D) // purple
                            textColor = Color(0xFF61AEEF) // blue
                        }
                        if (isEnumerate && cell.conflict) {
                            textColor = Color(0xFFc792ea)
                        }
                        if (cell.forcedFlag or cell.forcedOpen) {
                            textColor = Color(0xFFD3DAE3)
                        }
                        if (cell.isExploded) {
                            textColor = Color(0xFFFFFFFF)
                        }
                        Text(txt, color = textColor)
                    }
                }
            }
        }
    }
}

