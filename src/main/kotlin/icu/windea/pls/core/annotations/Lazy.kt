package icu.windea.pls.core.annotations

/**
 * 注明此属性在运行时需要使用一种不会带来额外内存开销的、线程安全的懒加载。
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class Lazy