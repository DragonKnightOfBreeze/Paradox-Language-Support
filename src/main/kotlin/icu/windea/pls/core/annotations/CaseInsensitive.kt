package icu.windea.pls.core.annotations

import icu.windea.pls.core.collections.CaseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.CaseInsensitiveStringSet

/**
 * 注明这里的字符串类型是忽略大小写的。
 * 如果此类型用作一个集合的类型参数，应对应地使用忽略大小写的特殊集合。
 *
 * @see CaseInsensitiveStringSet
 * @see CaseInsensitiveStringKeyMap
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE)
annotation class CaseInsensitive
