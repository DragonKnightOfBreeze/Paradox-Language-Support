package icu.windea.pls.core.annotations.api

/**
 * 注明此API是隐藏的。这意味着它所对应的功能和扩展是非标准的，并且不会在标准参考文档和标准CHANGELOG中写明。 
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class HiddenApi
