package icu.windea.pls.ai.util

import com.intellij.*
import icu.windea.pls.ai.requests.*
import org.apache.velocity.*
import org.apache.velocity.app.*
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import java.io.*
import java.util.*

object PlsPromptManager {
    private class Loader : ClasspathResourceLoader() //这里的继承是必要的，否则找到的 classLoader 会不正确

    private val velocityEngine by lazy {
       val properties = Properties()
        properties.setProperty("resource.loader", "classpath")
        properties.setProperty("classpath.resource.loader.class", PlsPromptResourceLoader::class.java.name)
        properties.setProperty("classpath.resource.loader.cache", "true")
        VelocityEngine(properties).apply {
            init()
            setProperty("resource.loaders", "classpath")
            setProperty("resource.loader.classpath.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")
            setProperty("resource.loader.classpath.cache", "true")
        }
    }

    /**
     * 通过[Velocity](https://velocity.apache.org/)模版引擎生成提示文本。
     *
     * @param name 名称。位于插件jar包中的`prompts`目录下。去除可能的语言区域后缀以及扩展名后的文件名。
     * @param request 请求对象。作为[VelocityContext]中的 request`变量的值。
     */
    fun fromTemplate(name: String, request: PlsAiRequest): String {
        val template = getTemplate(name)
        val writer = StringWriter()
        val context = VelocityContext()
        context.put("request", request)
        template.merge(context, writer)
        return writer.toString()
    }

    private fun getTemplate(name: String): Template {
        val templateName = "prompts/" + name + getLocaleSuffix()
        return velocityEngine.getTemplate(templateName)
    }

    private fun getLocaleSuffix(): String {
        return if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "_zh.vm" else ".vm"
    }
}
