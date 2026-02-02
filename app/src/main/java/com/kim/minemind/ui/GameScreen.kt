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


import androidx.compose.ui.graphics.TransformOrigin

import androidx.compose.ui.graphics.Color

import com.kim.minemind.core.TopMenuAction
import com.kim.minemind.core.probabilityBucketFor
import com.kim.minemind.ui.state.CellUI

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer


// https://material-theme.com/docs/reference/color-palette/

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import com.kim.minemind.analysis.rules.Rule
import com.kim.minemind.core.TapMode
import com.kim.minemind.core.probabilityToGlyph
import com.kim.minemind.ui.settings.ColorSet
import com.kim.minemind.ui.settings.GlyphMode
import com.kim.minemind.ui.settings.GlyphSet
import com.kim.minemind.ui.settings.VisualCatalog
import com.kim.minemind.ui.settings.VisualSettings
import com.kim.minemind.ui.settings.VisualState
import com.kim.minemind.ui.state.GameUiState
import kotlinx.coroutines.flow.StateFlow


/*
what do these two mean?  show entropy instead of raw probability

show component-level probability vs global mine-adjusted
*/


private const val TAG = "ui.GameViewModel"


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameTopBar(
    vm: GameViewModel,
    moves: Int,
    timeText: String,
    onMenu: (TopMenuAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showVisualSettings by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
        if (!showVisualSettings) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // I need to hide this when the settings are displayed
                    Text("MineMind", style = MaterialTheme.typography.titleMedium)
                    Text("⏱ $timeText", style = MaterialTheme.typography.bodyMedium)
                    Text("Moves: $moves", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
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

                // ⭐ Correct way: toggle the visibility state
                DropdownMenuItem(
                    text = { Text("Visual Settings") },
                    onClick = {
                        expanded = false
                        showVisualSettings = true
                    }
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

    // ⭐ Rendering the visual settings screen OUTSIDE the menu
    if (showVisualSettings) {
        VisualSettingsScreen(
            settingsFlow = vm.visualSettings,
            onChange = vm::updateVisualSettings,
            onDismiss = { showVisualSettings = false },
            visualState = vm.visualState
        )
    }
}



@Composable
fun GameTopBarWithMenu(vm: GameViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var showVisualSettings by remember { mutableStateOf(false) }

    // ... your TopAppBar / IconButton that toggles `expanded`

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        DropdownMenuItem(
            text = { Text("Visual Settings") },
            onClick = {
                expanded = false
                showVisualSettings = true
            }
        )
    }

    if (showVisualSettings) {
        VisualSettingsScreen(
            settingsFlow = vm.visualSettings,
            onChange = vm::updateVisualSettings,
            onDismiss = { showVisualSettings = false },
            visualState = vm.visualState
        )
    }
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
                vm = vm,
                moves = ui.moves,
                timeText = "00:41",
                onMenu = { vm.handleTopMenu(it) }
            )
        },
        // TODO: add menu item for Stats (mine counts, board status, number of solutions if enum is on, etc)
        // TODO: add menu item for highlight all the chords
        floatingActionButton = {
            OptionsFabMenu(
                ui = ui,
                expanded = optionsOpen,
                onExpandedChange = { optionsOpen = it },

                onVerify = vm::verify,
                onEnumerate = vm::enumerate,
                onAuto = vm::auto,
                onUndo = vm::undo,

                // TODO: make it so hint stays selected and maybe nothing in the bottom menu is selected
                onHint  = { vm.setTapMode(TapMode.HINT) },
                onInfo  = { vm.setTapMode(TapMode.INFO) },
                onChord = { vm.setTapMode(TapMode.CHORD) },
                onFlag  = { vm.setTapMode(TapMode.FLAG) },
                onOpen  = { vm.setTapMode(TapMode.HINT) },
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
                        TapMode.HINT -> vm.hint(gid)
                    }
                },
                onCellLongPress = { gid -> vm.dispatch(Action.FLAG, gid)},
                visualState = vm.visualState,
                visualSettings = vm.visualSettings
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
    val rules: Map<Int, Rule> = ui.ruleList
    val conflicts = ui.conflictBoard.merge(ui.conflictProbs)

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
                ui.isVerify && cell.isFlagged && cell.isMine -> "Flagged: correct"
                ui.isVerify && cell.isFlagged && !cell.isMine -> "Flagged: INCORRECT"
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

                if (!ui.isEnumerate) {
                    HorizontalDivider()
                    Text("Turn on Enumerate for more info about rules, probabilities, and conflicts.")
                }
                else if (cell.forcedOpen) {
                    HorizontalDivider()
                    Text("Rule Open (O): safe in all solutions")
                }
                else if (cell.forcedFlag) {
                    HorizontalDivider()
                    Text("Rule Flag (X): mine in all solutions")
                }
                if (isEnumerate) {
                    if (cell.gid in conflicts.keys) {
                        HorizontalDivider()
                        Text("Conflicts:", fontWeight = FontWeight.SemiBold)
                        conflicts.getReasons(cell.gid).forEach {
                            Text("\t- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (cell.gid in rules.keys) {
                        HorizontalDivider()
                        Text("Matching Rules:", fontWeight = FontWeight.SemiBold)
                        rules[cell.gid]?.reasons?.getReasons()?.forEach {
                            Text("\t- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (!ui.overlay.isConsistent) {
                    HorizontalDivider()
                    Text(text = "⚠ Board state is inconsistent (0 valid solutions)",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}

@Composable
fun HelpInfoDialog(
    cell: CellUI,
    ui: GameUiState,
    onDismiss: () -> Unit
) {
    val cols = ui.cols
    val isEnumerate = ui.isEnumerate
    val rules = ui.ruleList
    val conflicts = ui.conflictBoard.merge(ui.conflictProbs)

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
                ui.isVerify && cell.isFlagged && cell.isMine -> "Flagged: verified correct"
                ui.isVerify && cell.isFlagged && !cell.isMine -> "Flagged: verified INCORRECT/conflict"
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
                    if (cell.gid in conflicts.keys) {
                        HorizontalDivider()
                        Text("Conflicts:", fontWeight = FontWeight.SemiBold)
                        conflicts.getReasons(cell.gid).forEach {
                            Text("\t- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (cell.gid in rules.keys) {
                        HorizontalDivider()
                        Text("Matching Rules:", fontWeight = FontWeight.SemiBold)
                        rules.get(cell.gid)?.reasons?.getReasons()?.forEach {
                            Text("\t- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (!ui.overlay.isConsistent) {
                    HorizontalDivider()
                    Text("⚠ Board state is inconsistent (0 valid solutions)")
                }

                HorizontalDivider()
                Text("Legend", fontWeight = FontWeight.SemiBold)

                Text(
                    if (isEnumerate) {
                        "C = conflict\nO = forced open\nX = forced flag\n0 . , : - ~ = + * # X = probability buckets (low → high)"
                    } else {
                        "Turn on Enumerate to see probability/conflict info."
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()

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
                if (cell.forcedOpen) Text("Rule based Open (O)")
                if (cell.forcedFlag) Text("Rule based Flag (X)")
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
    onAuto: () -> Unit,
    onVerify: () -> Unit,
    onEnumerate: () -> Unit,
    onInfo: () -> Unit,
    onChord: () -> Unit,
    onFlag: () -> Unit,
    onOpen: () -> Unit,
    onHint: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {

        AnimatedVisibility(visible = expanded) {
            Column(horizontalAlignment = Alignment.End) {
                SmallActionPill(
                    label = if (ui.isVerify) "Verify ✓" else "Verify",
                    selected = ui.isVerify
                ) {
                    onExpandedChange(false); onVerify()
                }
                Spacer(Modifier.height(10.dp))
                SmallActionPill(
                    label = if (ui.isEnumerate) "Enumerate ✓" else "Enumerate",
                    selected = ui.isEnumerate
                ) {
                    onExpandedChange(false); onEnumerate()
                }
                Spacer(Modifier.height(10.dp))
                SmallActionPill(label = "Hint") { onExpandedChange(false); onHint(); }
                Spacer(Modifier.height(10.dp))
                SmallActionPill(label = "Autobot") { onExpandedChange(false); onAuto(); }
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
    visualState: StateFlow<VisualState>,
    visualSettings: StateFlow<VisualSettings>
) {

//    val rows = ui.rows
//    val cols = ui.cols
//    val cells = ui.cells
//
//    val isVerify = ui.isVerify
//    val isEnumerate = ui.isEnumerate
//
//    val shape = RoundedCornerShape(8.dp)
//    val cellSize = 34.dp
//
//    val boardW = cellSize * cols
//    val boardH = cellSize * rows
//
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    val minScale = 0.6f
//    val maxScale = 3.0f
//
//    // how much background you want to be able to reveal on either side
//    val slackDp: Dp = 120.dp
//
//    val state by visualState.collectAsState()
//    val settings by visualSettings.collectAsState()
//
//    BoxWithConstraints(
//        modifier = Modifier
//            .border(4.dp, Color(0xFF282A36), shape)
//            .clip(shape)
//            .background(Color(0xFF111318))
//    ) {
//        val viewportW = constraints.maxWidth.toFloat()
//        val viewportH = constraints.maxHeight.toFloat()
//
//        val density  = LocalDensity.current
//        val boardWpx = with(density) { boardW.toPx() }
//        val boardHpx = with(density) { boardH.toPx() }
//        val slackPx  = with(density) { slackDp.toPx() }
//
//        fun clampOffset(newOffset: Offset, newScale: Float)
//                : Offset {
//
//            val scaledW = boardWpx * newScale
//            val scaledH = boardHpx * newScale
//
//            // If board is smaller than viewport, allow it to float around centered (still with slack).
//            // If board is bigger, allow symmetric overscroll via slackPx.
//            val baseMinX = viewportW - scaledW
//            val baseMinY = viewportH - scaledH
//
//            val minX = if (scaledW <= viewportW) {
//                ((viewportW - scaledW) / 2f) - slackPx
//            } else {
//                baseMinX - viewportW
//            }
//            val maxX = if (scaledW <= viewportW) {
//                ((viewportW - scaledW) / 2f) + slackPx
//            } else {
//                0f + viewportW
//            }
//
//            val minY = if (scaledH <= viewportH) {
//                ((viewportH - scaledH) / 2f) - slackPx
//            } else {
//                baseMinY - viewportH
//            }
//            val maxY = if (scaledH <= viewportH) {
//                ((viewportH - scaledH) / 2f) + slackPx
//            } else {
//                0f + viewportH
//            }
//
//            return Offset(
//                x = newOffset.x.coerceIn(minX, maxX),
//                y = newOffset.y.coerceIn(minY, maxY),
//            )
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .pointerInput(Unit) {
//                    detectTransformGestures { _, pan, zoom, _ ->
//                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
//                        val newOffset = offset + pan
//                        scale = newScale
//                        offset = clampOffset(newOffset, newScale)
//                    }
//                }
//        ) {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(cols),
//                userScrollEnabled = false,
//                modifier = Modifier
//                    .requiredSize(boardW, boardH)
//                    .graphicsLayer {
//                        // IMPORTANT: makes clamp math behave intuitively
//                        transformOrigin = TransformOrigin(0f, 0f)
//
//                        scaleX = scale
//                        scaleY = scale
//                        translationX = offset.x
//                        translationY = offset.y
//                    },
//                horizontalArrangement = Arrangement.spacedBy(0.dp),
//                verticalArrangement = Arrangement.spacedBy(0.dp),
//            ) {
//
//
//                items(cells, key = { it.gid }) { cell ->
//
//                    var bg = Color(0xFF282C34)
//                    var txt = ""
//                    var fg = Color(0xFFFFFFFF)
//
//                    // •
//                    // https://materialui.co/colors
//                    // https://material-theme.com/docs/reference/color-palette/
//
//                    val flagBlueColor = Color(0xFF61AEEF)
//                    val revealedFont = Color(0xFFFFFFFF)
//                    val revealedBackground = Color(0xFF4D515D)
//                    val notRevealedBackground = Color(0xFF282C34)
//                    val explodedBackground = Color(0xFFf78c6c) // 0xFF9B859D 0xFF80cbc4
//                    val incorrectPinkColor = Color(0xFFc792ea)
//                    val probColor = Color(0xff7286BF)
//                    val ruleColor = Color(0xffbcc8eb)
//
//                    if (cell.isRevealed) {
//                        if (cell.adjacentMines == 0) {
//                            bg = revealedBackground
//                            fg = revealedBackground
//                            txt = ""
//                        } else if (cell.adjacentMines >= 1) {
//                            bg = revealedBackground
//                            fg = if (cell.conflict) incorrectPinkColor else
//                                (if (settings.glyphMode == GlyphMode.COLORS)
//                                    Color(state.colors.get(cell.adjacentMines-1))
//                                    else revealedFont)
//                            txt = state.glyphs.get(cell.adjacentMines-1)
//                        } else if (cell.isMine) {
//                            if (cell.isExploded) {
//                                bg = explodedBackground
//                                fg = revealedFont
//                                txt = "@"
//                            }
//                            else {
//                                bg = if (cell.isFlagged) flagBlueColor else incorrectPinkColor
//                                fg = revealedFont
//                                txt = if (cell.isFlagged) "F" else "X"
//                            }
//                        }
//                    } else { // not revealed
//                        if (cell.isExploded) {  // unflagged mine
//                            if (cell.isExplodedGid) {
//                                bg = explodedBackground
//                                fg = revealedFont
//                                txt = "@"
//                            } else if (cell.isFlagged) {
//                                bg = flagBlueColor
//                                fg = revealedFont
//                                txt = "F"
//                            } else {
//                                bg = incorrectPinkColor
//                                fg = revealedFont
//                                txt = "F"
//                            }
//                        }
//                        else if (cell.isFlagged) {
//                            if ((ui.isVerify or ui.gameOver) and !cell.isMine) {
//                                bg = incorrectPinkColor
//                                fg = revealedFont
//                                txt = "F"
//                            } else {
//                                bg = flagBlueColor
//                                fg = revealedFont
//                                txt = "F"
//                            }
//                        }
//                        else if (isEnumerate) {
//                            if (cell.conflict) {
//                                bg = notRevealedBackground
//                                fg = incorrectPinkColor
//                                txt = "C"
//                            } else if (cell.forcedFlag) {
//                                bg = notRevealedBackground
//                                fg = ruleColor
//                                txt = "X"
//                            } else if (cell.forcedOpen) {
//                                bg = notRevealedBackground
//                                fg = ruleColor
//                                txt = "O"
//                            } else if (ui.overlay.isConsistent && cell.probability != null) {
//                                bg = notRevealedBackground
//                                fg = probColor
//                                txt = probabilityToGlyph(cell.probability)
//                            } else {
//                                bg = notRevealedBackground
//                                fg = revealedFont
//                                txt = ""
//                            }
//                        }
//                    }
//
//                    Box(
//                        modifier = Modifier
//                            .padding(4.dp)
//                            .border(
//                                width = 4.dp,
//                                color = notRevealedBackground)
//                            .background(notRevealedBackground)
//                             ,
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .size(cellSize)
//                                .border(
//                                    width= 2.dp,
//                                    color = incorrectPinkColor, //notRevealedBackground,
//                                    shape = RoundedCornerShape(5))
//                                .background(
//                                    color = bg,
//                                    shape = RoundedCornerShape(5))
//                                .combinedClickable(
//                                    onClick = { onCell(cell.gid) },
//                                    onLongClick = { onCellLongPress(cell.gid) }
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(txt, color = fg)
//                        }
//                    }
//                }
//            }
//        }
//    }
}

@Composable
fun VisualSettingsScreen(
    settingsFlow: StateFlow<VisualSettings>,
    onChange: (VisualSettings) -> Unit,
    onDismiss: () -> Unit,
    visualState: StateFlow<VisualState>
) {
    val settings by settingsFlow.collectAsState()
    val state by visualState.collectAsState()

    val scrollStateVert = rememberScrollState()

    Column(Modifier.fillMaxSize()
        .verticalScroll(scrollStateVert)
        .padding(16.dp)) {

        Spacer(Modifier.height(12.dp))
        Text("Cell Glyphs", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        ModeSelector(
            mode = settings.glyphMode,
            onMode = { onChange(settings.copy(glyphMode = it)) }
        )

        Spacer(Modifier.height(12.dp))

        when (settings.glyphMode) {
            GlyphMode.NUMERALS -> {
                GlyphSetPicker(
                    title = "Numeral Set",
                    sets = VisualCatalog.numeralSets,
                    selectedId = settings.numeralSetId,
                    onSelect = { onChange(settings.copy(numeralSetId = it)) }
                )

                Spacer(Modifier.width(12.dp))

                val glyphs = VisualCatalog.numeral(settings.numeralSetId).glyphs.take(8)
                val scrollStateHorz = rememberScrollState()
                Box(
                    Modifier
                        .horizontalScroll(scrollStateHorz)
                        .fillMaxWidth()
                ) {
                    val boxSize = PreviewGlyphGrid(glyphs)
                    settings.copy(glyphSize = boxSize)
                }

                Spacer(Modifier.height(8.dp))
                state.copy(glyphs = glyphs)
            }

            GlyphMode.ALPHABET -> {
                GlyphSetPicker(
                    title = "Alphabet Set",
                    sets = VisualCatalog.alphaSets,
                    selectedId = settings.alphaSetId,
                    onSelect = { onChange(settings.copy(alphaSetId = it)) }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = settings.shuffleGlyphs,
                        onCheckedChange = { onChange(settings.copy(shuffleGlyphs = it)) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Shuffle")
                }

                Spacer(Modifier.width(12.dp))
                val glyphs = if (settings.shuffleGlyphs)
                    VisualCatalog.alpha(settings.alphaSetId).glyphs.shuffled().take(8) else
                        VisualCatalog.alpha(settings.alphaSetId).glyphs.take(8)

                val scrollStateHorz = rememberScrollState()
                Box(
                    Modifier
                        .horizontalScroll(scrollStateHorz)
                        .fillMaxWidth()
                ) {
                    val boxSize = PreviewGlyphGrid(glyphs)
                    settings.copy(glyphSize = boxSize)
                }

                Spacer(Modifier.height(8.dp))
                state.copy(glyphs = glyphs)
            }

            GlyphMode.COLORS -> {
                ColorSetPicker(
                    title = "Color Set",
                    sets = VisualCatalog.colorSets,
                    selectedId = settings.colorSetId,
                    onSelect = { onChange(settings.copy(colorSetId = it)) }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = settings.shuffleColors,
                        onCheckedChange = { onChange(settings.copy(shuffleColors = it)) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Shuffle")
                }
                val colors = if (settings.shuffleColors)
                    VisualCatalog.colors(settings.colorSetId).colors.shuffled() else
                        VisualCatalog.colors(settings.colorSetId).colors

                PreviewRowColors(colors)
                Spacer(Modifier.height(8.dp))
                state.copy(colors = colors)
            }
        }
        Button( onClick = onDismiss, ) {
            Text("Close")
        }
    }
}

@Composable
fun MeasuredGlyph(
    glyph: String,
    fontSize: Float,
    onHeightMeasured: (Int) -> Unit
) {
    Text(
        glyph,
        fontSize = fontSize.sp,
        modifier = Modifier.onGloballyPositioned { coords ->
            onHeightMeasured(coords.size.height)  // height in px
        }.padding(horizontal = 5.dp, vertical = 4.dp)
    )
}


@Composable
private fun ModeSelector(mode: GlyphMode, onMode: (GlyphMode) -> Unit) {
    Column {
        Text("Mode", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        GlyphMode.entries.forEach { m ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (mode == m), onClick = { onMode(m) })
                Spacer(Modifier.width(8.dp))
                Text(m.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
private fun GlyphSetPicker(
    title: String,
    sets: List<GlyphSet>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))

    // Simple dropdown-ish list; later you can swap to ExposedDropdownMenuBox.
    sets.forEach { set ->
        val selected = set.id == selectedId
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .then(if (selected) Modifier else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = { onSelect(set.id) })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(set.displayName)
                if (set.description.isNotBlank()) {
                    Text(set.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ColorSetPicker(
    title: String,
    sets: List<ColorSet>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))

    sets.forEach { set ->
        val selected = set.id == selectedId
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = { onSelect(set.id) })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(set.displayName)
                if (set.description.isNotBlank()) {
                    Text(set.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun PreviewRowStrings(items: List<String>,
                              fontSize: Float,
                              onGlyphHeight: (Int) -> Unit) {
    Spacer(Modifier.height(10.dp))
    Text("Preview", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { s ->

            Box {
                MeasuredGlyph(s, fontSize) { heightPx ->
                    onGlyphHeight(heightPx)
                }
            }

//            Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.small) {
//                Text(
//                    text = s,
//                    fontSize = fontSize.sp,
//                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
//                )
//            }
        }
    }
}

@Composable
fun PreviewGlyphGrid(
    glyphs: List<String>
): Int {
    var maxHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        glyphs.forEach { glyph ->
            Box( modifier = Modifier
                .padding(3.dp) .border(1.dp, Color.Gray), contentAlignment = Alignment.Center ) {
                Text(
                    text = glyph,
                    fontSize = 24.sp,
                    modifier = Modifier.onGloballyPositioned { coords ->
                        val h = coords.size.height
                        val w = coords.size.width
                        if (h > maxHeightPx) maxHeightPx = h
                        if (w > maxHeightPx) maxHeightPx = w
                    }
                )
            }
        }
    }
    val boxSize = with(density) { maxHeightPx.toDp().coerceAtLeast(24.dp) }
    return maxHeightPx
}


@Composable
private fun PreviewRowColors(items: List<Long>) {
    Spacer(Modifier.height(10.dp))
    Text("Preview", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { argb ->
            Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.small) {
                Box(
                    Modifier
                        .size(26.dp)
                        .background(Color(argb))
                )
            }
        }
    }
}


@Composable
fun VisualSettingsSheet(
    vm: GameViewModel,
    onClose: () -> Unit,
    numeralSets: List<GlyphSet>,
    alphaSets: List<GlyphSet>,
    palettes: List<ColorSet>,
) {
    val s by vm.visualSettings.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Visual Settings", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))

        // Mode picker (simple)
        Row {
            ModeChip("Numbers", s.glyphMode == GlyphMode.NUMERALS) {
                vm.updateVisualSettings { it.copy(glyphMode = GlyphMode.NUMERALS) }
            }
            Spacer(Modifier.width(8.dp))
            ModeChip("Alphabet", s.glyphMode == GlyphMode.ALPHABET) {
                vm.updateVisualSettings { it.copy(glyphMode = GlyphMode.ALPHABET) }
            }
            Spacer(Modifier.width(8.dp))
            ModeChip("Colors", s.glyphMode == GlyphMode.COLORS) {
                vm.updateVisualSettings { it.copy(glyphMode = GlyphMode.COLORS) }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (s.glyphMode) {
            GlyphMode.NUMERALS -> {
                SetDropdown(
                    label = "Numeral set",
                    selectedId = s.numeralSetId,
                    options = numeralSets.map { it.id to it.displayName },
                    onPick = { id -> vm.updateVisualSettings { it.copy(numeralSetId = id) } }
                )
                ToggleRow("Shuffle glyphs", s.shuffleGlyphs) { v ->
                    vm.updateVisualSettings { it.copy(shuffleGlyphs = v) }
                }
                PreviewRow("Preview", previewGlyphs(numeralSets.first { it.id == s.numeralSetId }, count = 8))
            }

            GlyphMode.ALPHABET -> {
                SetDropdown(
                    label = "Alphabet set",
                    selectedId = s.alphaSetId,
                    options = alphaSets.map { it.id to it.displayName },
                    onPick = { id -> vm.updateVisualSettings { it.copy(alphaSetId = id) } }
                )
                ToggleRow("Shuffle glyphs", s.shuffleGlyphs) { v ->
                    vm.updateVisualSettings { it.copy(shuffleGlyphs = v) }
                }
                PreviewRow("Preview", previewAlpha(alphaSets.first { it.id == s.alphaSetId }, count = 8))
            }

            GlyphMode.COLORS -> {
                SetDropdown(
                    label = "Color palette",
                    selectedId = s.colorSetId,
                    options = palettes.map { it.id to it.displayName },
                    onPick = { id -> vm.updateVisualSettings { it.copy(colorSetId = id) } }
                )
                ToggleRow("Shuffle colors", s.shuffleColors) { v ->
                    vm.updateVisualSettings { it.copy(shuffleColors = v) }
                }
                // preview chips optional
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onClose) { Text("Done") }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SetDropdown(
    label: String,
    selectedId: String,
    options: List<Pair<String, String>>, // id to display
    onPick: (String) -> Unit
) {
    // keep it simple; you can replace with ExposedDropdownMenuBox later
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        options.forEach { (id, name) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onPick(id) }
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (id == selectedId), onClick = { onPick(id) })
                Spacer(Modifier.width(8.dp))
                Text(name)
            }
        }
    }
}

private fun previewGlyphs(set: GlyphSet, count: Int): String {
    // show first N glyphs for quick preview
    return set.glyphs.take(count).joinToString("  ")
}

private fun previewAlpha(set: GlyphSet, count: Int): String {
    return set.glyphs.take(count).joinToString("  ")
}

@Composable
private fun PreviewRow(label: String, preview: String) {
    Spacer(Modifier.height(8.dp))
    Text(label, style = MaterialTheme.typography.labelLarge)
    Text(preview)
}
