package icu.windea.pls.core.psi

import com.intellij.psi.PsiReference

/**
 * 表示可直接提供 `PsiReference` 数组的元素。
 *
 * 用于绕过默认的引用收集流程，按需返回已计算好的引用集合。
 */
interface PsiReferencesAware {
    /** 返回引用数组；返回 `null` 表示使用默认机制计算。 */
    fun getReferences(): Array<out PsiReference>? = null
}
