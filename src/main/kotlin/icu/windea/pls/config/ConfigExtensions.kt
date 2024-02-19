@file:Suppress("unused")

package icu.windea.pls.config

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.path.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*

inline fun CwtMemberConfig<*>.processParent(inline: Boolean = false, processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    var parent = this.parentConfig
    while(parent != null) {
        val result = processor(parent)
        if(!result) return false
        if(inline) {
            parent = parent.inlineableConfig?.config ?: parent.parentConfig
        } else {
            parent = parent.parentConfig
        }
    }
    return true
}

fun CwtMemberConfig<*>.processDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    return doProcessDescendants(processor)
}

private fun CwtMemberConfig<*>.doProcessDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    processor(this).also { if(!it) return false }
    this.configs?.process { it.doProcessDescendants(processor) }?.also { if(!it) return false }
    return true
}

inline fun <T> Collection<T>.sortedByPriority(crossinline expressionProvider: (T) -> CwtDataExpression, crossinline configGroupProvider: (T) -> CwtConfigGroup): List<T> {
    if(size <= 1) return toListOrThis()
    return sortedByDescending { CwtDataExpressionPriorityProvider.getPriority(expressionProvider(it), configGroupProvider(it)) }
}

val CwtProperty.configPath: CwtConfigPath?
    get() = CwtConfigHandler.getConfigPath(this)

val CwtValue.configPath: CwtConfigPath?
    get() = CwtConfigHandler.getConfigPath(this)

val CwtProperty.configType: CwtConfigType?
    get() = CwtConfigHandler.getConfigType(this)

val CwtValue.configType: CwtConfigType?
    get() = CwtConfigHandler.getConfigType(this)


fun CwtTemplateExpression.extract(referenceName: String): String {
    return CwtTemplateExpressionHandler.extract(this, referenceName)
}

fun CwtTemplateExpression.extract(referenceNames: Map<CwtDataExpression, String>): String {
    return CwtTemplateExpressionHandler.extract(this, referenceNames)
}

fun CwtTemplateExpression.matches(text: String, contextElement: PsiElement, configGroup: CwtConfigGroup, matchOptions: Int = CwtConfigMatcher.Options.Default): Boolean {
    return CwtTemplateExpressionHandler.matches(text, contextElement, this, configGroup, matchOptions)
}

fun CwtTemplateExpression.resolve(text: String, contextElement: PsiElement, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
    return CwtTemplateExpressionHandler.resolve(text, contextElement, this, configGroup)
}

fun CwtTemplateExpression.resolveReferences(text: String, configGroup: CwtConfigGroup): List<ParadoxTemplateSnippetExpressionReference> {
    return CwtTemplateExpressionHandler.resolveReferences(text, this, configGroup)
}

fun CwtTemplateExpression.processResolveResult(contextElement: PsiElement, configGroup: CwtConfigGroup, processor: Processor<String>) {
    CwtTemplateExpressionHandler.processResolveResult(contextElement, this, configGroup, processor)
}

fun <C : CwtConfig<*>> Map<String, List<C>>.getAllByTemplate(text: String, contextElement: PsiElement, configGroup: CwtConfigGroup, matchOptions: Int = CwtConfigMatcher.Options.Default): List<C> {
    return get(text) ?: entries.find { (k) -> CwtTemplateExpression.resolve(k).matches(text, contextElement, configGroup, matchOptions) }?.value ?: emptyList()
}

fun <C : CwtConfig<*>> Map<String, C>.getByTemplate(text: String, contextElement: PsiElement, configGroup: CwtConfigGroup, matchOptions: Int = CwtConfigMatcher.Options.Default): C? {
    return get(text) ?: entries.find { (k) -> CwtTemplateExpression.resolve(k).matches(text, contextElement, configGroup, matchOptions) }?.value
}