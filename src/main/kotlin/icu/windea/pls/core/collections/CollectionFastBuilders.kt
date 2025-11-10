@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

import com.intellij.openapi.util.text.StringUtilRt
import icu.windea.pls.core.annotations.CaseInsensitive
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet

/** 忽略大小写的字符串哈希与相等策略。*/
object CaseInsensitiveStringHashingStrategy : Hash.Strategy<String?> {
    override fun hashCode(s: String?): Int {
        return if (s == null) 0 else StringUtilRt.stringHashCodeInsensitive(s)
    }

    override fun equals(s1: String?, s2: String?): Boolean {
        return s1.equals(s2, ignoreCase = true)
    }
}

/** 创建忽略大小写的字符串集合。*/
inline fun caseInsensitiveStringSet(): ObjectLinkedOpenCustomHashSet<@CaseInsensitive String> {
    return ObjectLinkedOpenCustomHashSet(CaseInsensitiveStringHashingStrategy)
}

/** 创建键为忽略大小写字符串的映射。*/
inline fun <V> caseInsensitiveStringKeyMap(): Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, V> {
    return Object2ObjectLinkedOpenCustomHashMap(CaseInsensitiveStringHashingStrategy)
}
