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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.kim.minemind.core.board.Cell


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameScreen(vm: GameViewModel) {
    val ui by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MineMind") },
                actions = {
                    Text("Moves: ${ui.moves}", modifier = Modifier.padding(end = 12.dp))
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = ui.flagMode,
                        onClick = { vm.toggleFlagMode() },
                        label = { Text(if (ui.flagMode) "Flag mode" else "Open mode") }
                    )
                    Button(onClick = { vm.undo() }) { Text("Undo") }
                    Button(onClick = { vm.newGame(ui.rows, ui.cols, ui.mines) }) { Text("New") }
                }
            }
        }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(12.dp)) {
            if (ui.gameOver) {
                val msg = if (ui.win) "You won!" else "Boom. You lost."
                Text(msg, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
            }

            BoardGrid(
                cols = ui.cols,
                cells = ui.cells,
                onCell = { r, c ->
                    val action = if (ui.flagMode) Action.FLAG else Action.OPEN
                    vm.dispatch(action, r, c)
                },
                onCellLongPress = { r, c ->
                    vm.dispatch(Action.FLAG, r, c)
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun BoardGrid(
    cols: Int,
    cells: List<Cell>,
    onCell: (Int, Int) -> Unit,
    onCellLongPress: (Int, Int) -> Unit,
) {
    val cellSize = 34.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = true
    ) {
        items(cells) { cell ->

            val p = (cell.probability ?: 0f) // 0..1, or null if unknown
            val bg = when {
                cell.isRevealed || cell.isFlagged ->
                    MaterialTheme.colorScheme.surfaceVariant

//                cell.probability != null ->
//                    probToBg(
//                        p = p,
//                        base = MaterialTheme.colorScheme.surfaceTint,          // light
//                        hot  = MaterialTheme.colorScheme.surfaceVariant    // darker
//                    )

                else ->
                    MaterialTheme.colorScheme.surface
            }

            Box(
                modifier = Modifier
                    .size(cellSize)
                    .padding(1.dp)
                    .background(bg)
                    .combinedClickable(
                        onClick = { onCell(cell.row, cell.col) },
                        onLongClick = { onCellLongPress(cell.row, cell.col) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val txt = when {
                    cell.isFlagged -> "F"
                    cell.isRevealed and (cell.adjacentMines == -1) -> "*"
                    cell.isRevealed and (cell.adjacentMines == 0) -> ""
                    cell.isRevealed and (cell.adjacentMines >= 1) -> cell.adjacentMines.toString()
                    cell.isRevealed -> ""
                    else -> "?"
                }
                Text(txt)
            }
        }
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