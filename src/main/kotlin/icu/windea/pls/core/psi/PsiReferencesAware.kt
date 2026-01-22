package icu.windea.pls.core.psi

import com.intellij.psi.PsiReference

/**
 * 声明“可以用来获取一组 PSI 引用”的能力。
 */
interface PsiReferencesAware {
    /** 获取一组 PSI 引用。如果无法获取，则返回空数组或 `null`。 */
    fun getReferences(): Array<out PsiReference>? = null
}
