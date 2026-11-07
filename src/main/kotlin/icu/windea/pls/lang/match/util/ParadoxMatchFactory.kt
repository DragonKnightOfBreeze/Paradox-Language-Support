package icu.windea.pls.lang.match.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.option.CwtOptionMetadata
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.match.TextMatcher
import icu.windea.pls.core.select.one
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.resolve.ParadoxModifierService
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.withSearchScopeType
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty

object ParadoxMatchFactory {
    fun matchesBlock(element: ParadoxScriptBlock, config: CwtMemberConfig<*>): Boolean {
        val keys = CwtConfigManager.getWithinBlockKeys(config)
        if (keys.isNotEmpty()) {
            // 根据其中存在的属性键进行过滤（注意这里需要考虑内联和可选的情况）
            // 如果子句中包含对应的任意子句规则中的任意必须的属性键（忽略大小写），则认为匹配
            return element.members(conditional = true, inline = true).any { it is ParadoxScriptProperty && it.name in keys }
        }

        // 如果子句中不存在常量键（例如，子句中只包含别名引用、模板表达式等），则无法根据属性键进行判断，
        // 此时需要进一步根据子句中的成员能否匹配子句规则，来判断是否匹配。
        // 否则，同一个属性键对应的多个子句规则（例如，作用域链接在触发器上下文中作为触发器块和作为算术运算块的两种子句）总会同时匹配，
        // 这会导致冲突检查产生不期望的冲突（参见 #284）。
        val childConfigs = config.configs.orEmpty()
        if (childConfigs.isEmpty()) return true
        val configGroup = config.configGroup
        val members = element.members(conditional = true, inline = true)
        // 如果子句内容为空，则无法根据内容进一步判断，视为匹配（这也意味着空子句的内容是存在歧义的）
        if (members.none()) return true
        return members.any { member ->
            member is ParadoxScriptProperty && childConfigs.any { childConfig -> matchesBlockChild(member, childConfig, configGroup) }
        }
    }

    /**
     * 检查子句中的属性（[element]）能否匹配子句规则（[config]），用于在无法根据属性键判断时进一步过滤子句规则。
     *
     * 通配符匹配、部分匹配和回退匹配不足以用于判断（例如，别名规则的名称为一个作用域引用时，任意文本都可以通配符匹配），
     * 因此这里仅接受精确匹配（包括宽松的精确匹配）以及需要进一步检测的匹配。
     */
    private fun matchesBlockChild(element: ParadoxScriptProperty, config: CwtMemberConfig<*>, configGroup: CwtConfigGroup): Boolean {
        val options = ParadoxMatchOptions.create()
        val expression = ParadoxExpression.resolve(element.propertyKey, options)
        val context = ParadoxExpressionMatchContext(element, expression, configGroup, options)
        val result = ParadoxExpressionMatchService.matchScriptExpression(context, config.configExpression, config)
        return when (result) {
            is ParadoxMatchResult.WildcardMatch, is ParadoxMatchResult.LenientWildcardMatch,
            is ParadoxMatchResult.PartialMatch, is ParadoxMatchResult.FallbackMatch -> false
            else -> result.get(options)
        }
    }

    fun matchesIntPercentageField(expression: String): Boolean {
        return TextMatcher.matchesIntPercentageField(expression)
    }

    fun matchesFloatPercentageField(expression: String): Boolean {
        return TextMatcher.matchesFloatPercentageField(expression)
    }

    fun matchesDateField(expression: String, pattern: String? = null): Boolean {
        return TextMatcher.matchesDateField(expression, pattern)
    }

    fun matchesDefinition(element: PsiElement, project: Project, name: String, typeExpression: String): Boolean {
        val selector = ParadoxDefinitionSearch.selector(project, element)
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
    }

    fun matchesLocalisation(element: PsiElement, project: Project, name: String, type: ParadoxLocalisationType = ParadoxLocalisationType.Normal): Boolean {
        val selector = ParadoxLocalisationSearch.selector(project, element)
        return ParadoxLocalisationSearch.search(name, selector, type).findFirst() != null
    }

    fun matchesPathReference(element: PsiElement, project: Project, expression: String, configExpression: CwtDataExpression): Boolean {
        val selector = ParadoxFilePathSearch.selector(project, element)
        return ParadoxFilePathSearch.search(expression, configExpression, selector).findFirst() != null
    }

    fun matchesComplexEnumValue(element: PsiElement, project: Project, name: String, enumName: String, searchScopeType: String? = null): Boolean {
        val selector = ParadoxComplexEnumValueSearch.selector(project, element).withSearchScopeType(searchScopeType)
        return ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
    }

    fun matchesModifier(element: PsiElement, configGroup: CwtConfigGroup, name: String): Boolean {
        return ParadoxModifierService.matchesModifier(name, element, configGroup)
    }

    /**
     * 根据附加到 [config] 上的选项元数据 [predicate][CwtOptionMetadata.predicate]，以及 [element] 所在的块或文件中的结构，进行简单的结构匹配。
     *
     * @param element 上下文 PSI 元素。
     * @param config 用于获取选项元数据的规则，也可以是属性值对应的规则。
     */
    fun matchesByPredicate(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        run {
            val predicate = config.optionMetadata.predicate
            if (predicate.isNullOrEmpty()) return@run
            val containerElement = selectScope { element.queryParent().queryParent(withSelf = false) } ?: return@run
            predicate.forEach f@{ (pk, pv) ->
                val p1 = selectScope { containerElement.properties(inline = true).ofKey(pk).one() }
                val pv1 = p1?.propertyValue?.stringValue()
                val pr = pv.withOperator { it == pv1 }
                if (!pr) return false
            }
        }
        return true
    }
}
