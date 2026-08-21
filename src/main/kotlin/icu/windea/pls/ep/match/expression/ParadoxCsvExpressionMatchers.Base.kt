package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult

@Optimized
abstract class ParadoxCsvCompositeExpressionMatcher : ParadoxCsvExpressionMatcher {
    private val _matchers = mutableListOf<ParadoxCsvLightExpressionMatcher>()
    private val _matcherMap = mutableMapOf<ParadoxCsvLightExpressionMatcher, MutableSet<CwtDataType>>()

    // NOTE 3.0.1 nested composite matchers are not supported atm

    @Suppress("unused")
    val matchers: List<ParadoxCsvLightExpressionMatcher> get() = _matchers
    val matcherMap: Map<ParadoxCsvLightExpressionMatcher, Set<CwtDataType>> get() = _matcherMap

    init {
        registerMatchers()
    }

    protected abstract fun registerMatchers()

    protected fun register(matcher: ParadoxCsvLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap[matcher] = CwtDataType.entries.values.toMutableSet()
    }

    protected fun register(dataType: CwtDataType, matcher: ParadoxCsvLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap.computeIfAbsent(matcher) { mutableSetOf() } += dataType
    }

    protected fun register(vararg dataTypes: CwtDataType, matcher: ParadoxCsvLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap.computeIfAbsent(matcher) { mutableSetOf() } += dataTypes
    }

    @JvmName("registerAll")
    protected fun register(dataTypes: Array<CwtDataType>, matcher: ParadoxCsvLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap.computeIfAbsent(matcher) { mutableSetOf() } += dataTypes
    }

    final override fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult? {
        _matchers.forEachFast f@{ matcher ->
            if (matcher is ParadoxScriptCompositeExpressionMatcher) return@f // skip
            matcher.match(context)?.let { return it }
        }
        return null
    }
}

abstract class ParadoxCsvSimpleExpressionMatcher : ParadoxCsvExpressionMatcher {
    abstract val dataTypes: Array<CwtDataType>
}

fun interface ParadoxCsvLightExpressionMatcher : ParadoxCsvExpressionMatcher
