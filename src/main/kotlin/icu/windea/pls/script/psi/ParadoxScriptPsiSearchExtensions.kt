@file:Suppress("UNCHECKED_CAST")

package icu.windea.pls.script.psi

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.inline.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*

/**
 * 遍历当前代码块中的所有（直接作为子节点的）值和属性。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
fun ParadoxScriptBlockElement.processData(
    conditional: Boolean = false,
    inline: Boolean = false,
    processor: (ParadoxScriptMemberElement) -> Boolean
): Boolean {
    return doProcessData(conditional, inline, processor)
}

/**
 * 遍历当前代码块中的所有（直接作为子节点的）属性。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
fun ParadoxScriptBlockElement.processProperty(
    conditional: Boolean = false,
    inline: Boolean = false,
    processor: (ParadoxScriptProperty) -> Boolean
): Boolean {
    return doProcessProperty(conditional,inline, processor)
}

/**
 * 遍历当前代码块中的所有（直接作为子节点的）值。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的值。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
fun ParadoxScriptBlockElement.processValue(
    conditional: Boolean = false,
    inline: Boolean = false,
    processor: (ParadoxScriptValue) -> Boolean
): Boolean {
    return doProcessValue(conditional, inline, processor)
}

/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）值或属性。
 */
fun ParadoxScriptParameterCondition.processData(
    conditional: Boolean = false,
    inline: Boolean = false,
    processor: (ParadoxScriptMemberElement) -> Boolean
): Boolean {
    return doProcessData(conditional, inline, processor)
}

/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）属性。
 */
fun ParadoxScriptParameterCondition.processProperty(
    conditional: Boolean = false,
    inline: Boolean = false,
    processor: (ParadoxScriptProperty) -> Boolean
): Boolean {
    return doProcessProperty(conditional,inline, processor)
}

/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）值。
 */
fun ParadoxScriptParameterCondition.processValue(
    conditional: Boolean = false,
    inline: Boolean = false,
    processor: (ParadoxScriptValue) -> Boolean
): Boolean {
    return doProcessValue(conditional, inline, processor)
}

private fun PsiElement.doProcessData(conditional: Boolean, inline: Boolean, processor: (ParadoxScriptMemberElement) -> Boolean): Boolean {
    return processChild {
        ProgressManager.checkCanceled()
        when {
            it is ParadoxScriptValue -> it.doProcessValueChild(conditional, inline, processor)
            it is ParadoxScriptProperty -> it.doProcessPropertyChild(conditional, inline, processor)
            conditional && it is ParadoxScriptParameterCondition -> it.doProcessData(true, inline, processor)
            else -> true
        }
    }
}

private fun PsiElement.doProcessProperty(conditional: Boolean,inline: Boolean, processor: (ParadoxScriptProperty) -> Boolean): Boolean {
    return processChild {
        ProgressManager.checkCanceled()
        when {
            it is ParadoxScriptProperty -> it.doProcessPropertyChild(conditional, inline, processor)
            conditional && it is ParadoxScriptParameterCondition -> it.doProcessProperty(true, inline,processor)
            else -> true
        }
    }
}

private fun PsiElement.doProcessValue(conditional: Boolean, inline: Boolean, processor: (ParadoxScriptValue) -> Boolean): Boolean {
    return processChild {
        ProgressManager.checkCanceled()
        when {
            it is ParadoxScriptValue -> it.doProcessValueChild(conditional, inline, processor)
            conditional && it is ParadoxScriptParameterCondition -> it.doProcessValue(true,inline, processor)
            else -> true
        }
    }
}

private fun ParadoxScriptValue.doProcessValueChild(conditional: Boolean, inline: Boolean, processor: (ParadoxScriptValue) -> Boolean): Boolean {
    val r = processor(this)
    if(!r) return false
    if(inline) {
        val inlined = ParadoxInlineSupport.inlineElement(this)
        if(inlined is ParadoxScriptDefinitionElement) {
            val block = inlined.block
            if(block != null) {
                val r1 = block.doProcessValue(conditional, true, processor)
                if(!r1) return false
            }
        }
        //不处理inlined是value的情况
    }
    return true
}

private fun ParadoxScriptProperty.doProcessPropertyChild(conditional: Boolean, inline: Boolean, processor: (ParadoxScriptProperty) -> Boolean): Boolean {
    val r = processor(this)
    if(!r) return false
    if(inline) {
        val inlined = ParadoxInlineSupport.inlineElement(this)
        if(inlined is ParadoxScriptDefinitionElement) {
            val block = inlined.block
            if(block != null) {
                val r1 = block.doProcessProperty(conditional, true, processor)
                if(!r1) return false
            }
        }
        //不处理inlined是value的情况
    }
    return true
}

/**
 * 得到指定名字的属性。
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果为空字符串且自身是脚本属性，则返回自身
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
fun PsiElement.findProperty(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false
): ParadoxScriptProperty? {
    if(language != ParadoxScriptLanguage) return null
    if(propertyName != null && propertyName.isEmpty()) return this as? ParadoxScriptProperty
    val block = when {
        this is ParadoxScriptDefinitionElement -> this.block
        this is ParadoxScriptBlock -> this
        else -> null
    }
    var result: ParadoxScriptProperty? = null
    block?.processProperty(conditional, inline) {
        if(propertyName == null || propertyName.equals(it.name, ignoreCase)) {
            result = it
            false
        } else {
            true
        }
    }
    return result
}

/**
 * 基于路径向下查找指定的属性或值。如果路径为空，则返回查找到的第一个属性或值。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 * @see ParadoxElementPath
 * @see ParadoxScriptMemberElement
 */
inline fun <reified T : ParadoxScriptMemberElement> ParadoxScriptMemberElement.findByPath(
    path: String = "",
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false
): T? {
    val targetType = T::class.java
    return findByPath(targetType, path, ignoreCase, conditional, inline)
}

fun <T : ParadoxScriptMemberElement> ParadoxScriptMemberElement.findByPath(
    targetType: Class<T>,
    path: String,
    ignoreCase: Boolean,
    conditional: Boolean,
    inline: Boolean
): T? {
    if(language != ParadoxScriptLanguage) return null
    var current: ParadoxScriptMemberElement = this
    if(path.isNotEmpty()) {
        val elementPath = ParadoxElementPath.resolve(path)
        val subPathInfos = elementPath.subPaths
        for(subPathInfo in subPathInfos) {
            val subPath = subPathInfo.subPath
            if(subPath == "-") return null //TODO 暂不支持
            current = current.findProperty(subPath, ignoreCase, conditional, inline) ?: return null
        }
    } else {
        current = current.findProperty("", ignoreCase, conditional, inline) ?: return null
    }
    when {
        ParadoxScriptProperty::class.java.isAssignableFrom(targetType) -> {
            return current.castOrNull<ParadoxScriptProperty>() as? T
        }
        ParadoxScriptValue::class.java.isAssignableFrom(targetType) -> {
            return current.castOrNull<ParadoxScriptProperty>()?.propertyValue() as? T
        }
    }
    return null
}


/**
 * 向上得到第一个定义。
 * 可能为null，可能为自身。
 */
fun PsiElement.findParentDefinition(): ParadoxScriptDefinitionElement? {
    if(language != ParadoxScriptLanguage) return null
    var current: PsiElement = this
    while(current !is PsiDirectory) {
        ProgressManager.checkCanceled()
        if(current is ParadoxScriptDefinitionElement && current.definitionInfo != null) return current
        current = current.parent ?: break
    }
    return null
}

/**
 * 向上得到第一个属性。可能为null，可能是定义，可能是脚本文件。
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果得到的是脚本文件，则忽略。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
fun PsiElement.findParentProperty(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    fromParentBlock: Boolean = false
): ParadoxScriptDefinitionElement? {
    if(language != ParadoxScriptLanguage) return null
    var current: PsiElement = when {
        fromParentBlock -> this.parentOfType<ParadoxScriptBlockElement>() ?: return null
        this is ParadoxScriptProperty -> this.parent
        else -> this
    }
    while(current !is PsiFile) {
        ProgressManager.checkCanceled()
        if(current is ParadoxScriptDefinitionElement) return current.takeIf { propertyName == null || propertyName.equals(it.name, ignoreCase) }
        if(current is ParadoxScriptBlock && !current.isPropertyValue()) return null
        current = current.parent ?: break
    }
    if(current is ParadoxScriptFile) return current
    return null
}

/**
 * 基于路径向上查找指定的属性。如果路径为空，则返回查找到的第一个属性或值。
 * @param definitionType 如果不为null则在查找到指定的属性之后再向上查找一层属性，并要求其是定义，如果接着不为空字符串则要求匹配该定义类型表达式。
 * @see ParadoxElementPath
 * @see ParadoxScriptMemberElement
 * @see ParadoxDefinitionTypeExpression
 */
fun ParadoxScriptMemberElement.findParentByPath(
    path: String = "",
    ignoreCase: Boolean = true,
    definitionType: String? = null
): ParadoxScriptDefinitionElement? {
    if(language != ParadoxScriptLanguage) return null
    var current: ParadoxScriptMemberElement = this
    if(path.isNotEmpty()) {
        val elementPath = ParadoxElementPath.resolve(path)
        val subPathInfos = elementPath.subPaths
        for(subPathInfo in subPathInfos.reversed()) {
            val subPath = subPathInfo.subPath
            if(subPath == "-") return null //TODO 暂不支持
            current = current.findParentProperty(subPath, ignoreCase) ?: return null
        }
    }
    if(definitionType != null) {
        val result = current.findParentProperty(null) ?: return null
        val definitionInfo = result.definitionInfo ?: return null
        if(definitionType.isNotEmpty()) {
            if(!ParadoxDefinitionTypeExpression.resolve(definitionType).matches(definitionInfo)) return null
        }
        return result
    }
    return current as? ParadoxScriptDefinitionElement?
}