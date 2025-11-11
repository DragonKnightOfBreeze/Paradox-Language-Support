@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import com.intellij.openapi.util.text.StringUtilRt
import icu.windea.pls.core.annotations.CaseInsensitive
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

typealias FastList<E> = ObjectArrayList<E>

typealias FastSet<E> = ObjectLinkedOpenHashSet<E>

typealias FastMap<K, V> = Object2ObjectLinkedOpenHashMap<K, V>

typealias FastCustomSet<E> = ObjectLinkedOpenCustomHashSet<E>

typealias FastCustomMap<K, V> = Object2ObjectLinkedOpenCustomHashMap<K, V>

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
inline fun caseInsensitiveStringSet(): FastCustomSet<@CaseInsensitive String> {
    return FastCustomSet(CaseInsensitiveStringHashingStrategy)
}

/** 创建键为忽略大小写字符串的映射。*/
inline fun <V> caseInsensitiveStringKeyMap(): FastCustomMap<@CaseInsensitive String, V> {
    return FastCustomMap(CaseInsensitiveStringHashingStrategy)
}
