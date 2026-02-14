package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.getModeFromExpression
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.getTargetFromExpression
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.isAvailable
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.isMatched
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
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
            return ParadoxDefinitionInjectionInfo(mode, target, type, modeConfig, typeConfig)
        }
        // 兼容目标为空或者目标类型无法解析的情况
        return ParadoxDefinitionInjectionInfo(mode, target, null, modeConfig, null)
    }

    fun resolveSubtypeConfigs(element: ParadoxScriptProperty, definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        // 从目标定义获取子类型信息
        val target = definitionInjectionInfo.target?.orNull() ?: return emptyList()
        val type = definitionInjectionInfo.type?.orNull() ?: return emptyList()
        val typeConfig = definitionInjectionInfo.typeConfig ?: return emptyList()
        if (typeConfig.subtypes.isEmpty()) return emptyList()
        val selector = selector(definitionInjectionInfo.project, element).definition()
        val targetDefinition = ParadoxDefinitionSearch.searchProperty(target, type, selector).findFirst() ?: return emptyList()
        val targetInfo = targetDefinition.definitionInfo ?: return emptyList()
        return targetInfo.getSubtypeConfigs(options)
    }

    fun resolveDeclaration(element: PsiElement, definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        val declarationConfig = definitionInjectionInfo.declarationConfig ?: return null
        val definitionName = definitionInjectionInfo.target?.orNull() ?: return null
        val definitionType = definitionInjectionInfo.type?.orNull() ?: return null
        val definitionSubtypes = definitionInjectionInfo.getSubtypeConfigs(options).map { it.name }
        val configGroup = definitionInjectionInfo.configGroup
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, definitionName, definitionType, definitionSubtypes, configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }
}
