package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchContext

@Optimized
abstract class ParadoxScriptCompositeExpressionMatcher : ParadoxScriptExpressionMatcher {
    private val _matchers = mutableListOf<ParadoxScriptLightExpressionMatcher>()
    private val _matcherMap = mutableMapOf<ParadoxScriptLightExpressionMatcher, MutableSet<CwtDataType>>()

    // NOTE 3.0.1 nested composite matchers are not supported atm

    @Suppress("unused")
    val matchers: List<ParadoxScriptLightExpressionMatcher> get() = _matchers
    val matcherMap: Map<ParadoxScriptLightExpressionMatcher, Set<CwtDataType>> get() = _matcherMap

    init {
        registerMatchers()
    }

    protected abstract fun registerMatchers()

    protected fun register(matcher: ParadoxScriptLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap[matcher] = CwtDataType.entries.values.toMutableSet()
    }

    protected fun register(dataType: CwtDataType, matcher: ParadoxScriptLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap.computeIfAbsent(matcher) { mutableSetOf() } += dataType
    }

    protected fun register(vararg dataTypes: CwtDataType, matcher: ParadoxScriptLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap.computeIfAbsent(matcher) { mutableSetOf() } += dataTypes
    }

    @JvmName("registerAll")
    protected fun register(dataTypes: Array<CwtDataType>, matcher: ParadoxScriptLightExpressionMatcher) {
        _matchers += matcher
        _matcherMap.computeIfAbsent(matcher) { mutableSetOf() } += dataTypes
    }

    final override fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult? {
        _matchers.forEachFast f@{ matcher ->
            if (matcher is ParadoxScriptCompositeExpressionMatcher) return@f // skip
            matcher.match(context)?.let { return it }
        }
        return null
    }
}

abstract class ParadoxScriptSimpleExpressionMatcher : ParadoxScriptExpressionMatcher {
    abstract val dataTypes: Array<CwtDataType>
}

fun interface ParadoxScriptLightExpressionMatcher : ParadoxScriptExpressionMatcher
