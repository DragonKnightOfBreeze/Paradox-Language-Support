package icu.windea.pls.config

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.dataExpression.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

val Project.configGroupLibrary: CwtConfigGroupLibrary
    get() = this.getOrPutUserData(PlsKeys.configGroupLibrary) { CwtConfigGroupLibrary(this) }

/**
 * 解析为被内联的CWT规则，或者返回自身。
 */
@Suppress("UNCHECKED_CAST")
fun <T: CwtConfig<*>> T.resolved(): T {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config ?: this
        else -> this
    } as T
}

/**
 * 解析为被内联的规则，或者返回null。
 */
@Suppress("UNCHECKED_CAST")
fun <T: CwtConfig<*>> T.resolvedOrNull(): T? {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config
        else -> this
    } as? T
}

inline fun CwtMemberConfig<*>.processParent(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    var parent = this.parentConfig
    while(parent != null) {
        val result = processor(parent)
        if(!result) return false
        parent = parent.parentConfig
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

inline fun <T> Collection<T>.sortedByPriority(crossinline expressionProvider: (T) -> CwtDataExpression?, crossinline configGroupProvider: (T) -> CwtConfigGroup): List<T> {
    if(size <= 1) return toListOrThis()
    return sortedByDescending s@{
        val expression = expressionProvider(it) ?: return@s Double.MAX_VALUE
        val configGroup = configGroupProvider(it)
        CwtDataExpressionPriorityProvider.getPriority(expression, configGroup)
    }
}

val CwtMemberElement.configPath: CwtConfigPath?
    get() = CwtConfigManager.getConfigPath(this)

val CwtMemberElement.configType: CwtConfigType?
    get() = CwtConfigManager.getConfigType(this)

fun <T : CwtMemberElement> T.bindConfig(config: CwtConfig<*>): T {
    this.putUserData(PlsKeys.bindingConfig, config)
    return this
}

fun CwtTemplateExpression.extract(referenceName: String): String {
    return CwtTemplateExpressionManager.extract(this, referenceName)
}

fun CwtTemplateExpression.extract(referenceNames: Map<CwtDataExpression, String>): String {
    return CwtTemplateExpressionManager.extract(this, referenceNames)
}

fun CwtTemplateExpression.matches(text: String, contextElement: PsiElement, configGroup: CwtConfigGroup, matchOptions: Int = ParadoxExpressionMatcher.Options.Default): Boolean {
    return CwtTemplateExpressionManager.matches(text, contextElement, this, configGroup, matchOptions)
}

fun CwtTemplateExpression.resolve(text: String, contextElement: PsiElement, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
    return CwtTemplateExpressionManager.resolve(text, contextElement, this, configGroup)
}

fun CwtTemplateExpression.resolveReferences(text: String, configGroup: CwtConfigGroup): List<ParadoxTemplateSnippetExpressionReference> {
    return CwtTemplateExpressionManager.resolveReferences(text, this, configGroup)
}

fun CwtTemplateExpression.processResolveResult(contextElement: PsiElement, configGroup: CwtConfigGroup, processor: Processor<String>) {
    CwtTemplateExpressionManager.processResolveResult(contextElement, this, configGroup, processor)
}
