package icu.windea.pls.tools.config.generators

import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.createParentDirectories
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleAssume
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path
import kotlin.io.path.writeText

/**
 * @see CwtConfigGenerator
 */
@RunWith(JUnit4::class)
abstract class CwtConfigGeneratorTest : BasePlatformTestCase() {
    @Before
    fun doSetUp() = ChronicleAssume.includeConfigGenerator()

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
