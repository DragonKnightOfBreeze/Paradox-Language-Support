package icu.windea.pls.config.util.generators

import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.createParentDirectories
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path
import kotlin.io.path.writeText

abstract class CwtConfigGeneratorTest : BasePlatformTestCase() {
    val latestStellarisVersion = "v4.1.5"

    protected fun generate(
        generator: CwtConfigGenerator,
        gameType: ParadoxGameType,
        inputPath: String,
        outputPath: String,
        generatedFileDirectory: String = gameType.id,
    ) {
        val title = generator.javaClass.name
        val hint = runWithModalProgressBlocking(project, title) {
            generator.generate(gameType, inputPath, outputPath)
        }
        println(hint.summary)
        println()
        println(hint.details)

        val fileName = outputPath.substringAfterLast('/')
        val path = Path.of("build", "generated", "config", generatedFileDirectory, fileName)
        path.createParentDirectories()
        path.writeText(hint.fileText)
        println()
        println("Generated file at: ${path}")
    }
}
