package icu.windea.pls.lang.match

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.withOperator
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.complexEnumValue
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScopeType
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.stringValue

object ParadoxMatchProvider {
    fun matchesDefinition(element: PsiElement, project: Project, name: String, typeExpression: String): Boolean {
        val selector = selector(project, element).definition()
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
    }

    fun matchesLocalisation(element: PsiElement, project: Project, name: String): Boolean {
        val selector = selector(project, element).localisation()
        return ParadoxLocalisationSearch.search(name, selector).findFirst() != null
    }

    fun matchesSyncedLocalisation(element: PsiElement, project: Project, name: String): Boolean {
        val selector = selector(project, element).localisation()
        return ParadoxSyncedLocalisationSearch.search(name, selector).findFirst() != null
    }

    fun matchesPathReference(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): Boolean {
        val selector = selector(project, element).file()
        return ParadoxFilePathSearch.search(expression, configExpression, selector).findFirst() != null
    }

    fun matchesComplexEnumValue(element: PsiElement, project: Project, name: String, enumName: String, searchScopeType: String? = null): Boolean {
        val selector = selector(project, element).complexEnumValue().withSearchScopeType(searchScopeType)
        return ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
    }

    fun matchesModifier(element: PsiElement, configGroup: CwtConfigGroup, name: String): Boolean {
        return ParadoxModifierManager.matchesModifier(name, element, configGroup)
    }

    fun matchesTemplate(element: PsiElement, configGroup: CwtConfigGroup, expression: String, templateExpression: String): Boolean {
        return CwtTemplateExpressionManager.matches(element, expression, CwtTemplateExpression.resolve(templateExpression), configGroup)
    }

    /**
     * 根据附加到 [config] 上的 `## predicate` 选项中的元数据，以及 [element] 所在的块（[icu.windea.pls.script.psi.ParadoxScriptBlockElement]）中的结构，进行简单的结构匹配。
     */
    fun matchesByPredicate(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        run {
            val predicate = config.optionData { predicate }
            if (predicate.isEmpty()) return@run
            val parentBlock = element.parentOfType<ParadoxScriptBlockElement>(withSelf = false) ?: return@run
            predicate.forEach f@{ (pk, pv) ->
                val p1 = parentBlock.findProperty(pk, inline = true)
                val pv1 = p1?.propertyValue?.stringValue()
                val pr = pv.withOperator { it == pv1 }
                if (!pr) return false
            }
        }
        return true
    }
}
