package icu.windea.pls.model.index

object ParadoxIndexInfoTypes {
    val DynamicValue = ParadoxIndexInfoType("DynamicValue", 2, ParadoxDynamicValueIndexInfo::class.java)
    val Parameter = ParadoxIndexInfoType("Parameter", 3, ParadoxParameterIndexInfo::class.java)
    val LocalisationParameter = ParadoxIndexInfoType("LocalisationParameter", 4, ParadoxLocalisationParameterIndexInfo::class.java)

    val InferredScopeContextAwareDefinition = ParadoxIndexInfoType("InferredScopeContextAwareDefinition", 5, ParadoxInferredScopeContextAwareDefinitionIndexInfo::class.java)
    val EventInOnAction = ParadoxIndexInfoType("EventInOnAction", 6, ParadoxEventInOnActionIndexInfo::class.java)
    val EventInEvent = ParadoxIndexInfoType("EventInEvent", 7, ParadoxEventInEventIndexInfo::class.java)
    val OnActionInEvent = ParadoxIndexInfoType("OnActionInEvent", 8, ParadoxOnActionInEventIndexInfo::class.java)
}
