package icu.windea.pls.ai.prompts

/**
 * 轻量的提示模版引擎。
 *
 * 支持占位符语法（如 `{{param}}`）以及数种指令语法（如 `<!-- @if predicate -->`）。
 *
 * @see PromptTemplate
 * @see PromptTemplateLoader
 * @see PromptTemplateDirectiveOld
 */
class PromptTemplateEngine(
    val loader: PromptTemplateLoader = ClasspathPromptTemplateLoader(),
) {
    var maxIncludeDepth: Int = 16

    /**
     * 从资源路径渲染模板。
     *
     * @param path 资源路径（如 `prompts/template.md`）
     * @param variables 作为占位符与条件的一组变量
     */
    fun render(path: String, variables: Map<String, Any?> = emptyMap()): String {
        val template = PromptTemplateImpl(path, this)
        return template.render(variables)
    }
}
