package icu.windea.pls.inject.annotations

/**
 * 注明需要基于字段缓存的一组方法。
 * 
 * 调用这些方法时，如果对应的字段的值不为空值，则直接返回对应字段的值，否则执行原始方法代码并将结果缓存到对应字段中。
 * 这些方法必须没有任何参数且拥有一个返回值，且允许返回null。
 * 这些字段的值会在指定的可选且没有任何参数的方法中被清空。
 * @property methods 目标方法的名字。
 * @property cleanupMethod 清理方法的名字。
 */
annotation class InjectCachedMethods(
    val methods: Array<String>,
    val cleanupMethod: String
)
