package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement

/**
 * 渲染器的统一抽象。
 *
 * 用于将输入（脚本文本、本地化文本、CSV 文本等）渲染为特定的格式（纯文本、富文本等）。
 */
interface ParadoxRenderer<S : ParadoxRenderer.Scope<R>, R> {
    fun createScope(): S

    fun render(input: PsiElement, scope: S = createScope()): R

    interface Scope<R> {
        fun build(): R
    }
}
