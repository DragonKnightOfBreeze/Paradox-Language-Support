package icu.windea.pls.ai.prompts

/**
 * 提示模板的加载器。
 *
 * 用于根据指定的相对路径（使用 `/` 作为分隔符），从类路径、文件系统等位置加载模板内容。
 *
 * @see ClasspathPromptTemplateLoader
 * @see FilePromptTemplateLoader
 */
interface PromptTemplateLoader {
    /**
     * 加载指定资源路径的模板的原始文本。
     */
    fun load(path: String): String?
}
