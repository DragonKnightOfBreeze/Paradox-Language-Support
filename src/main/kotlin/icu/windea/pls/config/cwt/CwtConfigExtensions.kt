@file:Suppress("unused")

package icu.windea.pls.config.cwt

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*

inline fun CwtDataConfig<*>.processParent(processor: ProcessEntry.(CwtDataConfig<*>) -> Boolean): Boolean {
    var parent = this.parent
    while(parent != null) {
        val result = ProcessEntry.processor(parent)
        if(!result) return false
        parent = parent.parent
    }
    return true
}

inline fun CwtDataConfig<*>.processParentProperty(processor: ProcessEntry.(CwtPropertyConfig) -> Boolean): Boolean {
    var parent = this.parent
    while(parent != null) {
        if(parent is CwtPropertyConfig) {
            val result = ProcessEntry.processor(parent)
            if(!result) return false
        }
        parent = parent.parent
    }
    return true
}

fun CwtDataConfig<*>.processDescendants(processor: ProcessEntry.(CwtDataConfig<*>) -> Boolean): Boolean {
    return doProcessDescendants(processor)
}

private fun CwtDataConfig<*>.doProcessDescendants(processor: ProcessEntry.(CwtDataConfig<*>) -> Boolean): Boolean {
    ProcessEntry.processor(this).also { if(!it) return false }
    this.properties?.process { it.doProcessDescendants(processor) }?.also { if(!it) return false }
    this.values?.process { it.doProcessDescendants(processor) }?.also { if(!it) return false }
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
    return this.sortedByDescending { CwtConfigHandler.getPriority(expressionExtractor(it), configGroup) }
}

/**
 * 判断指定的定义子类型表达式是否匹配一组子类型。
 * @param expression 表达式。示例：`origin`, `!origin`
 * @param subtypes 子类型列表。如果为null则表明不指定子类型，总是认为匹配。
 */
fun matchesDefinitionSubtypeExpression(expression: String, subtypes: List<String>?): Boolean {
    return when {
        subtypes == null -> true
        expression.startsWith('!') -> subtypes.isEmpty() || expression.drop(1) !in subtypes
        else -> subtypes.isNotEmpty() && expression in subtypes
    }
}


val CwtProperty.configPath: CwtConfigPath?
    get() = CwtConfigPathHandler.get(this)

val CwtValue.configPath: CwtConfigPath?
    get() = CwtConfigPathHandler.get(this)

val CwtProperty.configType: CwtConfigType?
    get() = CwtConfigTypeHandler.get(this)

val CwtValue.configType: CwtConfigType?
    get() = CwtConfigTypeHandler.get(this)


fun CwtTemplateExpression.extract(referenceName: String): String {
    return CwtTemplateExpressionHandler.extract(this, referenceName)
}

fun CwtTemplateExpression.extract(referenceNames: Map<CwtDataExpression, String>): String {
    return CwtTemplateExpressionHandler.extract(this, referenceNames)
}

fun CwtTemplateExpression.matches(text: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL) : Boolean {
    return CwtTemplateExpressionHandler.matches(text, this, configGroup, matchType)
}

fun CwtTemplateExpression.resolve(element: ParadoxScriptStringExpressionElement, text: String, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
    return CwtTemplateExpressionHandler.resolve(element, text, this, configGroup)
}

fun CwtTemplateExpression.resolveReferences(element: ParadoxScriptStringExpressionElement, text: String, configGroup: CwtConfigGroup): List<ParadoxInTemplateExpressionReference>? {
    return CwtTemplateExpressionHandler.resolveReferences(element, text, this, configGroup)
}

fun CwtTemplateExpression.processResolveResult(configGroup: CwtConfigGroup, processor: Processor<String>) {
    CwtTemplateExpressionHandler.processResolveResult(this, configGroup, processor)
}