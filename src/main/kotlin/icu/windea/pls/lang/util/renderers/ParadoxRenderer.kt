package icu.windea.pls.lang.util.renderers

/**
 * 渲染器的统一抽象。
 *
 * 用于将输入（脚本成员结构、本地化文本等）渲染为特定的格式（纯文本、富文本等）。
 */
interface ParadoxRenderer<T, C, R> {
    fun initContext(): C

    fun render(input: T, context: C): R
}
