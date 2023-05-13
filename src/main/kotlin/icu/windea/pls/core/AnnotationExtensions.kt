package icu.windea.pls.core

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*

/**
 * 基于注解[WithGameType]判断目标对象是否支持当前游戏类型。
 */
fun ParadoxGameType?.supportsByAnnotation(target: Any): Boolean {
    if(this == null) return true
    val targetGameType = target.javaClass.getAnnotation(WithGameType::class.java)?.value ?: return true
    return this in targetGameType
}