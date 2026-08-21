package icu.windea.pls.ep.index

import com.google.common.collect.ImmutableSet
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.attributes.CwtDeclarationConfigAttributes
import icu.windea.pls.config.attributes.CwtRowConfigAttributes
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.ParadoxMergedIndexType
import icu.windea.pls.lang.index.ParadoxMergedIndexTypes
import icu.windea.pls.lang.match.CwtRowConfigMatchContext
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 文件级别的默认优化方案。
 * - 对于脚本文件，没有默认可用的合并索引类型。
 * - 对于本地化文件，默认可用的合并索引类型为 [ParadoxMergedIndexTypes.DynamicValue]。
 * - 对于 CSV 文件，没有默认可用的合并索引类型。
 */
class ParadoxDefaultFileLevelMergedIndexOptimizer : ParadoxMergedIndexOptimizer {
    override fun getAvailableTypes(file: ParadoxLocalisationFile): Collection<ParadoxMergedIndexType<*>> {
        return ImmutableSet.of(ParadoxMergedIndexTypes.DynamicValue)
    }
}

/**
 * 基于定义的优化方案。
 * - 检查文件级别的类型规则候选项是否存在，对应的类型规则的名字，以及对应的声明规则的综合属性。
 * - 检查定义级别的类型规则的名字，以及对应的声明规则的综合属性。
 */
class ParadoxDefinitionBasedMergedIndexOptimizer : ParadoxMergedIndexOptimizer {
    override fun getAvailableTypes(file: ParadoxScriptFile): Collection<ParadoxMergedIndexType<*>> {
        val fileInfo = file.fileInfo ?: return emptySet()
        val gameType = fileInfo.gameType
        val configGroup = ChronicleFacade.getConfigGroup(file.project, gameType)
        val path = fileInfo.path
        val fileLevelMatchContext = CwtTypeConfigMatchContext(configGroup, path)
        val fileLevelTypeConfigs = ParadoxConfigMatchService.getTypeConfigCandidates(fileLevelMatchContext)

        // 如果文件级别的定义规则候选项为空，则认为是不可用的（构建索引数据时，不需要继续递归向下检查定义成员以及其他 PSI 元素）
        if (fileLevelTypeConfigs.isEmpty()) return emptySet()

        // 要求存在声明规则
        val declarations = configGroup.declarations
        if (declarations.isEmpty()) return emptySet()

        val builder = ImmutableSet.builder<ParadoxMergedIndexType<*>>()
        for (typeConfig in fileLevelTypeConfigs) {
            // 要求存在声明规则
            val declarationConfig = declarations[typeConfig.name] ?: continue

            collectFromTypeConfig(typeConfig, declarationConfig, builder)
        }
        return builder.build()
    }

    override fun getAvailableTypes(definitionCandidateInfo: ParadoxDefinitionCandidateInfo): Collection<ParadoxMergedIndexType<*>> {
        val typeConfig = definitionCandidateInfo.typeConfig ?: return emptySet()
        val configGroup = definitionCandidateInfo.configGroup

        // 要求存在声明规则
        val declarations = configGroup.declarations
        val declarationConfig = declarations[typeConfig.name] ?: return emptySet()

        val builder = ImmutableSet.builder<ParadoxMergedIndexType<*>>()
        collectFromTypeConfig(typeConfig, declarationConfig, builder)
        return builder.build()
    }

    private fun collectFromTypeConfig(typeConfig: CwtTypeConfig, declarationConfig: CwtDeclarationConfig, builder: ImmutableSet.Builder<ParadoxMergedIndexType<*>>) {
        // 检查类型规则
        checkTypeConfig(typeConfig, builder)
        // 如果涉及特定类型的定义，则认为是可用的
        checkForcedTypeConfig(typeConfig, builder)
        // 检查对应的声明规则的综合属性，如果发现可能包含要索引的数据，则认为是可用的
        checkInvolvedDeclarationConfig(declarationConfig, builder)
    }

    private fun checkTypeConfig(typeConfig: CwtTypeConfig, builder: ImmutableSet.Builder<ParadoxMergedIndexType<*>>) {
        val typesModel = typeConfig.configGroup.typeModel
        // see: icu.windea.pls.ep.index.ParadoxParameterWithReadAccessMergedIndexSupport
        // TODO 3.0.1+ need further check
        if (typeConfig.name in typesModel.supportParameters) builder.add(ParadoxMergedIndexTypes.ParameterWithReadAccess)
    }

    private fun checkForcedTypeConfig(typeConfig: CwtTypeConfig, builder: ImmutableSet.Builder<ParadoxMergedIndexType<*>>) {
        val name = typeConfig.name
        when (name) {
            // see: icu.windea.pls.ep.index.ParadoxEventInOnActionMergedIndexSupport
            ParadoxDefinitionTypes.onAction -> builder.add(ParadoxMergedIndexTypes.EventInOnAction)
            // see: icu.windea.pls.ep.index.ParadoxEventInEventMergedIndexSupport
            // see: icu.windea.pls.ep.index.ParadoxOnActionInEventMergedIndexSupport
            ParadoxDefinitionTypes.event -> builder.add(ParadoxMergedIndexTypes.EventInEvent, ParadoxMergedIndexTypes.OnActionInEvent)
        }
    }

    private fun checkInvolvedDeclarationConfig(declarationConfig: CwtDeclarationConfig, builder: ImmutableSet.Builder<ParadoxMergedIndexType<*>>) {
        val attributes = declarationConfig.attributes
        if (attributes === CwtDeclarationConfigAttributes.EMPTY) return
        when {
            // see: icu.windea.pls.ep.index.ParadoxDynamicValueMergedIndexSupport
            attributes.involveDynamicValue -> builder.add(ParadoxMergedIndexTypes.DynamicValue)
            // see: icu.windea.pls.ep.index.ParadoxParameterMergedIndexSupport
            attributes.involveParameter -> builder.add(ParadoxMergedIndexTypes.Parameter)
            // see: icu.windea.pls.ep.index.ParadoxLocalisationParameterMergedIndexSupport
            attributes.involveLocalisationParameter -> builder.add(ParadoxMergedIndexTypes.LocalisationParameter)
            // see: icu.windea.pls.ep.index.ParadoxShaderEffectMergedIndexSupport
            // see: icu.windea.pls.ep.index.ParadoxMeshLocatorMergedIndexSupport
            attributes.involveExternalReference -> builder.add(ParadoxMergedIndexTypes.ShaderEffect, ParadoxMergedIndexTypes.MeshLocator)
            // see: icu.windea.pls.ep.index.ParadoxScopeInferrableDefinitionMergedIndexSupport
            attributes.involveScopeInferrableDefinitionReference -> builder.add(ParadoxMergedIndexTypes.ScopeInferrableDefinition)
        }
    }
}

/**
 * 基于列的优化方案。
 * - 检查文件级别的列规则候选项是否存在，以及对应的列规则的综合属性。
 */
class ParadoxRowBasedMergedIndexOptimizer : ParadoxMergedIndexOptimizer {
    override fun getAvailableTypes(file: ParadoxCsvFile): Collection<ParadoxMergedIndexType<*>> {
        val fileInfo = file.fileInfo ?: return emptySet()
        val gameType = fileInfo.gameType
        val configGroup = ChronicleFacade.getConfigGroup(file.project, gameType)
        val path = fileInfo.path
        val fileLevelMatchContext = CwtRowConfigMatchContext(configGroup, path)
        val fileLevelRowConfigs = ParadoxConfigMatchService.getRowConfigCandidates(fileLevelMatchContext)

        // 如果文件级别的行规则候选项为空，则认为是不可用的（直接跳过）
        if (fileLevelRowConfigs.isEmpty()) return emptySet()

        val builder = ImmutableSet.builder<ParadoxMergedIndexType<*>>()
        for (rowConfig in fileLevelRowConfigs) {
            // 检查行规则的综合属性，如果发现可能包含要索引的数据，则认为是可用的
            checkInvolvedRowConfig(rowConfig, builder)
        }
        return builder.build()
    }

    private fun checkInvolvedRowConfig(rowConfig: CwtRowConfig, builder: ImmutableSet.Builder<ParadoxMergedIndexType<*>>) {
        val attributes = rowConfig.attributes
        if (attributes === CwtRowConfigAttributes.EMPTY) return
        when {
            // see: icu.windea.pls.ep.index.ParadoxDynamicValueMergedIndexSupport
            attributes.involveDynamicValue -> builder.add(ParadoxMergedIndexTypes.DynamicValue)
        }
    }
}

