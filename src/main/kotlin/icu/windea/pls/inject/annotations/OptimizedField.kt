package icu.windea.pls.inject.annotations

import kotlin.reflect.KClass

/**
 * 要优化的成员字段的信息。
 *
 * @property value 目标字段的名字。
 * @property type 要替换成的字段类型。如果是非私有字段，则必须兼容原始类型（相同或是其子类型）。
 * @property initType 要用于重新初始化字段的类型。必须兼容 [type]（相同或是其子类型）。
 */
@Target(AnnotationTarget.CLASS)
@Repeatable
annotation class OptimizedField(
    val value: String,
    val type: KClass<*>,
    val initType: KClass<*>,
)
