package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement

/**
 * 渲染器的统一抽象。
 *
 * 用于将输入（脚本片段、本地化文本等）渲染为特定的格式（纯文本、富文本等）。
 */
interface ParadoxRenderer<C, R> {
    fun initContext(): C

    fun render(input: PsiElement, context: C): R
}
