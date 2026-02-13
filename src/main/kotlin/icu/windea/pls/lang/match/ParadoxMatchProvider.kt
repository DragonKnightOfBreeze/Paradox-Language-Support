package icu.windea.pls.lang.match

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScopeType
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import java.util.concurrent.atomic.AtomicLong

object ParadoxMatchProvider {
    private val keyCounter = AtomicLong()
    private val intCounter = AtomicLong()
    private val floatCounter = AtomicLong()
    private val stringCounter = AtomicLong()

    fun matchesBlock(element: ParadoxScriptBlock, config: CwtMemberConfig<*>): Boolean {
        val keys = CwtConfigManipulator.getInBlockKeys(config)
        if (keys.isEmpty()) return true

        // 根据其中存在的属性键进行过滤（注意这里需要考虑内联和可选的情况）
        // 如果子句中包含对应的任意子句规则中的任意必须的属性键（忽略大小写），则认为匹配
        return element.members(conditional = true, inline = true).any {
            if (it is ParadoxScriptProperty) it.name in keys else false
        }
    }

    fun matchesDefinition(element: PsiElement, project: Project, name: String, typeExpression: String): Boolean {
        when (element) {
            is ParadoxScriptPropertyKey -> keyCounter.incrementAndGet()
            is ParadoxScriptInt -> intCounter.incrementAndGet()
            is ParadoxScriptFloat -> floatCounter.incrementAndGet()
            is ParadoxScriptString -> stringCounter.incrementAndGet()
        }

        val selector = selector(project, element).definition()
        return ParadoxDefinitionSearch.searchElement(name, typeExpression, selector).findFirst() != null
    }

    fun matchesLocalisation(element: PsiElement, project: Project, name: String): Boolean {
        val selector = selector(project, element).localisation()
        return ParadoxLocalisationSearch.searchNormal(name, selector).findFirst() != null
    }

    fun matchesSyncedLocalisation(element: PsiElement, project: Project, name: String): Boolean {
        val selector = selector(project, element).localisation()
        return ParadoxLocalisationSearch.searchSynced(name, selector).findFirst() != null
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
        return ParadoxConfigExpressionMatchService.matchesTemplate(element, configGroup, expression, CwtTemplateExpression.resolve(templateExpression))
    }

    /**
     * 根据附加到 [config] 上的选项数据（[CwtOptionDataHolder.predicate]），
     * 以及 [element] 所在的块（[ParadoxScriptBlockElement]）中的结构，
     * 进行简单的结构匹配。
     *
     * @param element 上下文 PSI 元素。
     * @param config 用于获取选项数据的规则，也可以是属性值对应的规则。
     */
    fun matchesByPredicate(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        run {
            val predicate = config.optionData.predicate
            if (predicate.isNullOrEmpty()) return@run
            val parentBlock = element.parentOfType<ParadoxScriptBlockElement>(withSelf = false) ?: return@run
            predicate.forEach f@{ (pk, pv) ->
                val p1 = selectScope { parentBlock.properties(inline = true).ofKey(pk).one() }
                val pv1 = p1?.propertyValue?.stringValue()
                val pr = pv.withOperator { it == pv1 }
                if (!pr) return false
            }
        }
        return true
    }
}
