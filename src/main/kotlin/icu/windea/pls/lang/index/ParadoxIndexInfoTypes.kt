package icu.windea.pls.lang.index

import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxEventInEventIndexInfo
import icu.windea.pls.model.index.ParadoxEventInOnActionIndexInfo
import icu.windea.pls.model.index.ParadoxInferredScopeContextAwareDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxMeshLocatorIndexInfo
import icu.windea.pls.model.index.ParadoxOnActionInEventIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.model.index.ParadoxShaderEffectIndexInfo

object ParadoxIndexInfoTypes {
    val DynamicValue = ParadoxMergedIndexType("DynamicValue", 2, ParadoxDynamicValueIndexInfo::class.java)
    val Parameter = ParadoxMergedIndexType("Parameter", 3, ParadoxParameterIndexInfo::class.java)
    val LocalisationParameter = ParadoxMergedIndexType("LocalisationParameter", 4, ParadoxLocalisationParameterIndexInfo::class.java)

    val InferredScopeContextAwareDefinition = ParadoxMergedIndexType("InferredScopeContextAwareDefinition", 5, ParadoxInferredScopeContextAwareDefinitionIndexInfo::class.java)
    val EventInOnAction = ParadoxMergedIndexType("EventInOnAction", 6, ParadoxEventInOnActionIndexInfo::class.java)
    val EventInEvent = ParadoxMergedIndexType("EventInEvent", 7, ParadoxEventInEventIndexInfo::class.java)
    val OnActionInEvent = ParadoxMergedIndexType("OnActionInEvent", 8, ParadoxOnActionInEventIndexInfo::class.java)

    val ShaderEffect = ParadoxMergedIndexType("ShaderEffect", 9, ParadoxShaderEffectIndexInfo::class.java)
    val MeshLocator = ParadoxMergedIndexType("MeshLocator", 10, ParadoxMeshLocatorIndexInfo::class.java)
}
