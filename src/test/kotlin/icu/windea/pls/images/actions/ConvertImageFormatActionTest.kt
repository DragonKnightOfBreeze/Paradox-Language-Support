package icu.windea.pls.images.actions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleIntegrationsSettings
import icu.windea.pls.integrations.ExternalToolDownloadTestService
import icu.windea.pls.test.ChronicleAssume
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

/**
 * @see ConvertImageFormatAction
 */
@RunWith(JUnit4::class)
class ConvertImageFormatActionTest : BasePlatformTestCase() {
    private val formats = listOf("jpg", "png", "dds", "tga")

    private val imageState get() = ChronicleIntegrationsSettings.getInstance().state.image
    private var originalEnableTexconv = false
    private var originalTexconvPath: String? = null
    private var originalEnableMagick = false
    private var originalMagickPath: String? = null

    @Before
    fun setUpSettings() {
        originalEnableTexconv = imageState.enableTexconv
        originalTexconvPath = imageState.texconvPath
        originalEnableMagick = imageState.enableMagick
        originalMagickPath = imageState.magickPath
        imageState.enableTexconv = false
        imageState.texconvPath = null
        imageState.enableMagick = false
        imageState.magickPath = null
    }

    @After
    fun restoreSettings() {
        imageState.enableTexconv = originalEnableTexconv
        imageState.texconvPath = originalTexconvPath
        imageState.enableMagick = originalEnableMagick
        imageState.magickPath = originalMagickPath
    }

    @Test
    fun defaultStrategy_matrix_test() {
        assertMatrix("default") { sourceFormat, targetFormat -> sourceFormat == targetFormat || targetFormat != "dds" }
    }

    @Test
    fun texconvStrategy_matrix_test() {
        ChronicleAssume.includeExternalTools()
        Assume.assumeTrue("Texconv is Windows-only", SystemInfo.isWindows)
        val descriptor = ExternalToolDownloadTestService.texconv ?: error("Texconv descriptor is missing")
        val exePath = ExternalToolDownloadTestService.ensureTool(descriptor)
        imageState.enableTexconv = true
        imageState.texconvPath = exePath.toString()
        assertMatrix("texconv") { _, _ -> true }
    }

    @Test
    fun magickStrategy_matrix_test() {
        ChronicleAssume.includeExternalTools()
        Assume.assumeTrue("ImageMagick is not tested on this OS", SystemInfo.isWindows || SystemInfo.isLinux)
        val descriptor = ExternalToolDownloadTestService.magick ?: error("ImageMagick descriptor is missing")
        val exePath = ExternalToolDownloadTestService.ensureTool(descriptor)
        imageState.enableMagick = true
        imageState.magickPath = exePath.toString()
        assertMatrix("magick") { _, _ -> true }
    }

    private fun assertMatrix(strategy: String, expectedSuccess: (sourceFormat: String, targetFormat: String) -> Boolean) {
        for (sourceFormat in formats) {
            for (targetFormat in formats) {
                val action = TestConvertImageFormatAction(targetFormat)
                val sourcePath = sourcePath(sourceFormat)
                val targetDirectoryPath = targetDirectoryPath(strategy, sourceFormat, targetFormat)
                val targetFileName = "target.$targetFormat"
                val targetPath = targetDirectoryPath.resolve(targetFileName).also { it.deleteIfExists() }

                val sourceFile = psiFile(sourcePath)
                val targetDirectory = psiDirectory(targetDirectoryPath)
                val result = ApplicationManager.getApplication().runWriteAction<Result<PsiFile?>> {
                    runCatching {
                        action.doConvertImageFormat(sourceFile, targetDirectory, targetFileName)
                    }
                }
                val expected = expectedSuccess(sourceFormat, targetFormat)
                Assert.assertEquals("$strategy: $sourceFormat -> $targetFormat, error=${result.exceptionOrNull()}", expected, result.isSuccess)
                if (expected) {
                    Assert.assertNotNull("$strategy: $sourceFormat -> $targetFormat", result.getOrNull())
                    Assert.assertTrue("Missing converted image: $targetPath", Files.isRegularFile(targetPath))
                    Assert.assertTrue("Empty converted image: $targetPath", Files.size(targetPath) > 0)
                } else {
                    Assert.assertFalse("Unexpected converted image: $targetPath", Files.isRegularFile(targetPath) && Files.size(targetPath) > 0)
                }
            }
        }
    }

    private fun sourcePath(format: String): Path {
        val source = Path.of("src", "test", "testData", "images", "mod_unknown.$format").toAbsolutePath().normalize()
        val target = Path.of("build", "tmp", "convert-image-format-action-test", "sources", "mod_unknown.$format")
            .toAbsolutePath()
            .normalize()
            .also { it.parent.createDirectories() }
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        return target
    }

    private fun targetDirectoryPath(strategy: String, sourceFormat: String, targetFormat: String): Path {
        return Path.of("build", "tmp", "convert-image-format-action-test", strategy, "$sourceFormat-to-$targetFormat")
            .toAbsolutePath()
            .normalize()
            .also { it.createDirectories() }
    }

    private fun psiFile(path: Path): PsiFile {
        val vFile = refreshAndFindFile(path) ?: error("Cannot find file: $path")
        return PsiManager.getInstance(project).findFile(vFile) ?: error("Cannot find PSI file: $path")
    }

    private fun psiDirectory(path: Path): PsiDirectory {
        val vFile = refreshAndFindFile(path) ?: error("Cannot find directory: $path")
        return PsiManager.getInstance(project).findDirectory(vFile) ?: error("Cannot find PSI directory: $path")
    }

    private fun refreshAndFindFile(path: Path): VirtualFile? {
        return ApplicationManager.getApplication().runWriteAction<VirtualFile?> {
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
        }
    }

    private class TestConvertImageFormatAction(targetFormatName: String) : ConvertImageFormatAction(targetFormatName)
}
