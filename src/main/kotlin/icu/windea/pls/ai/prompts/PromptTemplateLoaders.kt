package icu.windea.pls.ai.prompts

import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.CacheBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 从类路径加载模板内容。
 *
 * - 缓存加载结果。
 */
class ClasspathPromptTemplateLoader(
    private val classLoader: ClassLoader = PromptTemplateEngine::class.java.classLoader
) : PromptTemplateLoader {
    private val cache = CacheBuilder("expireAfterAccess=1h").build<String, String> { doLoad(it).orEmpty() }

    override fun load(path: String): String? {
        return cache.get(path).orNull()
    }

    private fun doLoad(path: String): String? {
        val normalized = path.replace('\\', '/').trimStart('/')
        return classLoader.getResource(normalized)?.readText()
    }
}

/**
 * 从文件系统加载模板内容。
 *
 * - 缓存加载结果。
 * - 使用 [baseDir] 作为路径解析的根目录，所有加载都被限制在此目录内。
 */
data class FilePromptTemplateLoader(
    private val baseDir: Path
) : PromptTemplateLoader {
    private val cache = CacheBuilder("expireAfterAccess=1h").build<String, String> { doLoad(it).orEmpty() }

    override fun load(path: String): String? {
        return cache.get(path).orNull()
    }

    private fun doLoad(path: String): String? {
        // 将相对路径解析到 baseDir 下，并防止越界访问
        val relative: Path = Paths.get(path.replace('/', File.separatorChar))
        val resolved = baseDir.resolve(relative).normalize()
        if (!resolved.startsWith(baseDir)) return null
        if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) return null
        return Files.readString(resolved, StandardCharsets.UTF_8)
    }
}
