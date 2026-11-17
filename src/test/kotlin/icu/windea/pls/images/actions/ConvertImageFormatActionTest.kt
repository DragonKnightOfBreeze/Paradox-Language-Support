package icu.windea.pls.images.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.refactoring.SkipOverwriteChoice
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleIntegrationSettings
import icu.windea.pls.images.ImageContext
import icu.windea.pls.images.ImageSupport
import icu.windea.pls.integrations.ExternalToolDownloadTestService
import icu.windea.pls.test.ChronicleAssume
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.Callable
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile

/**
 * @see ConvertImageFormatAction
 * @see ConvertImageFormatActions
 */
@RunWith(JUnit4::class)
class ConvertImageFormatActionTest : BasePlatformTestCase() {
    private val formats = listOf("jpg", "png", "gif", "dds", "tga")
    private val root = Path.of("build", "tmp", "convert-image-format-action-test")

    private val imageState get() = ChronicleIntegrationSettings.getInstance().state.image
    private var originalEnableTexconv = false
    private var originalTexconvPath: String? = null
    private var originalEnableMagick = false
    private var originalMagickPath: String? = null

    @Before
    fun setUpSettings() {
        root.toFile().deleteRecursively()
        root.createDirectories()
        // 注册内置的 DDS/TGA ImageIO SPI，确保测试不依赖插件启动生命周期
        ImageContext.init()
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

    // region 转换策略矩阵

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
                val action = TestTransientConvertImageFormatAction(targetFormat)
                val sourcePath = prepareSource(sourceFormat)
                val targetDirectoryPath = tempDir("matrix/$strategy/$sourceFormat-to-$targetFormat")
                val targetFileName = "target.$targetFormat"
                val targetPath = targetDirectoryPath.resolve(targetFileName).also { it.deleteIfExists() }

                val sourceFile = psiFile(sourcePath)
                val targetDirectory = psiDirectory(targetDirectoryPath)
                val result = runCatching {
                    runSuspending { action.convertImageFormat(sourceFile, targetDirectory, targetFileName) }
                }
                val expected = expectedSuccess(sourceFormat, targetFormat)
                Assert.assertEquals("$strategy: $sourceFormat -> $targetFormat, error=${result.exceptionOrNull()}", expected, result.isSuccess)
                if (expected) {
                    Assert.assertNotNull("$strategy: $sourceFormat -> $targetFormat", result.getOrNull())
                    Assert.assertTrue("Missing converted image: $targetPath", targetPath.isRegularFile())
                    Assert.assertTrue("Empty converted image: $targetPath", targetPath.fileSize() > 0)
                } else {
                    Assert.assertFalse("Unexpected converted image: $targetPath", targetPath.isRegularFile() && targetPath.fileSize() > 0)
                }
            }
        }
    }

    // endregion

    // region Action 可见性

    @Test
    @Suppress("OverrideOnly")
    fun update_visibility_test() {
        val png = prepareSource("png", "update", "file.png")
        val jpg = prepareSource("jpg", "update", "file.jpg")
        val jpeg = prepareSource("jpg", "update", "file.jpeg")
        val gif = prepareSource("gif", "update", "file.gif")
        val dds = prepareSource("dds", "update", "file.dds")
        val tga = prepareSource("tga", "update", "file.tga")
        val txt = root.resolve("update/file.txt").also {
            it.parent.createDirectories()
            Files.writeString(it, "plain text")
        }

        val cases = listOf(
            // PNG：源文件已经是 PNG 时禁用
            Triple<AnAction, Path, Boolean>(ConvertImageFormatActions.Png(), png, false),
            Triple(ConvertImageFormatActions.Png(), jpg, true),
            Triple(ConvertImageFormatActions.Png(), jpeg, true),
            Triple(ConvertImageFormatActions.Png(), gif, true),
            Triple(ConvertImageFormatActions.Png(), dds, true),
            Triple(ConvertImageFormatActions.Png(), tga, true),
            Triple(ConvertImageFormatActions.Png(), txt, false),
            // JPG：`.jpg` 和 `.jpeg` 均禁用
            Triple(ConvertImageFormatActions.Jpg(), png, true),
            Triple(ConvertImageFormatActions.Jpg(), jpg, false),
            Triple(ConvertImageFormatActions.Jpg(), jpeg, false),
            Triple(ConvertImageFormatActions.Jpg(), gif, true),
            Triple(ConvertImageFormatActions.Jpg(), dds, true),
            Triple(ConvertImageFormatActions.Jpg(), tga, true),
            Triple(ConvertImageFormatActions.Jpg(), txt, false),
            // GIF：源文件已经是 GIF 时禁用
            Triple(ConvertImageFormatActions.Gif(), gif, false),
            Triple(ConvertImageFormatActions.Gif(), png, true),
            Triple(ConvertImageFormatActions.Gif(), txt, false),
            // DDS：源文件已经是 DDS 时禁用
            Triple(ConvertImageFormatActions.Dds(), dds, false),
            Triple(ConvertImageFormatActions.Dds(), png, true),
            Triple(ConvertImageFormatActions.Dds(), txt, false),
            // TGA：源文件已经是 TGA 时禁用
            Triple(ConvertImageFormatActions.Tga(), tga, false),
            Triple(ConvertImageFormatActions.Tga(), png, true),
            Triple(ConvertImageFormatActions.Tga(), txt, false),
        )
        for ((action, path, expected) in cases) {
            val event = eventFor(action, listOf(path))
            action.update(event)
            Assert.assertEquals(
                "Unexpected visibility for ${action.javaClass.simpleName} on ${path.fileName}",
                expected,
                event.presentation.isEnabledAndVisible,
            )
        }
    }

    // endregion

    // region 端到端 Action 执行

    @Test
    fun cancel_prompt_test() {
        val source = prepareSource("png", "cancel", "source.png")
        val targetDirectoryPath = tempDir("cancel-target")
        val action = TestConvertImageFormatAction("PNG")

        val event = eventFor(action, listOf(source))
        action.actionPerformed(event)
        waitForJob(action.lastConversionJob)

        Assert.assertTrue("Prompt should be invoked", action.promptCalled)
        Assert.assertFalse("No file should be created", Files.list(targetDirectoryPath).use { it.findAny().isPresent })
        Assert.assertNull("No failure notification should be sent", action.failedFiles)
        Assert.assertTrue("No error dialog should be shown", action.errorMessages.isEmpty())
        Assert.assertTrue("No overwrite prompt should be shown", action.overwritePrompts.isEmpty())
    }

    @Test
    fun single_file_action_end_to_end_test() {
        val source = prepareSource("png", "single", "mod_unknown.png")
        val targetDirectoryPath = tempDir("single-target")
        val targetDirectory = psiDirectory(targetDirectoryPath)
        val action = TestConvertImageFormatAction("PNG")
        action.configureOptions("renamed.png", targetDirectory)

        val event = eventFor(action, listOf(source))
        action.actionPerformed(event)
        waitForJob(action.lastConversionJob)

        val targetPath = targetDirectoryPath.resolve("renamed.png")
        assertValidImage(targetPath, source)
        Assert.assertEquals("renamed.png", psiFile(targetPath).name)
        Assert.assertTrue("Prompt should be invoked", action.promptCalled)
        Assert.assertEquals("mod_unknown.png", action.lastDefaultNewFileName)
        Assert.assertNull(action.failedFiles)
    }

    @Test
    fun batch_partial_failure_test() {
        val batchDirectory = tempDir("batch")
        val good = prepareSource("png", "batch", "good.png")
        val bad = batchDirectory.resolve("bad.png").also { Files.writeString(it, "not an image") }
        val targetDirectoryPath = tempDir("batch-target")
        val targetDirectory = psiDirectory(targetDirectoryPath)
        val action = TestConvertImageFormatAction("JPG")
        action.configureOptions(null, targetDirectory)

        val event = eventFor(action, listOf(good, bad))
        action.actionPerformed(event)
        waitForJob(action.lastConversionJob)

        assertValidImage(targetDirectoryPath.resolve("good.jpg"), good)
        Assert.assertFalse("Broken image should not be converted", Files.exists(targetDirectoryPath.resolve("bad.jpg")))
        Assert.assertTrue("Prompt should be invoked", action.promptCalled)
        Assert.assertNull("Batch conversion has no default file name", action.lastDefaultNewFileName)
        Assert.assertNotNull("Failure notification should be sent", action.failedFiles)
        Assert.assertEquals(1, action.failedFiles!!.size)
        Assert.assertEquals("bad.png", action.failedFiles!!.first().name)
    }

    // endregion

    // region 目标文件冲突处理

    @Test
    fun overwrite_skip_test() {
        val source = prepareSource("png", "conflict-skip", "source.png")
        val targetDirectoryPath = tempDir("conflict-skip-target")
        val existingTarget = targetDirectoryPath.resolve("target.png").also { writeImage(it, 11, 13, Color.GREEN.rgb) }
        val existingBytes = Files.readAllBytes(existingTarget)
        val action = TestConvertImageFormatAction("PNG")
        action.configureOptions("target.png", psiDirectory(targetDirectoryPath))
        action.configureOverwriteChoice { _, _ -> SkipOverwriteChoice.SKIP }

        val sourceFile = psiFile(source)
        val result = runSuspending { action.convertSuspend(listOf(sourceFile), project) }

        Assert.assertEquals(0, result.added.size)
        Assert.assertEquals(0, result.failed.size)
        Assert.assertArrayEquals("Skipped target must not be overwritten", existingBytes, Files.readAllBytes(existingTarget))
        Assert.assertEquals(1, action.overwritePrompts.size)
        Assert.assertFalse("Single-file conflict should not be treated as batch", action.overwritePrompts.first().second)
    }

    @Test
    fun overwrite_skip_all_test() {
        val sourceA = prepareSource("png", "conflict-skip-all", "a.png")
        val sourceB = prepareSource("png", "conflict-skip-all", "b.png")
        val targetDirectoryPath = tempDir("conflict-skip-all-target")
        val targetA = targetDirectoryPath.resolve("a.png").also { writeImage(it, 11, 13, Color.GREEN.rgb) }
        val targetB = targetDirectoryPath.resolve("b.png").also { writeImage(it, 17, 19, Color.BLUE.rgb) }
        val bytesA = Files.readAllBytes(targetA)
        val bytesB = Files.readAllBytes(targetB)
        val action = TestConvertImageFormatAction("PNG")
        action.configureOptions(null, psiDirectory(targetDirectoryPath))
        action.configureOverwriteChoice { _, _ -> SkipOverwriteChoice.SKIP_ALL }

        val sourceFileA = psiFile(sourceA)
        val sourceFileB = psiFile(sourceB)
        val result = runSuspending {
            action.convertSuspend(listOf(sourceFileA, sourceFileB), project)
        }

        Assert.assertEquals(0, result.added.size)
        Assert.assertArrayEquals(bytesA, Files.readAllBytes(targetA))
        Assert.assertArrayEquals(bytesB, Files.readAllBytes(targetB))
        Assert.assertEquals("SKIP_ALL should only prompt once", 1, action.overwritePrompts.size)
        Assert.assertTrue("Batch conflict should be treated as batch", action.overwritePrompts.first().second)
    }

    @Test
    fun overwrite_overwrite_test() {
        val source = prepareSource("png", "conflict-overwrite", "source.png")
        val targetDirectoryPath = tempDir("conflict-overwrite-target")
        val existingTarget = targetDirectoryPath.resolve("target.png").also { writeImage(it, 11, 13, Color.GREEN.rgb) }
        val action = TestConvertImageFormatAction("PNG")
        action.configureOptions("target.png", psiDirectory(targetDirectoryPath))
        action.configureOverwriteChoice { _, _ -> SkipOverwriteChoice.OVERWRITE }

        val sourceFile = psiFile(source)
        val result = runSuspending { action.convertSuspend(listOf(sourceFile), project) }

        Assert.assertEquals(1, result.added.size)
        Assert.assertArrayEquals("Overwritten target should contain the converted image", Files.readAllBytes(source), Files.readAllBytes(existingTarget))
        Assert.assertEquals(1, action.overwritePrompts.size)
    }

    @Test
    fun overwrite_overwrite_all_test() {
        val sourceA = prepareSource("png", "conflict-overwrite-all", "a.png")
        val sourceB = prepareSource("png", "conflict-overwrite-all", "b.png")
        val targetDirectoryPath = tempDir("conflict-overwrite-all-target")
        val targetA = targetDirectoryPath.resolve("a.png").also { writeImage(it, 11, 13, Color.GREEN.rgb) }
        val targetB = targetDirectoryPath.resolve("b.png").also { writeImage(it, 17, 19, Color.BLUE.rgb) }
        val action = TestConvertImageFormatAction("PNG")
        action.configureOptions(null, psiDirectory(targetDirectoryPath))
        action.configureOverwriteChoice { _, _ -> SkipOverwriteChoice.OVERWRITE_ALL }

        val sourceFileA = psiFile(sourceA)
        val sourceFileB = psiFile(sourceB)
        val result = runSuspending {
            action.convertSuspend(listOf(sourceFileA, sourceFileB), project)
        }

        Assert.assertEquals(2, result.added.size)
        Assert.assertArrayEquals(Files.readAllBytes(sourceA), Files.readAllBytes(targetA))
        Assert.assertArrayEquals(Files.readAllBytes(sourceB), Files.readAllBytes(targetB))
        Assert.assertEquals("OVERWRITE_ALL should only prompt once", 1, action.overwritePrompts.size)
    }

    @Test
    fun overwrite_same_path_test() {
        val directory = tempDir("self-overwrite")
        val source = prepareSource("png", "self-overwrite", "self.png")
        val originalBytes = Files.readAllBytes(source)
        val action = TestConvertImageFormatAction("PNG")
        action.configureOptions("self.png", psiDirectory(directory))
        action.configureOverwriteChoice { _, _ -> SkipOverwriteChoice.OVERWRITE }

        val sourceFile = psiFile(source)
        val result = runSuspending { action.convertSuspend(listOf(sourceFile), project) }

        Assert.assertEquals(1, result.added.size)
        Assert.assertEquals(0, result.failed.size)
        Assert.assertArrayEquals("Same-path conversion must not corrupt the file", originalBytes, Files.readAllBytes(source))
        assertValidImage(source, source)
        Assert.assertEquals(1, action.overwritePrompts.size)
    }

    // endregion

    // region 并发与锁安全

    @Test
    fun conversion_runs_outside_edt_and_locks_test() {
        val support = RecordingImageSupport()
        ExtensionTestUtil.maskExtensions(ImageSupport.EP_NAME, listOf(support), testRootDisposable)

        val source = prepareSource("png", "locks", "source.png")
        val targetDirectoryPath = tempDir("locks-target")
        val action = TestConvertImageFormatAction("JPG")
        action.configureOptions("target.jpg", psiDirectory(targetDirectoryPath))

        val sourceFile = psiFile(source)
        val result = runSuspending { action.convertSuspend(listOf(sourceFile), project) }

        Assert.assertTrue("ImageSupport.convertImageFormat must be invoked", support.called)
        Assert.assertFalse("Conversion must not run in EDT", support.inDispatchThread)
        Assert.assertFalse("Conversion must not hold read lock", support.inReadAction)
        Assert.assertFalse("Conversion must not hold write lock", support.inWriteAction)
        Assert.assertEquals(0, result.failed.size)
        Assert.assertEquals(1, result.added.size)
        Assert.assertTrue(Files.isRegularFile(targetDirectoryPath.resolve("target.jpg")))
    }

    // endregion

    // region 对话框校验

    @Test
    fun dialog_basic_validation_test() {
        val source = prepareSource("png", "dialog", "source.png")
        val file = psiFile(source)
        var dialog: ConvertImageFormatDialog? = null
        ApplicationManager.getApplication().invokeAndWait {
            val newDialog = ConvertImageFormatDialog(listOf(file), project, "default.png", "PNG")
            dialog = newDialog
            newDialog.createCenterPanel()
        }

        val actual = requireNotNull(dialog)
        Assert.assertEquals("default.png", actual.newFileName)
        Assert.assertEquals(file.parent, actual.targetDirectory)
    }

    // endregion

    // region 辅助方法

    private fun <T> runSuspending(block: suspend () -> T): T {
        val future = ApplicationManager.getApplication().executeOnPooledThread(Callable { runBlocking { block() } })
        return PlatformTestUtil.waitForFuture(future)
    }

    private fun waitForJob(job: Job?) {
        val actualJob = requireNotNull(job) { "Conversion job is missing" }
        val future = ApplicationManager.getApplication().executeOnPooledThread(Callable { runBlocking { actualJob.join() } })
        PlatformTestUtil.waitForFuture(future)
        Assert.assertTrue("Conversion job should be completed", actualJob.isCompleted)
    }

    private fun eventFor(action: AnAction, paths: List<Path>): AnActionEvent {
        val virtualFiles = paths.map {
            refreshAndFindFile(it) ?: error("Cannot find file: $it")
        }.toTypedArray()
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, virtualFiles)
            .build()
        return TestActionEvent.createTestEvent(action, dataContext)
    }

    private fun prepareSource(format: String, directoryName: String = "sources", fileName: String = "mod_unknown.$format"): Path {
        val source = Path.of("src", "test", "testData", "images", "mod_unknown.$format").toAbsolutePath().normalize()
        val target = root.resolve(directoryName).resolve(fileName).toAbsolutePath().normalize()
        target.parent.createDirectories()
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
        return target
    }

    private fun tempDir(name: String): Path {
        return root.resolve(name).toAbsolutePath().normalize().also { it.createDirectories() }
    }

    private fun writeImage(path: Path, width: Int, height: Int, rgb: Int) {
        path.parent.createDirectories()
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.color = Color(rgb)
            graphics.fillRect(0, 0, width, height)
        } finally {
            graphics.dispose()
        }
        ImageIO.write(image, "png", path.toFile())
    }

    private fun assertValidImage(target: Path, source: Path) {
        Assert.assertTrue("Missing converted image: $target", target.isRegularFile())
        Assert.assertTrue("Empty converted image: $target", target.fileSize() > 0)
        val sourceImage = requireNotNull(ImageIO.read(source.toFile())) { "Cannot read source image: $source" }
        val targetImage = requireNotNull(ImageIO.read(target.toFile())) { "Cannot read target image: $target" }
        Assert.assertEquals("Unexpected width: $target", sourceImage.width, targetImage.width)
        Assert.assertEquals("Unexpected height: $target", sourceImage.height, targetImage.height)
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

    // endregion

    // region 测试用实现

    /** 直接调用 [ConvertImageFormatAction.convertImageFormat] 的临时实现，用于策略矩阵测试。 */
    private class TestTransientConvertImageFormatAction(targetFormatName: String) : ConvertImageFormatAction(targetFormatName)

    /** 可覆写交互钩子的测试实现，用于端到端 Action 测试。 */
    private class TestConvertImageFormatAction(targetFormatName: String) : ConvertImageFormatAction(targetFormatName) {
        private var options: ConversionOptions? = null
        private var overwriteChoiceProvider: (fileName: String, isBatch: Boolean) -> SkipOverwriteChoice = { _, _ -> SkipOverwriteChoice.OVERWRITE }

        var failedFiles: List<PsiFile>? = null
        val errorMessages = mutableListOf<String?>()
        val overwritePrompts = mutableListOf<Pair<String, Boolean>>()
        var promptCalled = false
        var lastDefaultNewFileName: String? = null

        fun configureOptions(newFileName: String?, targetDirectory: PsiDirectory) {
            options = ConversionOptions(newFileName, targetDirectory)
        }

        fun configureOverwriteChoice(provider: (fileName: String, isBatch: Boolean) -> SkipOverwriteChoice) {
            overwriteChoiceProvider = provider
        }

        override fun promptConversionOptions(files: List<PsiFile>, project: Project, defaultNewFileName: String?): ConversionOptions? {
            promptCalled = true
            lastDefaultNewFileName = defaultNewFileName
            return options
        }

        override suspend fun promptOverwriteChoice(
            targetDirectory: PsiDirectory,
            fileName: String,
            title: String,
            isBatch: Boolean,
        ): SkipOverwriteChoice {
            overwritePrompts.add(fileName to isBatch)
            return overwriteChoiceProvider(fileName, isBatch)
        }

        override suspend fun notifyFailed(project: Project, files: List<PsiFile>, failed: List<PsiFile>) {
            failedFiles = failed.toList()
        }

        override suspend fun showErrorDialog(project: Project, message: String?) {
            errorMessages.add(message)
        }
    }

    /** 记录调用时线程与锁状态的图片支持实现。 */
    private class RecordingImageSupport : ImageSupport {
        @Volatile
        var called = false

        @Volatile
        var inDispatchThread = true

        @Volatile
        var inReadAction = true

        @Volatile
        var inWriteAction = true

        override suspend fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
            return false
        }

        override suspend fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
            val application = ApplicationManager.getApplication()
            called = true
            inDispatchThread = application.isDispatchThread
            inReadAction = application.isReadAccessAllowed
            inWriteAction = application.isWriteAccessAllowed
            Assert.assertFalse("Conversion must not run in EDT", inDispatchThread)
            Assert.assertFalse("Conversion must not hold read lock", inReadAction)
            Assert.assertFalse("Conversion must not hold write lock", inWriteAction)
            Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING)
            return true
        }
    }

    // endregion
}
