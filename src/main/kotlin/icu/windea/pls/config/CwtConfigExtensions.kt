@file:Suppress("unused")

package icu.windea.pls.config

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

inline fun CwtDataConfig<*>.processParent(inline: Boolean = false, processor: (CwtDataConfig<*>) -> Boolean): Boolean {
    var parent = this.parent
    while(parent != null) {
        val result = processor(parent)
        if(!result) return false
        if(inline) {
            parent = parent.inlineableConfig?.config ?: parent.parent
        } else {
            parent = parent.parent
        }
    }
    return true
}

fun CwtDataConfig<*>.processDescendants(processor: (CwtDataConfig<*>) -> Boolean): Boolean {
    return doProcessDescendants(processor)
}

private fun CwtDataConfig<*>.doProcessDescendants(processor: (CwtDataConfig<*>) -> Boolean): Boolean {
    processor(this).also { if(!it) return false }
    this.configs?.process { it.doProcessDescendants(processor) }?.also { if(!it) return false }
    return true
}

fun CwtConfig<*>.findAliasConfig(): CwtAliasConfig? {
    return when {
        this is CwtPropertyConfig -> this.inlineableConfig?.castOrNull()
        this is CwtValueConfig -> this.propertyConfig?.inlineableConfig?.castOrNull()
        this is CwtAliasConfig -> this
        else -> null
    }
}

inline fun <T> Iterable<T>.sortedByPriority(configGroup: CwtConfigGroup, crossinline expressionExtractor: (T) -> CwtDataExpression): List<T> {
    return this.sortedByDescending { ParadoxConfigHandler.getPriority(expressionExtractor(it), configGroup) }
}


val CwtProperty.configPath: CwtConfigPath?
    get() = CwtConfigHandler.get(this)

val CwtValue.configPath: CwtConfigPath?
    get() = CwtConfigHandler.get(this)

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

fun CwtTemplateExpression.matches(text: String, element: PsiElement, configGroup: CwtConfigGroup, matchOptions: Int = ParadoxConfigMatcher.Options.Default): Boolean {
    return CwtTemplateExpressionHandler.matches(text, element, this, configGroup, matchOptions)
}

fun CwtTemplateExpression.resolve(text: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
    return CwtTemplateExpressionHandler.resolve(text, element, this, configGroup)
}

fun CwtTemplateExpression.resolveReferences(text: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): List<ParadoxTemplateSnippetExpressionReference> {
    return CwtTemplateExpressionHandler.resolveReferences(text, element, this, configGroup)
}

fun CwtTemplateExpression.processResolveResult(contextElement: PsiElement, configGroup: CwtConfigGroup, processor: Processor<String>) {
    CwtTemplateExpressionHandler.processResolveResult(contextElement, this, configGroup, processor)
}

fun <C : CwtConfig<*>> Map<String, C>.getByTemplate(text: String, element: PsiElement, configGroup: CwtConfigGroup, matchOptions: Int = ParadoxConfigMatcher.Options.Default): C? {
    return get(text) ?: entries.find { (k) -> CwtTemplateExpression.resolve(k).matches(text, element, configGroup, matchOptions) }?.value
}