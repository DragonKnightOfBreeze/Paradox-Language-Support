package icu.windea.pls.ai.util

import com.intellij.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.lang.util.manipulators.*
import io.pebbletemplates.pebble.*
import io.pebbletemplates.pebble.template.*
import java.io.*
import java.util.*

object PlsChatMessageManager {
    private val engine by lazy {
        PebbleEngine.Builder().autoEscaping(false).build()
    }

    /**
     * 通过 [Pebble](https://github.com/PebbleTemplates/pebble) 模版引擎生成提示文本。去除首位空白。
     *
     * @param name 名称。位于插件jar包中的`prompts`目录下，去除可能的语言区域后缀以及扩展名后的文件名。
     * @param request 请求对象。模版文件中的`request`变量的值。
     */
    fun fromTemplate(name: String, request: PlsAiRequest): String {
        val template = getTemplate(name)
        val writer = StringWriter()
        val context = mutableMapOf<String, Any?>()
        context.put("request", request)
        context.put("eol", "\n")
        template.evaluate(writer, context)
        return writer.toString().trim()
    }

    private fun getTemplate(name: String): PebbleTemplate {
        val templateName = "prompts/" + name + getLocaleSuffix()
        return engine.getTemplate(templateName)
    }

    private fun getLocaleSuffix(): String {
        return if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "_zh.md" else ".md"
    }

    fun fromLocalisationContexts(localisationContexts: List<ParadoxLocalisationContext>): String {
        return localisationContexts.joinToString("\n") { context -> context.join() }.trim() //去除首尾空白
    }
}
