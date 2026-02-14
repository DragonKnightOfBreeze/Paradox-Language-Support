package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.getModeFromExpression
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.getTargetFromExpression
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.isAvailable
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.isMatched
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

object ParadoxDefinitionInjectionService {
    fun resolveInfo(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefinitionInjectionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val expression = element.name
        if (!isMatched(expression, gameType)) return null
        if (!isAvailable(element)) return null
        if (expression.isParameterized()) return null // 忽略带参数的情况
        val mode = getModeFromExpression(expression)
        if (mode.isNullOrEmpty()) return null
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 project
        val config = configGroup.directivesModel.definitionInjection ?: return null
        val modeConfig = config.modeConfigs[mode] ?: return null
        val target = getTargetFromExpression(expression)
        run {
            if (target.isNullOrEmpty()) return@run
            val path = fileInfo.path
            val matchContext = CwtTypeConfigMatchContext(configGroup, path)
            val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfigForInjection(matchContext) ?: return@run
            val type = typeConfig.name
            return ParadoxDefinitionInjectionInfo(mode, target, type, modeConfig, typeConfig).also { it.element = element }
        }
        // 兼容目标为空或者目标类型无法解析的情况
        return ParadoxDefinitionInjectionInfo(mode, target, null, modeConfig, null).also { it.element = element }
    }

    fun resolveSubtypeConfigs(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        // 从目标定义获取子类型信息
        val element = definitionInjectionInfo.element ?: return emptyList()
        val target = definitionInjectionInfo.target?.orNull() ?: return emptyList()
        val type = definitionInjectionInfo.type?.orNull() ?: return emptyList()
        val typeConfig = definitionInjectionInfo.typeConfig ?: return emptyList()
        if (typeConfig.subtypes.isEmpty()) return emptyList()
        val selector = selector(definitionInjectionInfo.project, element).definition().contextSensitive()
        val targetDefinition = ParadoxDefinitionSearch.searchProperty(target, type, selector).find() ?: return emptyList()
        val targetInfo = targetDefinition.definitionInfo ?: return emptyList()
        return targetInfo.getSubtypeConfigs(options)
    }

    fun resolveDeclaration(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        val element = definitionInjectionInfo.element ?: return null
        val name = definitionInjectionInfo.target?.orNull() ?: return null
        val type = definitionInjectionInfo.type?.orNull() ?: return null
        val configGroup = definitionInjectionInfo.configGroup
        val declarationConfig = definitionInjectionInfo.declarationConfig ?: return null
        val subtypeConfigs = ParadoxDefinitionInjectionManager.getSubtypeConfigs(definitionInjectionInfo, options)
        val subtypes = ParadoxConfigManager.getSubtypes(subtypeConfigs)
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, name, type, subtypes, configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    @Suppress("UNUSED_PARAMETER")
    fun getDependencies(element: ParadoxDefinitionElement, file: PsiFile): List<Any> {
        return listOf(file)
    }

    fun getSubtypeAwareDependencies(element: ParadoxDefinitionElement, definitionInjectionInfo: ParadoxDefinitionInjectionInfo): List<Any> {
        // 如果没有子类型候选项，则没有额外的 tracker
        if (definitionInjectionInfo.subtypeConfigs.isEmpty()) return listOf(element.containingFile)

        // TODO 2.1.3 考虑到匹配子类型时可能需要检查某个属性值是否是特定的定义类型，目前暂时依赖所有脚本文件。
        return listOf(element.containingFile, ParadoxModificationTrackers.ScriptFile)
    }
}
