package icu.windea.pls.ai.prompts

/**
 * 模板加载器。
 *
 * 用于按给定的相对路径（使用 `/` 作为分隔符）加载模板的原始文本内容。
 * 具体实现可来自类路径、文件系统等位置。
 *
 * @see ClasspathPromptTemplateLoader
 * @see FilePromptTemplateLoader
 */
interface PromptTemplateLoader {
    /**
     * 加载给定路径的模板原始文本。
     *
     * @param path 模版路径（例如：`prompts/template.md`）
     */
    fun load(path: String): String?
}
