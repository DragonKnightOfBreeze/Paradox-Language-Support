package icu.windea.pls.config.util.generators

import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.createParentDirectories
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path
import kotlin.io.path.writeText

abstract class CwtConfigGeneratorTest : BasePlatformTestCase() {
    val latestStellarisVersion = "v4.1"

    protected fun execute(generator: CwtConfigGenerator, gameType: ParadoxGameType, inputPath: String, outputPath: String, version: String) {
        val title = generator.javaClass.name
        val hint = runWithModalProgressBlocking(project, title) {
            generator.generate(gameType, inputPath, outputPath)
        }
        println(hint.summary)
        println()
        println(hint.details)

        val fileName = outputPath.substringAfterLast('/')
        val path = Path.of("build", "generated", "config", gameType.id + "_" + version, fileName)
        path.createParentDirectories()
        path.writeText(hint.fileText + "\n") // 在文件末尾添加一个换行符
        println()
        println("Generated file at: ${path}")
    }
}
