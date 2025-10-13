package icu.windea.pls.config.util.generators

import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.createParentDirectories
import java.nio.file.Path
import kotlin.io.path.writeText

abstract class CwtConfigGeneratorTest : BasePlatformTestCase() {
    val latestStellarisVersion = "v4.1"

    protected fun generate(generator: CwtConfigGenerator, version: String, generateFile: Boolean = true) {
        val title = generator.javaClass.name
        val hint = runWithModalProgressBlocking(project, title) {
            generator.generate(project)
        }
        println(hint.summary)
        println()
        println(hint.details)

        if (generateFile) {
            val fileName = generator.outputPath.substringAfterLast('/')
            val path = Path.of("build", "generated", "config", generator.gameType.id + "_" + version, fileName)
            path.createParentDirectories()
            path.writeText(hint.fileText + "\n") // 在文件末尾添加一个换行符
            println()
            println("Generated file at: ${path}")
        }
    }
}
