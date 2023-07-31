package icu.windea.pls.inject.annotations

/**
 * 注明需要对其结果使用字段缓存的方法。
 * 
 * 所谓字段缓存是指一个注有@Volatile的字段，它的值初始为null，
 * 当调用指定的方法时，如果对应的字段的值不为null，则直接返回这个字段的值。
 * 这些方法必须没有任何参数且拥有一个返回值。
 * 这些字段的值会在指定的清理方法中被清空。
 * 这个清理方法必须没有任何参数。
 * @property methods 目标方法的名字。
 * @property fieldPrefix 生成的字段的名字需要在对应的方法名的基础上加上的前缀。
 * @property cleanupMethod 清理方法的名字。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class FieldCache(
    val methods: Array<String>,
    val cleanupMethod: String,
    val fieldPrefix: String = "__"
)
