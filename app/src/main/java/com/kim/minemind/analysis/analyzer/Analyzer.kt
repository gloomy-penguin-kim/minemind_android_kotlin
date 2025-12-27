package com.kim.minemind.analysis

import com.kim.minemind.analysis.enumeration.ProbabilityEngine
import com.kim.minemind.analysis.frontier.Frontier
import com.kim.minemind.analysis.analyzer.AnalyzerOverlay
import com.kim.minemind.analysis.rules.RuleEngine
import com.kim.minemind.core.board.Board

class Analyzer() {
    private val config: AnalysisConfig = AnalysisConfig
    private val frontier: Frontier = Frontier()
    private val ruleEngine: RuleEngine = RuleEngine()
    private val probabilityEngine: ProbabilityEngine = ProbabilityEngine()

    fun analyze(board: Board): AnalyzerOverlay {
        val comps = frontier.build(board)
        val rules = ruleEngine.evaluate(board, comps)

        val shouldRunProbs = shouldRunProbabilities(comps, rules)
        val probs = if (shouldRunProbs) probabilityEngine.computeProbabilities(board, comps) else emptyMap()

        return AnalyzerOverlay(
            probabilities = probs,
            ruleActions = rules.ruleActionByGid,
            conflicts = rules.conflicts,
            forcedFlags = rules.forcedFlags,
            forcedOpens = rules.forcedOpens
        )
    }

    fun clear()  {
        probabilityEngine.clear()
    }

    private fun shouldRunProbabilities(
        comps: List<com.kim.minemind.analysis.frontier.Component>,
        rules: com.kim.minemind.analysis.rules.RuleResult
    ): Boolean {
        if (!config.enableProbabilities) return false

        // If rules already found forced moves, probs aren’t needed *yet*
        if (rules.forcedFlags.isNotEmpty() || rules.forcedOpens.isNotEmpty()) return config.runProbabilitiesEvenWhenForced

        val totalK = comps.sumOf { it.k }
        if (totalK == 0) return false

        val maxK = comps.maxOfOrNull { it.k } ?: 0
        if (maxK > config.maxKPerComponent) return false

        if (comps.size > config.maxComponents) return false

        // optional: cap total unknowns too
        if (totalK > config.maxTotalK) return false

        return true
    }
}

