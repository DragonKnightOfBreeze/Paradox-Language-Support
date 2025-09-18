package icu.windea.pls.core.psi

import com.intellij.psi.PsiReference

/**
 * 声明“可提供 PSI 引用数组”的能力。
 *
 * 某些 PSI 实现通过实现该接口，允许外部在无需再次解析的情况下快速获取 [PsiReference] 数组。
 */
interface PsiReferencesAware {
    /** 返回当前元素的引用数组；若不可用返回 `null`。*/
    fun getReferences(): Array<out PsiReference>? = null
}
