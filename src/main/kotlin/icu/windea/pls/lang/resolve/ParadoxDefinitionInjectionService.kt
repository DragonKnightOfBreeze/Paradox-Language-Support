package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.collections.orNull
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
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

object ParadoxDefinitionInjectionService {
    fun resolveInfo(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefinitionInjectionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val expression = element.name
        if (!ParadoxDefinitionInjectionManager.isMatched(expression, gameType)) return null
        if (!ParadoxDefinitionInjectionManager.isAvailable(element)) return null
        if (expression.isParameterized()) return null // 忽略带参数的情况
        val mode = getModeFromExpression(expression)
        if (mode.isNullOrEmpty()) return null
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val config = configGroup.directivesModel.definitionInjection ?: return null
        val modeConfig = config.modeConfigs[mode] ?: return null
        val target = getTargetFromExpression(expression)
        run {
            if (target.isNullOrEmpty()) return@run
            val path = fileInfo.path
            val matchContext = CwtTypeConfigMatchContext(configGroup, path)
            val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfigForInjection(matchContext) ?: return@run
            val type = typeConfig.name.orNull() ?: return@run
            return ParadoxDefinitionInjectionInfo(mode, target, type, modeConfig, typeConfig).also { it.element = element }
        }
        // 兼容目标为空或者目标类型无法解析的情况
        return ParadoxDefinitionInjectionInfo(mode, target, null, modeConfig, null).also { it.element = element }
    }

    fun resolveSubtypeConfigs(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        // 根据模式决定是从目标声明还是自身声明获取子类型
        if (definitionInjectionInfo.isReplaceMode()) {
            return resolveSubtypeConfigsFromSelf(definitionInjectionInfo, options)
        } else {
            return resolveSubtypeConfigsFromTarget(definitionInjectionInfo, options)
        }
    }

    private fun resolveSubtypeConfigsFromTarget(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
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

    private fun resolveSubtypeConfigsFromSelf(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        val element = definitionInjectionInfo.element ?: return emptyList()
        val typeConfig = definitionInjectionInfo.typeConfig ?: return emptyList()
        val subtypes = typeConfig.subtypes.orNull() ?: return emptyList()
        val typeKey = definitionInjectionInfo.target.orEmpty() // use target name as type key

        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypes.values) {
            val matched = ParadoxConfigMatchService.matchesSubtype(element, subtypeConfig, result, typeKey, options)
            if (matched) result += subtypeConfig
        }
        // processSubtypeConfigsFromInherit(definitionInfo, result) // NOTE 2.1.3 commented out since it's uncessary for injections
        return result
    }

    fun resolveDeclaration(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        val element = definitionInjectionInfo.element ?: return null
        val name = definitionInjectionInfo.target?.orNull() ?: return null
        val type = definitionInjectionInfo.type?.orNull() ?: return null
        val configGroup = definitionInjectionInfo.configGroup
        val declarationConfig = definitionInjectionInfo.declarationConfig ?: return null
        val subtypeConfigs = ParadoxDefinitionInjectionManager.getSubtypeConfigs(definitionInjectionInfo, options)
        val subtypes = ParadoxConfigManager.getSubtypes(subtypeConfigs)
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, configGroup, name, type, subtypes)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    @Suppress("UNUSED_PARAMETER")
    fun getDependencies(element: ParadoxDefinitionElement, file: PsiFile): List<Any> {
        // 由于不能有 rootKey 或 typeKeyPrefix，因此这里可以直接依赖 element，但为了与 definitionInfo 保持一致，仍然依赖 file
        return listOf(file)
    }

    fun getSubtypeAwareDependencies(element: ParadoxDefinitionElement, definitionInjectionInfo: ParadoxDefinitionInjectionInfo): List<Any> {
        val subtypes = definitionInjectionInfo.typeConfig?.subtypes
        val file = element.containingFile

        // 无子类型候选项
        if (subtypes.isNullOrEmpty()) return listOf(file)

        // 所有子类型候选项都不依赖声明结构（快速匹配）
        val allFastMatch = subtypes.values.all { it.config.configs.isNullOrEmpty() }
        if (allFastMatch) return listOf(file)

        // 需要依赖声明结构
        return listOf(file, ParadoxModificationTrackers.ScriptFile)
    }
}
