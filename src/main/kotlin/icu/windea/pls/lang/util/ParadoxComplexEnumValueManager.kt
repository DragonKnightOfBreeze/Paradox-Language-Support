package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtComplexEnumConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxComplexEnumValueManager {
    object Keys : KeyRegistry() {
        val cachedComplexEnumValueInfo by registerKey<CachedValue<ParadoxComplexEnumValueInfo>>(Keys)
    }

    fun getInfo(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueInfo? {
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueInfo? {
        // invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedComplexEnumValueInfo) {
            val file = element.containingFile
            val value = doGetInfo(element, file)
            value.withDependencyItems(file)
        }
    }

    private fun doGetInfo(element: ParadoxScriptStringExpressionElement, file: PsiFile): ParadoxComplexEnumValueInfo? {
        val value = element.value
        if (value.isParameterized()) return null // 排除可能带参数的情况
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType
        if (ParadoxInlineScriptManager.isMatched(value, gameType)) return null // 排除是内联脚本用法的情况
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val matchContext = CwtComplexEnumConfigMatchContext(configGroup, path)
        val config = ParadoxConfigMatchService.getMatchedComplexEnumConfig(matchContext, element) ?: return null
        val name = getName(value) ?: return null
        val enumName = config.name
        return ParadoxComplexEnumValueInfo(name, enumName, config)
    }

    fun getName(expression: String): String? {
        return expression.orNull()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).find()
    }

    @Suppress("unused")
    fun getNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): Set<ParadoxLocalisationProperty> {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
    }
}
