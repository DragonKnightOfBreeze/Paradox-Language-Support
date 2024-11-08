package icu.windea.pls.ep.index

import icu.windea.pls.model.expressionInfo.*

sealed class ParadoxExpressionIndexId<T : ParadoxExpressionInfo>(val code: Byte) {
    data object ComplexEnumValue : ParadoxExpressionIndexId<ParadoxComplexEnumValueInfo>(1)
    data object DynamicValue : ParadoxExpressionIndexId<ParadoxDynamicValueInfo>(2)
    data object InlineScriptUsage : ParadoxExpressionIndexId<ParadoxInlineScriptUsageInfo>(3)
    data object Parameter : ParadoxExpressionIndexId<ParadoxParameterInfo>(4)
    data object LocalisationParameter : ParadoxExpressionIndexId<ParadoxLocalisationParameterInfo>(5)

    data object InferredScopeContextAwareDefinition : ParadoxExpressionIndexId<ParadoxInferredScopeContextAwareDefinitionInfo>(11)
    data object EventInOnAction : ParadoxExpressionIndexId<ParadoxEventInOnActionInfo>(12)
    data object EventInEvent : ParadoxExpressionIndexId<ParadoxEventInEventInfo>(13)
    data object OnActionInEvent : ParadoxExpressionIndexId<ParadoxOnActionInEventInfo>(14)
}
