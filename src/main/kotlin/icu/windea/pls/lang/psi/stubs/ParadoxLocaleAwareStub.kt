package icu.windea.pls.lang.psi.stubs

import com.intellij.psi.PsiElement

/**
 * 可以获取语言环境的存根。
 *
 * @property locale 语言环境。子存根的语言环境应当从此存根获取。
 */
interface ParadoxLocaleAwareStub<T : PsiElement> : ParadoxStub<T> {
    val locale: String?
}
