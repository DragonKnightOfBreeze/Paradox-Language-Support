package icu.windea.pls.ai.util

import com.intellij.DynamicBundle.getLocale
import icu.windea.pls.ai.model.requests.AiRequest
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import java.io.StringWriter
import java.util.Locale.SIMPLIFIED_CHINESE

object PlsChatMessageManager {
    private val engine by lazy {
        val loader = ClasspathLoader().apply { prefix = "prompts/" }
        PebbleEngine.Builder().loader(loader).autoEscaping(false).build()
    }

    /**
     * 通过 [Pebble](https://github.com/PebbleTemplates/pebble) 模版引擎生成提示文本。
     *
     * - 模版文件统一位于`prompts`目录下。
     * - 最终使用的模版名是`{templateId}.md`。如果界面语言是简体中文，则是`prompts/{templateId}_zh.md`。
     * - 最终得到的提示文本会去除首尾空白。
     *
     * @param templateId 模版ID。
     * @param request 请求对象。模版文件中的`request`变量的值。
     */
    fun fromTemplate(templateId: String, request: AiRequest): String {
        val templateName = getTemplateName(templateId)
        val template = engine.getTemplate(templateName)
        val writer = StringWriter()
        val context = mutableMapOf<String, Any?>()
        context.put("request", request)
        context.put("eol", "\n")
        template.evaluate(writer, context)
        return writer.toString().trim()
    }

    private fun getTemplateName(templateId: String): String {
        val suffix = if (getLocale() == SIMPLIFIED_CHINESE) "_zh.md" else ".md"
        return templateId + suffix
    }

    fun fromLocalisationContexts(localisationContexts: List<ParadoxLocalisationContext>): String {
        //去除首尾空白
        return localisationContexts.joinToString("\n") { context -> "${context.key}: \"${context.newText}\"" }.trim()
    }
}
