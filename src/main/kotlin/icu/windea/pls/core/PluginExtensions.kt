package icu.windea.pls.core

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

fun String.isParameterizedExactIdentifier(vararg extraChars: Char): Boolean {
    var isParameter = false
    for(c in this) {
        when {
            c == '$' -> {
                isParameter = !isParameter
            }
            isParameter -> {}
            c.isExactIdentifierChar() || c in extraChars -> {}
            else -> return false
        }
    }
    return true
}

fun String.isParameterized(): Boolean {
    return this.any { it == '$' }
}

fun String.isInlineUsage(): Boolean {
    return this.lowercase() == ParadoxInlineScriptHandler.inlineScriptName
}


/**
 * 基于注解[WithGameType]判断目标对象是否支持当前游戏类型。
 */
fun ParadoxGameType?.supportsByAnnotation(target: Any): Boolean {
    if(this == null) return true
    val targetGameType = target.javaClass.getAnnotation(WithGameType::class.java)?.value ?: return true
    return this in targetGameType
}