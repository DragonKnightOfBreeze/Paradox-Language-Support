package icu.windea.pls.ai.prompts

/**
 * 自定义提示模板引擎（轻量实现）。
 *
 * @see PromptTemplate
 * @see PromptTemplateImpl
 * @see PromptTemplateLoader
 */
class PromptTemplateEngine(
    private val templateLoader: PromptTemplateLoader = ClasspathPromptTemplateLoader(),
    private val maxIncludeDepth: Int = 16,
) {
    /**
     * 从资源路径渲染模板。
     *
     * @param path 模版路径（例如：`prompts/template.md`）
     * @param variables 作为占位符与条件的一组变量
     */
    fun render(path: String, variables: Map<String, Any?> = emptyMap()): String {
        val template = PromptTemplateImpl(templateLoader, path, maxIncludeDepth)
        return template.render(variables)
    }
}
