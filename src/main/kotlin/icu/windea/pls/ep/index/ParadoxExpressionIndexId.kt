package icu.windea.pls.ep.index

import icu.windea.pls.model.expressionInfo.*

sealed class ParadoxExpressionIndexId<T : ParadoxExpressionInfo>(val code: Byte) {
    object ComplexEnumValue : ParadoxExpressionIndexId<ParadoxComplexEnumValueInfo>(1)
    object DynamicValue : ParadoxExpressionIndexId<ParadoxDynamicValueInfo>(2)
    object InlineScriptUsage : ParadoxExpressionIndexId<ParadoxInlineScriptUsageInfo>(3)
    object Parameter : ParadoxExpressionIndexId<ParadoxParameterInfo>(4)
    object LocalisationParameter : ParadoxExpressionIndexId<ParadoxLocalisationParameterInfo>(5)

    object InferredScopeContextAwareDefinition : ParadoxExpressionIndexId<ParadoxInferredScopeContextAwareDefinitionInfo>(11)
    object EventInOnAction : ParadoxExpressionIndexId<ParadoxEventInOnActionInfo>(12)
    object EventInEvent : ParadoxExpressionIndexId<ParadoxEventInEventInfo>(13)
    object OnActionInEvent : ParadoxExpressionIndexId<ParadoxOnActionInEventInfo>(14)
}
