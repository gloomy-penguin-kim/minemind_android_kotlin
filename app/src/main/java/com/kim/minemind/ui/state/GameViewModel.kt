package com.kim.minemind.ui.state

import androidx.lifecycle.ViewModel
import com.kim.minemind.analysis.AnalysisConfig
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
import androidx.lifecycle.viewModelScope
import com.kim.minemind.core.history.HistoryEvent
import com.kim.minemind.shared.ConflictDelta
import com.kim.minemind.shared.ConflictList
import com.kim.minemind.ui.settings.GlyphMode
import com.kim.minemind.ui.settings.VisualResolver
import com.kim.minemind.ui.settings.VisualState
import com.kim.minemind.ui.settings.VisualSettings
import com.kim.minemind.ui.settings.VisualSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Random

class GameViewModel(
    private val settingsRepo: VisualSettingsRepository,
    private val visualResolver: VisualResolver,
    private val savedStateRepo: GameStateRepository
) : ViewModel(
) {

    private var board: Board = Board(rows = 25, cols = 25, mines = 200, seed = 0)
    private var solver: Solver = Solver()
    private val analyzer: Analyzer = Analyzer()
    private val history = HistoryStack()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

//    private fun idx(r: Int, c: Int, cols: Int) = r * cols + c

    // persisted settings (DataStore)


    companion object {
        private const val KEY_GAME_SNAPSHOT = "game_snapshot" // whatever you store
        private const val TAG = "ui.GameViewModel"
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
        newGame(rows=25, cols= 25, mines=150)
        val snapshot = savedStateRepo.get<String>(KEY_GAME_SNAPSHOT)
        if (snapshot != null) {
            // restoreGame(snapshot)
        } else {
            // startNewGame()
        }
    }

    private fun persist(snapshot: String) {
        savedStateHandle[KEY_GAME_SNAPSHOT] = snapshot
    }
    fun newGame(rows: Int, cols: Int, mines: Int) {
        solver.clear()
        analyzer.clear()
        history.clear()
        board = Board(rows, cols, mines, seed = 0)
        _uiState.value = GameUiState(
            rows = rows,
            cols = cols,
            mines = mines,
            moves = 0,
            cells = buildCells(board, analyzer.analyze(board))
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

    fun dispatch(action: Action?, gid: Int) {
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

        // TODO:  maybe separate out probs and rules so maybe they can be ran concurrently..?

        val overlay = if (!board.gameOver and _uiState.value.isEnumerate) analyzer.analyze(board) else AnalyzerOverlay()
        val conflictDelta: ConflictDelta = if (!board.gameOver and _uiState.value.isEnumerate) board.conflictsByGid(cs.getAllGid()) else ConflictDelta()
        val conflictBoard = _uiState.value.conflictBoard.applyDelta(conflictDelta)

        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "overlay.conflictProbs = ${overlay.conflictProbs}")
        Log.d(TAG, "conflictBoard = ${conflictBoard}")

        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = patchCells(
                    base = s.cells,
                    cs = cs,
                    board = board,
                    overlay = overlay,
                    conflictsBoard = conflictBoard,
                    clearConflicts = conflictDelta.removes
                ),
                overlay = overlay,
                ruleList = overlay.ruleList,
                conflictBoard = conflictBoard,
                conflictProbs = overlay.conflictProbs
            )
        }
    }


    private fun patchCells(
        base: List<CellUI>,
        cs: ChangeSet,
        board: Board,
        overlay: AnalyzerOverlay,
        conflictsBoard: ConflictList = ConflictList(),
        clearConflicts: Set<Int> = emptySet()
    ): List<CellUI> {
        val updated = base.toMutableList()

        val changed = HashSet<Int>().apply {
            addAll(cs.revealed)
            addAll(cs.flagged)
            addAll(cs.exploded)

            addAll(conflictsBoard.keys)

            addAll(overlay.conflictProbs.keys)
            addAll(overlay.forcedOpens)
            addAll(overlay.forcedFlags)
            addAll(overlay.probabilities.keys)

            addAll(clearConflicts)
        }

        for (gid in changed) {
            val bc = board.cells[gid]

            updated[gid] = CellUI(
                gid = gid,

                isMine = bc.isMine,
                isRevealed = bc.isRevealed,
                isFlagged = bc.isFlagged,
                isExploded = bc.isExploded,
                adjacentMines = bc.adjacentMines,

                conflict = (gid in overlay.conflictProbs.keys) or (gid in conflictsBoard.keys),

                probability = overlay.probabilities.get(gid),

                forcedOpen = gid in (overlay.forcedOpens),
                forcedFlag = gid in (overlay.forcedFlags)
            )
        }

        return updated
    }

    // autoBuildCells(totalMove
    // s, board, overlay, csOpen, _uiState.value.conflictDelta, _uiState.value.conflictBoard)
    // TODO: make it so autobot doesn't make a wrong move ever
    private fun autoBuildCells(
        totalMoves: Int,
        board: Board,
        overlay: AnalyzerOverlay,
        cs: ChangeSet,
        conflictDelta: ConflictDelta,
        conflictBoard: ConflictList
    ) {
        _uiState.update { s ->
            s.copy(
                moves = totalMoves,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = patchCells(
                    base = s.cells,
                    cs = cs,
                    board = board,
                    overlay = overlay,
                    conflictsBoard = conflictBoard,
                    clearConflicts = conflictDelta.removes
                ),
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
                // this actually does check the board for mines because I don't want autobot to blow
                // up anyones game.  i can stop the autobot before/when it hits a mine and just stop there
                // and say it cannot continue but skipping the area (conflict area) seems okay because
                // even if there weren't conflicts beforehand it seems that now there are always a lot after
                // if flags are incorrectly placed
                // the autobot currently stops when no more RULES can be applied, it does not use the
                // probability data, even if it is a sure thing.  this is a change since the python code
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

            _uiState.update { s ->
                s.copy(
                    moves = beforeMoves + totalMoves,
                    gameOver = totalChanges.gameOver,
                    win = totalChanges.win,
                    cells = patchCells(
                        base = s.cells,
                        cs = totalChanges,
                        board = board,
                        overlay = overlay,
                        conflictsBoard = conflictBoard,
                        clearConflicts = conflictDelta.removes
                    ),
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
        val e = _uiState.value.isEnumerate
        if (!e) {
            val overlay = analyzer.analyze(board)

            val n = (0..((board.rows * board.cols)-1)).toSet()
            val conflictBoard: ConflictDelta = board.conflictsByGid(n)

            _uiState.update { s ->
                s.copy(
                    cells = patchCells(
                        base = s.cells,
                        cs = ChangeSet(),
                        board = board,
                        overlay = overlay
                    ),
                    isEnumerate = true,

                    overlay = overlay,
                    ruleList = overlay.ruleList,
                    conflictBoard = conflictBoard.upserts,
                    conflictProbs = overlay.conflictProbs
                )
            }
        }
        else {
            _uiState.update { s -> s.copy( isEnumerate = false)}
        }
    }

    fun undo() {
        val entry = history.pop() ?: return

        board.undo(entry) // revert truth only

        var overlay = AnalyzerOverlay()
        var conflictDelta = ConflictDelta()

        if (_uiState.value.isEnumerate) {
            overlay = analyzer.analyze(board)
            val n = (0..<(board.rows * board.cols)).toSet()
            conflictDelta = board.conflictsByGid(n)
        }


        _uiState.update { s ->
            s.copy(
                moves = entry.moveCountBefore,
                gameOver = false,
                win = false,
                cells = buildCells(
                    b = board,
                    overlay = overlay,
                    conflictBoard = conflictDelta.upserts
                ),
                overlay = overlay,
                ruleList = overlay.ruleList,
                conflictBoard = conflictDelta.upserts,
                conflictProbs = overlay.conflictProbs
            )
        }
    }

    private fun buildCells(
        b: Board,
        overlay: AnalyzerOverlay = AnalyzerOverlay(),
        conflictBoard: ConflictList = ConflictList(),
        clearConflicts: Set<Int> = emptySet()
    ): List<CellUI> {
        val out = ArrayList<CellUI>(b.rows * b.cols)
        for (gid in 0 until (b.rows * b.cols)) {
            val bc = b.cells[gid]
            out.add(
                CellUI(
                    gid = gid,

                    isMine = bc.isMine,
                    isRevealed = bc.isRevealed,
                    isFlagged = bc.isFlagged,
                    isExploded = bc.isExploded,
                    adjacentMines = bc.adjacentMines,

                    probability = overlay.probabilities.get(gid),

                    forcedOpen = gid in overlay.forcedOpens,
                    forcedFlag = gid in overlay.forcedFlags,

                    conflict = (gid in overlay.conflictProbs.keys) or (gid in conflictBoard.keys),
                )
            )
        }
        return out
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////




}
