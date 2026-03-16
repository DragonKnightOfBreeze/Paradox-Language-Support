package icu.windea.pls.ep.index

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.attributes.CwtDeclarationConfigAttributes
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtDefinitionTypesModel
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 基于定义的方案。
 * - 检查文件级别的定义规则候选项是否存在、对应的定义规则的名字、以及对应的声明规则的综合属性。
 */
class ParadoxDefinitionBasedMergedIndexSupport : ParadoxMergedIndexOptimizer {
    override fun isAvailableForFile(file: ParadoxScriptFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val gameType = fileInfo.rootInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val path = fileInfo.path
        val fileLevelMatchContext = CwtTypeConfigMatchContext(configGroup, path)
        val fileLevelTypeConfigs = ParadoxConfigMatchService.getTypeConfigCandidates(fileLevelMatchContext)

        // 如果文件级别的定义规则候选项为空，则认为是不可用的（构建索引数据时，不需要继续递归向下检查定义成员以及其他 PSI 元素）
        if (fileLevelTypeConfigs.isEmpty()) return false

        val definitionTypesModel = configGroup.definitionTypesModel
        val declarations = configGroup.declarations
        for (typeConfig in fileLevelTypeConfigs) {
            // 如果涉及特定类型的定义，则认为是可用的
            if (isForcedTypeConfig(typeConfig, definitionTypesModel)) return true

            // 检查对应的声明规则的综合属性，如果发现可能包含要索引的数据，则认为是可用的
            val declarationConfig = declarations[typeConfig.name] ?: continue
            if (isInvolvedDeclarationConfig(declarationConfig)) return true
        }
        return false
    }

    private fun isForcedTypeConfig(typeConfig: CwtTypeConfig, definitionTypesModel: CwtDefinitionTypesModel): Boolean {
        val name = typeConfig.name
        return when (name) {
            // see: icu.windea.pls.ep.index.ParadoxEventInOnActionMergedIndexSupport
            ParadoxDefinitionTypes.onAction -> true
            // see: icu.windea.pls.ep.index.ParadoxEventInEventMergedIndexSupport
            // see: icu.windea.pls.ep.index.ParadoxOnActionInEventMergedIndexSupport
            ParadoxDefinitionTypes.event -> true
            // see: icu.windea.pls.ep.index.ParadoxInferredScopeContextAwareDefinitionMergedIndexSupport
            in definitionTypesModel.supportScopeContextInference -> true
            else -> false
        }
    }

    private fun isInvolvedDeclarationConfig(declarationConfig: CwtDeclarationConfig): Boolean {
        val attributes = declarationConfig.attributes
        if (attributes === CwtDeclarationConfigAttributes.EMPTY) return false
        return when {
            // see: icu.windea.pls.ep.index.ParadoxDynamicValueMergedIndexSupport
            attributes.dynamicValueInvolved -> true
            // see: icu.windea.pls.ep.index.ParadoxParameterMergedIndexSupport
            attributes.parameterInvolved -> true
            // see: icu.windea.pls.ep.index.ParadoxLocalisationParameterMergedIndexSupport
            attributes.localisationParameterInvolved -> true
            else -> false
        }
    }
}

/**
 * 回退方案。
 * - 脚本文件默认不可用，本地化文件默认可用。
 */
class ParadoxFallbackMergedIndexOptimizer : ParadoxMergedIndexOptimizer {
    override fun isAvailableForFile(file: ParadoxScriptFile): Boolean {
        return false // fallback to false (skip)
    }

    override fun isAvailableForFile(file: ParadoxLocalisationFile): Boolean {
        return true // fallback to true
    }
}
