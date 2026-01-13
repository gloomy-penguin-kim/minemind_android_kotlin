package com.kim.minemind.ui.state

import androidx.lifecycle.ViewModel
import com.kim.minemind.analysis.Analyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.kim.minemind.core.*
import com.kim.minemind.analysis.Solver
import com.kim.minemind.analysis.AnalyzerOverlay
import com.kim.minemind.core.board.Board
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.core.history.HistoryEntry
import com.kim.minemind.core.history.HistoryStack

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.kim.minemind.core.board.restoreFromSnapshot
import com.kim.minemind.core.history.toSnapshot
import com.kim.minemind.core.board.toSnapshot
import com.kim.minemind.core.history.HistoryEvent
import com.kim.minemind.shared.ConflictDelta
import com.kim.minemind.shared.ConflictList
import com.kim.minemind.shared.snapshot.BoardSnapshot
import com.kim.minemind.shared.snapshot.CellSnapshot
import com.kim.minemind.shared.snapshot.GameSnapshot
import com.kim.minemind.ui.settings.GlyphMode
import com.kim.minemind.ui.settings.VisualResolver
import com.kim.minemind.ui.settings.VisualState
import com.kim.minemind.ui.settings.VisualSettings
import com.kim.minemind.ui.settings.VisualSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

import kotlinx.serialization.json.Json

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class GameViewModel(
    private val settingsRepo: VisualSettingsRepository,
    private val visualResolver: VisualResolver,
    private val gameStateRepo: GameStateRepository
) : ViewModel(
) {

    private var board: Board = Board(rows = 25, cols = 25, mines = 200, seed = 0)
    private var solver: Solver = Solver()
    private val analyzer: Analyzer = Analyzer()
    private val history = HistoryStack()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    val cells = mutableStateListOf<CellUI>()


//    private fun idx(r: Int, c: Int, cols: Int) = r * cols + c

    // persisted settings (DataStore)


    companion object {
        private const val KEY_GAME_SNAPSHOT = "game_snapshot" // whatever you store
        private const val TAG = "ui.GameViewModel"
    }
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private fun buildSnapshot(): GameSnapshot =
        GameSnapshot(
            board = board.toSnapshot(),
            history = history.toSnapshot()
        )
    //    Save after truth changes
    //    dispatch()
    //    undo()
    //    newGame()

    fun updateVisualSettings(newSettings: VisualSettings) {
        viewModelScope.launch {
            settingsRepo.set(newSettings)
        }
    }

    val visualSettings: StateFlow<VisualSettings> =
        settingsRepo.settingsFlow
            .stateIn(viewModelScope, SharingStarted.Eagerly, VisualSettings())

    // derived visuals (what the board/UI uses to draw labels)
    val visualState: StateFlow<VisualState> =
        visualSettings
            .map { s -> visualResolver.resolve(s, Random(board.seed.toLong())) }
            .stateIn(viewModelScope, SharingStarted.Eagerly,
                VisualState(glyphs = (1..8).map { it.toString() }, colors = emptyList())
            ) // as StateFlow<VisualState>

    fun updateVisualSettings(transform: (VisualSettings) -> VisualSettings) {
        viewModelScope.launch {
            val newSettings = transform(visualSettings.value)
            settingsRepo.set(newSettings)
        }
    }


    // Example setters you’ll call from the modal:
    fun setStyle(style: GlyphMode) = updateVisualSettings { it.copy(glyphMode = style) }
    fun setNumeralSet(id: String) = updateVisualSettings { it.copy(numeralSetId = id) }
    fun setAlphaSet(id: String) = updateVisualSettings { it.copy(alphaSetId = id) }
    fun setShuffleGlyphs(on: Boolean) = updateVisualSettings { it.copy(shuffleGlyphs = on) }
    fun setShuffleColors(on: Boolean) = updateVisualSettings { it.copy(shuffleColors = on) }


    init {
        newGame(rows=25, cols=25, mines=150)
        viewModelScope.launch {
            val encoded = gameStateRepo.snapshotFlow.first()
            if (encoded.isNullOrBlank()) {
                newGame(rows = 25, cols = 25, mines = 150)
                return@launch
            }
            runCatching {
                val snap = json.decodeFromString(GameSnapshot.serializer(), encoded)
                restoreFromSnapshot(snap)
            }.onFailure {
                newGame(rows = 25, cols = 25, mines = 150)
            }
        }
    }

    private fun restoreFromSnapshot(snap: GameSnapshot) {
        board = Board(
            rows = snap.board.rows,
            cols = snap.board.cols,
            mines = snap.board.mines,
            seed = snap.board.seed
        )
        board.restoreFromSnapshot(snap.board)
    }

    private fun persist(encoded: String) {
        viewModelScope.launch {
            gameStateRepo.save(encoded)
        }
    }

    private fun persistSnapshot() {
        val snap = buildSnapshot()
        val encoded = json.encodeToString(GameSnapshot.serializer(), snap)

        viewModelScope.launch {
            gameStateRepo.save(encoded)
        }
    }

    fun newGame(rows: Int, cols: Int, mines: Int) {
        cells.clear()

        solver.clear()
        analyzer.clear()
        history.clear()
        board = Board(rows, cols, mines, seed = 0)

        cells.addAll(buildCells(board, analyzer.analyze(board)))
        _uiState.value = GameUiState(
            rows = rows,
            cols = cols,
            mines = mines,
            moves = 0,
            cells = cells
        )
    }
    fun handleTopMenu(action: TopMenuAction) {
        if (action == TopMenuAction.NEW) {
            newGame(rows=25, cols= 25, mines=150)
        }
    }

    fun setTapMode(m: TapMode) {
        _uiState.update { it.copy(tapMode = m) }
    }

    private var overlayJob: Job? = null

    fun dispatch(action: Action?, gid: Int) {
        if (action == null || board.gameOver) return

        val ui = _uiState.value
        val beforeMoves = ui.moves
        val beforeRemainingSafe = board.remainingSafe

        // 1. Apply truth update (fast)
        val cs = board.apply(action, gid)
        if (cs.empty) return

        // Record in history (cheap)
        history.push(
            HistoryEntry(
                event = HistoryEvent.UserCommand(action, gid),
                changes = cs,
                moveCountBefore = beforeMoves,
                remainingSafeBefore = beforeRemainingSafe
            )
        )

        // 2. Update the UI immediately, *without* solver
        // This makes the click feel instant
        patchCells(
            cs = cs,
            board = board,
            overlay = AnalyzerOverlay()
        )
        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = cells
            )
        }

        // If not in enumeration mode, nothing more to compute
        if (!ui.isEnumerate || cs.gameOver) {
            persistSnapshot()
            return
        }

        // 3. Cancel previous job (if user clicks fast)
        overlayJob?.cancel()

        // 4. Launch solver + conflicts async
        overlayJob = viewModelScope.launch(Dispatchers.Default) {

            // This is the expensive work:
            val overlay = analyzer.analyze(board)
            val conflictDelta = board.conflictsByGid(cs.getAllGid())
            val conflictBoard = ui.conflictBoard.applyDelta(conflictDelta)

            // Switch back to main thread for UI update
            withContext(Dispatchers.Main) {
                val latest = _uiState.value
                patchCells(
                    cs = cs,
                    board = board,
                    overlay = overlay,
                    conflictsBoard = conflictBoard,
                    clearConflicts = conflictDelta.removes
                )
                _uiState.update { s ->
                    s.copy(
                        cells = cells,
                        overlay = overlay,
                        ruleList = overlay.ruleList,
                        conflictBoard = conflictBoard,
                        conflictProbs = overlay.conflictProbs,
                    )
                }

                persistSnapshot()
            }
        }
    }


    fun dispatch2(action: Action?, gid: Int) {
        if (action == null) return
        val beforeMoves = _uiState.value.moves
        val beforeRemainingSafe = board.remainingSafe

        val cs = board.apply(action, gid) // <-- TRUTH ONLY
        Log.d(TAG, "cs = $cs")
        if (cs.empty) return
        history.push(
            HistoryEntry(
                event = HistoryEvent.UserCommand(action, gid),
                changes = cs,
                moveCountBefore = beforeMoves,
                remainingSafeBefore = beforeRemainingSafe
            )
        )

        // TODO:  maybe separate out prob and rules so maybe they can be ran concurrently..?

        val overlay = if (!board.gameOver and _uiState.value.isEnumerate) analyzer.analyze(board) else AnalyzerOverlay()
        val conflictDelta: ConflictDelta = if (!board.gameOver and _uiState.value.isEnumerate) board.conflictsByGid(cs.getAllGid()) else ConflictDelta()
        val conflictBoard = _uiState.value.conflictBoard.applyDelta(conflictDelta)

//        viewModelScope.launch(Dispatchers.Default) {
//            val overlay = analyzer.analyze(board)
//            val conflictDelta = board.conflictsByGid(cs.getAllGid())
//
//            withContext(Dispatchers.Main) {
//                applyOverlayToUi(overlay, conflictDelta)
//            }
//        }



        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "overlay.conflictProbs = ${overlay.conflictProbs}")
        Log.d(TAG, "conflictBoard = ${conflictBoard}")

        patchCells(
            cs = cs,
            board = board,
            overlay = overlay,
            conflictsBoard = conflictBoard,
            clearConflicts = conflictDelta.removes
        )
        Log.d(TAG, "cells = ${cells}")

        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = cells,
                overlay = overlay,
                ruleList = overlay.ruleList,
                conflictBoard = conflictBoard,
                conflictProbs = overlay.conflictProbs
            )
        }
    }

    private fun patchCells(
        cs: ChangeSet,
        board: Board,
        overlay: AnalyzerOverlay,
        conflictsBoard: ConflictList = ConflictList(),
        clearConflicts: Set<Int> = emptySet()
    )  {
        // 1. Build the set of changed gids (same as before)
        val changed = HashSet<Int>().apply {
            addAll(cs.revealed)
            addAll(cs.flagged)
            addAll(cs.exploded)

            addAll(overlay.forcedFlags)
            addAll(overlay.forcedOpens)
            addAll(overlay.probabilities.keys)
            addAll(overlay.conflictProbs.keys)

            addAll(conflictsBoard.keys)
            addAll(clearConflicts)
        }

        // 2. Update only the changed cells
        for (gid in changed) {
            val bc = board.cells[gid]

            cells[gid] = CellUI(
                gid = gid,

                isMine = bc.isMine,
                isRevealed = bc.isRevealed,
                isFlagged = bc.isFlagged,
                isExploded = bc.isExploded,
                isExplodedGid = bc.isExplodedGid,

                adjacentMines = bc.adjacentMines,

                conflict = (gid in overlay.conflictProbs.keys) ||
                            (gid in conflictsBoard.keys),

                probability = overlay.probabilities[gid],

                forcedOpen = gid in overlay.forcedOpens,
                forcedFlag = gid in overlay.forcedFlags
            )
        }
    }


    private fun autoBuildCells(
        totalMoves: Int,
        board: Board,
        overlay: AnalyzerOverlay,
        cs: ChangeSet,
        conflictDelta: ConflictDelta,
        conflictBoard: ConflictList
    ) {
        patchCells(
            cs = cs,
            board = board,
            overlay = overlay,
            conflictsBoard = conflictBoard,
            clearConflicts = conflictDelta.removes
        )
        Log.d(TAG, "cells = ${cells}")

        _uiState.update { s ->
            s.copy(
                moves = totalMoves,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = cells,
                overlay = overlay,
                ruleList = overlay.ruleList,
                conflictBoard = conflictBoard,
                conflictProbs = overlay.conflictProbs
            )
        }
    }

    // tODO:  fix autobots
    fun auto() {
        if (board.gameOver) return

        Log.d(TAG, "autobot")

        var overlay = _uiState.value.overlay
        val beforeMoves = _uiState.value.moves
        val beforeRemainingSafe = board.remainingSafe

        var prevTotalMoves = 0
        var totalMoves = 0
        var totalChanges = ChangeSet()
        do {
            overlay = analyzer.runRulesOnly(board, stopAfterOne = false)
            Log.d(TAG, "overlay is $overlay")
            prevTotalMoves = totalMoves
            for (ruleAction in overlay.ruleActions) {
                if ((ruleAction.value == Action.OPEN) and board.isMine(ruleAction.key)) continue
                if ((ruleAction.value == Action.FLAG) and !board.isMine(ruleAction.key)) continue

                val cs = board.apply(ruleAction.value, ruleAction.key)
                autoBuildCells(totalMoves, board, overlay, cs, ConflictDelta(), _uiState.value.conflictBoard)
                totalChanges = totalChanges.merged(cs)
                totalMoves += 1
                Log.d(TAG, "ruleAction = ${ruleAction.value} ${ruleAction.key}")
                Log.d(TAG, "cs is $cs")
            }
        } while ((totalMoves != prevTotalMoves) and !totalChanges.gameOver)

        if (totalChanges.empty) return

        history.push(
            HistoryEntry(
                event = HistoryEvent.ApplyRecommendations(totalMoves),
                changes = totalChanges,
                moveCountBefore = beforeMoves,
                remainingSafeBefore = beforeRemainingSafe
            )
        )

        if (!totalChanges.gameOver and _uiState.value.isEnumerate) {
            overlay = analyzer.analyze(board)

            val conflictDelta =  board.conflictsByGid(totalChanges.getAllGid())
            val conflictBoard = _uiState.value.conflictBoard.applyDelta(conflictDelta)
            patchCells(
                cs = totalChanges,
                board = board,
                overlay = overlay,
                conflictsBoard = conflictBoard,
                clearConflicts = conflictDelta.removes
            )
            _uiState.update { s ->
                s.copy(
                    moves = beforeMoves + totalMoves,
                    gameOver = totalChanges.gameOver,
                    win = totalChanges.win,
                    cells = cells,
                    overlay = overlay,
                    ruleList = overlay.ruleList,
                    conflictBoard = conflictBoard,
                    conflictProbs = overlay.conflictProbs
                )
            }
        }
    }

    fun hint(gid: Int) { }

    fun verify() {
        val v = _uiState.value.isVerify
        _uiState.update { s ->
            s.copy(
                isVerify = !v
            )
        }
    }

    fun enumerate() {
        val current = _uiState.value
        val enabled = current.isEnumerate

        // -------------------------
        // 1. Disabling enumerate
        // -------------------------
        if (enabled) {
            _uiState.update { it.copy(isEnumerate = false) }
            persistSnapshot()
            return
        }

        // -------------------------
        // 2. Enabling enumerate
        // -------------------------
        _uiState.update { it.copy(isEnumerate = true) }

        // Cancel any pending background solver
        overlayJob?.cancel()

        // Run analyzer on background thread
        overlayJob = viewModelScope.launch(Dispatchers.Default) {
            val overlay = analyzer.analyze(board)

            // Build a fresh conflictBoard for ALL cells
            val conflictDelta = board.conflictsByGid(
                (0 until board.rows * board.cols).toSet()
            )
            val conflictBoard = conflictDelta.upserts

            withContext(Dispatchers.Main) {

                // Incrementally update cells from overlay & conflict
                patchCells(
                    cs = ChangeSet(),      // no truth changes
                    board = board,
                    overlay = overlay,
                    conflictsBoard = conflictBoard
                )

                // Update the rest of the UI state
                _uiState.update { s ->
                    s.copy(
                        overlay = overlay,
                        ruleList = overlay.ruleList,
                        conflictBoard = conflictBoard,
                        conflictProbs = overlay.conflictProbs
                    )
                }

                persistSnapshot()
            }
        }
    }


    fun undo() {
        val entry = history.pop() ?: return
        board.undo(entry)   // restore truth only

        // 1. Cancel solver job if active
        overlayJob?.cancel()

        // 2. Remove overlay immediately while recomputing
        _uiState.update { s ->
            s.copy(
                moves = entry.moveCountBefore,
                gameOver = false,
                win = false
            )
        }

        // 3. Launch background solver + conflicts
        overlayJob = viewModelScope.launch(Dispatchers.Default) {

            // Recompute overlay only if enumerate mode is ON
            val overlay = if (_uiState.value.isEnumerate)
                analyzer.analyze(board)
            else AnalyzerOverlay()

            // Recompute conflicts (truth changed)
            val conflictDelta =
                if (_uiState.value.isEnumerate)
                    board.conflictsByGid((0 until board.rows * board.cols).toSet())
                else ConflictDelta()

            val conflictBoard = conflictDelta.upserts

            withContext(Dispatchers.Main) {

                // 4. Incrementally update only changed truth cells
                patchCells(
                    cs = entry.changes,      // the reverse of what was applied originally
                    board = board,
                    overlay = overlay,
                    conflictsBoard = conflictBoard,
                    clearConflicts = conflictDelta.removes
                )

                // 5. Update UI state metadata
                _uiState.update { s ->
                    s.copy(
                        overlay = overlay,
                        ruleList = overlay.ruleList,
                        conflictBoard = conflictBoard,
                        conflictProbs = overlay.conflictProbs
                    )
                }

                persistSnapshot()
            }
        }
    }


    private fun buildCells(
        b: Board,
        overlay: AnalyzerOverlay = AnalyzerOverlay(),
        conflictBoard: ConflictList = ConflictList()
    ): SnapshotStateList<CellUI> {

        val cells = mutableStateListOf<CellUI>()
//        cells.ensureCapacity(b.rows * b.cols)

        val conflictKeys = conflictBoard.keys
        val probKeys = overlay.probabilities.keys
        val forcedOpen = overlay.forcedOpens
        val forcedFlag = overlay.forcedFlags
        val overlayConflicts = overlay.conflictProbs.keys

        for (gid in 0 until b.rows * b.cols) {
            val bc = b.cells[gid]

            cells.add(
                CellUI(
                    gid = gid,

                    isMine = bc.isMine,
                    isRevealed = bc.isRevealed,
                    isFlagged = bc.isFlagged,
                    isExploded = bc.isExploded,
                    isExplodedGid = bc.isExplodedGid,

                    adjacentMines = bc.adjacentMines,

                    probability = overlay.probabilities[gid],

                    forcedOpen = gid in forcedOpen,
                    forcedFlag = gid in forcedFlag,

                    conflict = (gid in overlayConflicts) || (gid in conflictKeys)
                )
            )
        }

        return cells
    }



//
//    private fun buildCells(
//        b: Board,
//        overlay: AnalyzerOverlay = AnalyzerOverlay(),
//        conflictBoard: ConflictList = ConflictList(),
//        clearConflicts: Set<Int> = emptySet()
//    ): List<CellUI> {
//        val out = ArrayList<CellUI>(b.rows * b.cols)
//        for (gid in 0 until (b.rows * b.cols)) {
//            val bc = b.cells[gid]
//            out.add(
//                CellUI(
//                    gid = gid,
//
//                    isMine = bc.isMine,
//                    isRevealed = bc.isRevealed,
//                    isFlagged = bc.isFlagged,
//                    isExploded = bc.isExploded,
//                    adjacentMines = bc.adjacentMines,
//
//                    probability = overlay.probabilities.get(gid),
//
//                    forcedOpen = gid in overlay.forcedOpens,
//                    forcedFlag = gid in overlay.forcedFlags,
//
//                    conflict = (gid in overlay.conflictProbs.keys) or (gid in conflictBoard.keys),
//                )
//            )
//        }
//        return out
//    }





}

