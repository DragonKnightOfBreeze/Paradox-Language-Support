package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtComplexEnumConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxComplexEnumValueService {
    fun resolveInfo(element: ParadoxScriptStringExpressionElement, file: PsiFile): ParadoxComplexEnumValueInfo? {
        val name = element.value
        if (name.isParameterized()) return null // 排除可能带参数的情况
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType
        if (ParadoxInlineScriptManager.isMatched(name, gameType)) return null // 排除是内联脚本用法的情况
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val matchContext = CwtComplexEnumConfigMatchContext(configGroup, path)
        val config = ParadoxConfigMatchService.getMatchedComplexEnumConfig(matchContext, element) ?: return null
        val enumName = config.name
        return ParadoxComplexEnumValueInfo(name, enumName, config)
    }

    fun resolveNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).find()
    }

    fun resolveNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): Set<ParadoxLocalisationProperty> {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
    }
}
