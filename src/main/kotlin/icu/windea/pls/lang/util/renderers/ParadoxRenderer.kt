package icu.windea.pls.lang.util.renderers

import com.intellij.psi.PsiElement

/**
 * 渲染器的统一抽象。
 *
 * 用于将输入（脚本文本、本地化文本、CSV 文本等）渲染为特定的格式（纯文本、富文本等）。
 */
interface ParadoxRenderer<T, C : ParadoxRenderContext<T>, S : ParadoxRenderSettings> {
    val settings: S

    fun createContext(): C

    fun render(input: PsiElement, context: C): T
}

/**
 * 渲染上下文。
 */
interface ParadoxRenderContext<T> {
    fun build(): T
}

/**
 * 渲染设置。
 */
interface ParadoxRenderSettings
