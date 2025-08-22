package icu.windea.pls.ai.util

import com.intellij.DynamicBundle.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.lang.util.manipulators.*
import io.pebbletemplates.pebble.*
import java.io.*
import java.util.Locale.*

object PlsChatMessageManager {
    private val engine by lazy {
        PebbleEngine.Builder().autoEscaping(false)
            .build()
    }

    /**
     * 通过 [Pebble](https://github.com/PebbleTemplates/pebble) 模版引擎生成提示文本。
     *
     * - 最终使用的模版名是`prompts/{templateId}.md`。如果界面语言是简体中文，则是`prompts/{templateId}_zh.md`。
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
        val prefix = "prompts/"
        val suffix = if (getLocale() == SIMPLIFIED_CHINESE) "_zh.md" else ".md"
        return prefix + templateId + suffix
    }

    fun fromLocalisationContexts(localisationContexts: List<ParadoxLocalisationContext>): String {
        //去除首尾空白
        return localisationContexts.joinToString("\n") { context -> "${context.key} \"${context.text}\"" }.trim()
    }
}
