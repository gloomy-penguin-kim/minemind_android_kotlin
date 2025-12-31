package com.kim.minemind.analysis

import com.kim.minemind.analysis.AnalyzerOverlay
import com.kim.minemind.analysis.enumeration.ProbabilityEngine
import com.kim.minemind.analysis.enumeration.ProbabilityResult
import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.analysis.frontier.Frontier
import com.kim.minemind.analysis.rules.RuleEngine
import com.kim.minemind.analysis.rules.RuleResult
import com.kim.minemind.core.Action
import com.kim.minemind.core.board.Board
import com.kim.minemind.shared.ConflictList
import com.kim.minemind.shared.Move

class Analyzer() {
    private val config: AnalysisConfig = AnalysisConfig
    private val frontier: Frontier = Frontier()
    private val ruleEngine: RuleEngine = RuleEngine()
    private val probabilityEngine: ProbabilityEngine = ProbabilityEngine()

    fun analyze(board: Board, stopAfterOne: Boolean = false): AnalyzerOverlay {
        val comps = frontier.build(board)
        var rules = ruleEngine.evaluate(board, comps, stopAfterOne)

        val shouldRunProbs = shouldRunProbabilities(comps, rules)
        val probs = if (shouldRunProbs)
            probabilityEngine.computeProbabilities(board, comps)
        else emptyMap()
        if (probs.isEmpty()) {
            rules = RuleResult(
                forcedFlags = setOf(),
                forcedOpens = setOf(),
                ruleActionByGid = mapOf(),
                conflictList = ConflictList(),
                ruleList = mapOf())
        }
        val conflictProbVsRule = compareRulesAndProbsForConflict(rules, probs)
        return AnalyzerOverlay(
            probabilities = probs,

            ruleActions = rules.ruleActionByGid,        // gid -> why/what (optional)
            forcedFlags = setOf(), //rules.forcedFlags,            // mines proven by rules
            forcedOpens = setOf(), //rules.forcedOpens,            // safe cells proven by rules

            conflictProbs = rules.conflictList.merge(conflictProbVsRule),
            ruleList = rules.ruleList,
            isConsistent = if (shouldRunProbs and probs.isEmpty()) false else true
        )
    }


    fun runRulesOnly(board: Board, stopAfterOne: Boolean = false): AnalyzerOverlay {
        val comps = frontier.build(board)
        var rules = ruleEngine.evaluate(board, comps, stopAfterOne)

        return AnalyzerOverlay(
            probabilities = emptyMap(),

            ruleActions = rules.ruleActionByGid,        // gid -> why/what (optional)
            forcedFlags = setOf(), //rules.forcedFlags,            // mines proven by rules
            forcedOpens = setOf(), //rules.forcedOpens,            // safe cells proven by rules

            conflictProbs = rules.conflictList,
            ruleList = rules.ruleList,
            isConsistent = rules.ruleActionByGid.isNotEmpty()
        )
    }

    fun clear()  {
        probabilityEngine.clear()
    }

    private fun shouldRunProbabilities(
        comps: List<Component>,
        rules: RuleResult
    ): Boolean {
//        // If rules already found forced moves, probs aren’t needed *yet*
//        if (rules.forcedFlags.isNotEmpty() || rules.forcedOpens.isNotEmpty())
//            return config.runProbabilitiesEvenWhenForced

        val totalK = comps.sumOf { it.k }
        if (totalK == 0) return false

        val maxK = comps.maxOfOrNull { it.k } ?: 0
        if (maxK > config.maxKPerComponent) return false

        if (comps.size > config.maxComponents) return false

        // optional: cap total unknowns too
        if (totalK > config.maxTotalK) return false

        return true
    }

    private fun compareRulesAndProbsForConflict(rules: RuleResult, probs: Map<Int, Float>): ConflictList {
        val conflictList = ConflictList()

        val ruleList = rules.ruleList
        for (rule in ruleList) {
            val gid = rule.key
            val action = rule.value.action

            if (probs.containsKey(gid)) {
                if (!probs.containsKey(gid)) {
                    conflictList.addConflict(gid, "Probability and rules do not agree.")
                }
                else {
                    if (action == Action.FLAG) {
                        probs[gid]?.let {
                            if (it < 0.5f) {
                                conflictList.addConflict(gid, "Probability and rules do not agree.")
                            }
                        }
                    }
                    if (action == Action.OPEN) {
                        probs[gid]?.let {
                            if (it > 0.5f) {
                                conflictList.addConflict(gid, "Probability and rules do not agree.")
                            }
                        }
                    }
                }
            }
        }
        return conflictList
    }
}