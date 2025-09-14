package icu.windea.pls.ai.prompts

/**
 * 提示模板。
 *
 * 表示一个可渲染的模板资源，调用 [render] 以给定的参数生成最终文本。
 *
 * @property path 模版路径（例如：`prompts/template.md`）
 */
interface PromptTemplate {
    val path: String

    /**
     * 渲染模板。
     * - 仅进行一次占位符替换，不会再次解析渲染结果中的占位符。
     * - 在所有指令语法全部渲染完成后，再替换占位符。
     *
     * @param variables 作为占位符与条件的一组变量
     */
    fun render(variables: Map<String, Any?> = emptyMap()): String
}
