package icu.windea.pls.config

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.path.*

val Project.configGroupLibrary: CwtConfigGroupLibrary
    get() = this.getOrPutUserData(PlsKeys.configGroupLibrary) { CwtConfigGroupLibrary(this) }

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

val CwtMemberElement.configPath: CwtConfigPath?
    get() = CwtConfigManager.getConfigPath(this)

val CwtMemberElement.configType: CwtConfigType?
    get() = CwtConfigManager.getConfigType(this)


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
