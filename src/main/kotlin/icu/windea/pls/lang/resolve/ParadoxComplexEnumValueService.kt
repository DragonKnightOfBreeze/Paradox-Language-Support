package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtComplexEnumConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

object ParadoxComplexEnumValueService {
    fun resolveInfo(element: ParadoxScriptExpressionElement, file: PsiFile): ParadoxComplexEnumValueInfo? {
        val name = element.value
        if (name.isParameterized()) return null // 排除可能带参数的情况
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType
        if (ParadoxInlineScriptManager.isMatched(name, gameType)) return null // 排除是内联脚本用法的情况
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val matchContext = CwtComplexEnumConfigMatchContext(configGroup, path)
        val config = ParadoxConfigMatchService.getMatchedComplexEnumConfig(matchContext, element) ?: return null
        val enumName = config.name
        return ParadoxComplexEnumValueInfo(name, enumName, config)
    }

    fun resolveInfo(element: ParadoxCsvExpressionElement): ParadoxComplexEnumValueInfo? {
        if (element !is ParadoxCsvColumn) return null
        val name = element.value
        if (name.isParameterized()) return null // 排除可能带参数的情况
        val columnConfig = ParadoxCsvManager.getColumnConfig(element) ?: return null
        val enumName = columnConfig.optionData.declareComplexEnum?.orNull() ?: return null
        val config = columnConfig.configGroup.complexEnumsFromColumns[enumName] ?: return null // 这里使用来自列规则的复杂枚举规则
        return ParadoxComplexEnumValueInfo(name, enumName, config)
    }

    fun resolveNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): ParadoxLocalisationProperty? {
        val selector = ParadoxLocalisationSearch.selector(contextElement.project, contextElement).contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).find()
    }

    fun resolveNameLocalisations(name: String, contextElement: PsiElement, locale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): List<ParadoxLocalisationProperty> {
        val selector = ParadoxLocalisationSearch.selector(contextElement.project, contextElement).contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
    }

    @Suppress("UNUSED_PARAMETER")
    fun getInfoDependencies(element: ParadoxScriptExpressionElement, file: PsiFile): List<Any> {
        return listOf(file)
    }

    @Suppress("UNUSED_PARAMETER")
    fun getInfoDependencies(element: ParadoxCsvExpressionElement): List<Any> {
        if (element is ParadoxCsvColumn) element.parent?.let { return listOf(it) } // depend on current row
        return listOf(element.containingFile)
    }
}
