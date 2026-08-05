package icu.windea.pls.lang.index

import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxEventInEventIndexInfo
import icu.windea.pls.model.index.ParadoxEventInOnActionIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxMeshLocatorIndexInfo
import icu.windea.pls.model.index.ParadoxOnActionInEventIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.model.index.ParadoxScopeInferrableDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxShaderEffectIndexInfo

/**
 * 所有预定义的合并索引类型。
 *
 * @see ParadoxMergedIndexType
 */
object ParadoxMergedIndexTypes {
    val DynamicValue = ParadoxMergedIndexType.builder<ParadoxDynamicValueIndexInfo>("DynamicValue", "dynamicValue").build()
    val Parameter = ParadoxMergedIndexType.builder<ParadoxParameterIndexInfo>("Parameter", "parameter").build()
    val LocalisationParameter = ParadoxMergedIndexType.builder<ParadoxLocalisationParameterIndexInfo>("LocalisationParameter", "localisationParameter").build()

    // NOTE: must use same key with `Parameter`
    val ParameterWithReadAccess = ParadoxMergedIndexType.builder<ParadoxParameterIndexInfo>("ParameterWithReadAccess", "parameter").build()

    val ShaderEffect = ParadoxMergedIndexType.builder<ParadoxShaderEffectIndexInfo>("ShaderEffect", "shaderEffect").build()
    val MeshLocator = ParadoxMergedIndexType.builder<ParadoxMeshLocatorIndexInfo>("MeshLocator", "meshLocator").build()

    val ScopeInferrableDefinition = ParadoxMergedIndexType.builder<ParadoxScopeInferrableDefinitionIndexInfo>("ScopeInferrableDefinition", "scopeInferrableDefinition").build()
    val EventInOnAction = ParadoxMergedIndexType.builder<ParadoxEventInOnActionIndexInfo>("EventInOnAction", "event.in.onAction").build()
    val EventInEvent = ParadoxMergedIndexType.builder<ParadoxEventInEventIndexInfo>("EventInEvent", "event.in.event").build()
    val OnActionInEvent = ParadoxMergedIndexType.builder<ParadoxOnActionInEventIndexInfo>("OnActionInEvent", "onAction.in.event").build()
}
