package icu.windea.pls.lang.expressionIndex

import icu.windea.pls.model.expression.*

sealed class ParadoxExpressionIndexId<T: ParadoxExpressionInfo>(val id: Byte) {
    object ComplexEnumValue: ParadoxExpressionIndexId<ParadoxComplexEnumValueInfo>(1)
    object ValueSetValue: ParadoxExpressionIndexId<ParadoxValueSetValueInfo>(2)
    object InlineScriptUsage: ParadoxExpressionIndexId<ParadoxInlineScriptUsageInfo>(3)
    object Parameter: ParadoxExpressionIndexId<ParadoxParameterInfo>(4)
    object LocalisationParameter: ParadoxExpressionIndexId<ParadoxLocalisationParameterInfo>(5)
    
    object InferredScopeContextAwareDefinition: ParadoxExpressionIndexId<ParadoxInferredScopeContextAwareDefinitionInfo>(11)
    object EventInOnAction: ParadoxExpressionIndexId<ParadoxEventInOnActionInfo>(12)
    object EventInEvent: ParadoxExpressionIndexId<ParadoxEventInEventInfo>(13)
    object OnActionInEvent: ParadoxExpressionIndexId<ParadoxOnActionInEventInfo>(14)
}