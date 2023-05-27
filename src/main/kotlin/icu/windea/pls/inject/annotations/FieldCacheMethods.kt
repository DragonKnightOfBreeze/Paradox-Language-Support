package icu.windea.pls.inject.annotations

/**
 * 注明需要对其结果使用本地缓存的方法。
 * 
 * 所谓字段缓存是指一个注有@Volatile的字段，它的值初始为null，
 * 当调用特定清空方法时，如果对应的字段的值不为null，则直接返回这个字段的值。
 * 这些方法必须没有任何参数且拥有一个返回值。
 * 这个清空方法必须没有任何参数。
 * 这些字段的值会在指定的方法中被清空。
 * @property methods 目标方法名的数组。
 * @property fieldPrefix 生成的字段的名字需要在对应的方法名的基础上加上的前缀。
 * @property cleanupMethod 指定需要在被调用时清空所有本地缓存的方法的名字。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class FieldCacheMethods(
    val methods: Array<String>,
    val cleanupMethod: String,
    val fieldPrefix: String = "__"
)
