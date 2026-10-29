package icu.windea.pls.test.issues

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleIntegrationsSettings
import icu.windea.pls.base.settings.ChronicleProfilesSettings
import icu.windea.pls.base.settings.ParadoxModDescriptorSettingsState
import icu.windea.pls.base.settings.ParadoxModSettingsState
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.ep.analysis.ParadoxDescriptorModBasedModMetadataProvider
import icu.windea.pls.integrations.ExternalToolDownloadTestService
import icu.windea.pls.integrations.lints.TigerLintToolService
import icu.windea.pls.lang.analysis.ParadoxAnalysisDataManager
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.inspections.lints.ChronicleTigerLintInspection
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.tools.SpecialPathService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxRootMetadata
import icu.windea.pls.test.ChronicleAssume
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

/**
 * Tiger 检查逻辑和 `ChronicleTigerLintInspection` 的进阶测试。
 *
 * 测试使用测试数据目录中的简单 CK3 模组：
 * - `descriptor.mod` 提供模组描述符。
 * - `common/scripted_triggers/test_triggers.txt` 包含一个有效触发器和一个无效字段。
 *
 * 测试需要：
 * - 已下载可用的 `ck3-tiger`（由 `ExternalToolDownloadTestService` 管理）。
 * - 本地存在 CK3 游戏目录（Tiger 需要游戏目录来解析基础游戏定义）。
 *
 * 测试流程 A：
 * 1. 构造真实的 `ParadoxRootInfo.Mod` 并注入根目录和文件信息。
 * 2. 直接调用 `TigerLintToolService.collectRequest` 和 `getTigerLintResult`，验证请求和结果解析。
 * 3. 启用 `ChronicleTigerLintInspection`，通过 `doHighlighting` 验证外部注解器的端到端结果。
 *
 * 测试流程 B：
 * 1. 构造真实的 `ParadoxRootInfo.Mod` 并注入根目录和文件信息。
 * 2. 直接启用 `ChronicleTigerLintInspection`，通过 `doHighlighting` 验证外部注解器的端到端结果。
 *
 * 如果本地没有游戏目录或未启用外部工具测试，则跳过。
 *
 * @see TigerLintToolService
 * @see ChronicleTigerLintInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue396TigerLintTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    private var originalEnableTiger = false
    private var originalTigerPath: String? = null
    private var originalModSettings: ParadoxModSettingsState? = null
    private var originalModDescriptorSettings: ParadoxModDescriptorSettingsState? = null
    private var modRootPath: String = ""
    private lateinit var modRootInfo: ParadoxRootInfo.Mod

    @Before
    fun doSetUp() {
        ChronicleAssume.includeExternalTools()
        val descriptor = ExternalToolDownloadTestService.ck3Tiger ?: error("ck3-tiger descriptor is missing")
        val tigerPath = ExternalToolDownloadTestService.ensureTool(descriptor)

        val gameDir = findCk3GameDirectory()
        Assume.assumeTrue("CK3 game directory is not available", gameDir != null)

        markIntegrationTest()
        addAdditionalAllowedRoots(Path.of("src", "test", "testData"))

        val settings = ChronicleIntegrationsSettings.getInstance().state.lint
        originalEnableTiger = settings.enableTiger
        originalTigerPath = settings.ck3TigerPath
        settings.enableTiger = true
        settings.ck3TigerPath = tigerPath.toString()

        val rootFile = findModRootFile()
        modRootPath = rootFile.path
        val metadata = ParadoxDescriptorModBasedModMetadataProvider().getRootMetadata(rootFile.toNioPath())
            ?: error("Cannot parse mod descriptor: ${rootFile.path}")

        val profilesSettings = ChronicleProfilesSettings.getInstance().state
        originalModDescriptorSettings = profilesSettings.modDescriptorSettings[modRootPath]
        profilesSettings.modDescriptorSettings[modRootPath] = ParadoxModDescriptorSettingsState().apply {
            gameType = ParadoxGameType.Ck3
            modDirectory = modRootPath
        }
        originalModSettings = profilesSettings.modSettings[modRootPath]
        profilesSettings.modSettings[modRootPath] = ParadoxModSettingsState().apply {
            gameType = ParadoxGameType.Ck3
            gameDirectory = gameDir.toString()
            modDirectory = modRootPath
        }

        val rootInfo = ParadoxRootInfo.Mod(rootFile, metadata as ParadoxRootMetadata.Mod)
        modRootInfo = rootInfo
        markFileInfo(rootInfo, "common/scripted_triggers/test_triggers.txt")
    }

    @After
    fun doTearDown() {
        if (modRootPath.isEmpty()) return
        val settings = ChronicleIntegrationsSettings.getInstance().state.lint
        settings.enableTiger = originalEnableTiger
        settings.ck3TigerPath = originalTigerPath
        val profilesSettings = ChronicleProfilesSettings.getInstance().state
        if (originalModDescriptorSettings == null) {
            profilesSettings.modDescriptorSettings.remove(modRootPath)
        } else {
            profilesSettings.modDescriptorSettings[modRootPath] = originalModDescriptorSettings!!
        }
        if (originalModSettings == null) {
            profilesSettings.modSettings.remove(modRootPath)
        } else {
            profilesSettings.modSettings[modRootPath] = originalModSettings!!
        }
        clearIntegrationTest()
    }

    @Test
    fun tigerLintResultAndInspection_test() {
        configureTestCase()

        val service = TigerLintToolService.getInstance()
        val file = myFixture.file
        val request = service.collectRequest(file)
        val fileInfo = file.virtualFile?.fileInfo
        Assert.assertNotNull("Cannot collect Tiger lint request (language=${file.language}, fileInfo=$fileInfo)", request)
        val result = service.getTigerLintResult(request!!)
        Assert.assertNotNull("Tiger lint result is null", result)
        Assert.assertNull("Tiger lint failed: ${result!!.error?.message}", result.error)

        val fileResult = result.fromPath("common/scripted_triggers/test_triggers.txt")
        Assert.assertNotNull("Tiger did not report the test file. Items: ${result.itemGroup.keys}", fileResult)
        Assert.assertTrue("Tiger did not report unknown-field", fileResult!!.items.any { it.key == "unknown-field" })

        myFixture.enableInspections(ChronicleTigerLintInspection())
        val highlights = myFixture.doHighlighting()
        Assert.assertTrue(
            "Tiger external annotation was not applied: ${highlights.map { it.description }}",
            highlights.any { it.description?.contains("unknown-field") == true },
        )
    }

    @Test
    fun tigerLintOnlyInspection_test() {
        configureTestCase()

        myFixture.enableInspections(ChronicleTigerLintInspection())
        val highlights = myFixture.doHighlighting()
        Assert.assertTrue(
            "Tiger external annotation was not applied: ${highlights.map { it.description }}",
            highlights.any { it.description?.contains("unknown-field") == true },
        )
    }

    private fun configureTestCase() {
        myFixture.configureByFile("issues/396/tiger-mod/common/scripted_triggers/test_triggers.txt")
        val file = myFixture.file
        val path = "common/scripted_triggers/test_triggers.txt"
        ParadoxAnalysisDataManager.clearData(file.virtualFile, ParadoxAnalysisDataManager.Keys.cachedFileInfo)
        ParadoxAnalysisDataManager.clearData(file.originalFile.virtualFile, ParadoxAnalysisDataManager.Keys.cachedFileInfo)
        ParadoxAnalysisInjectionManager.markFileInfo(modRootInfo, path)
        ParadoxAnalysisInjectionManager.injectFileInfo(file.virtualFile, modRootInfo, path)
        ParadoxAnalysisInjectionManager.injectFileInfo(file.originalFile.virtualFile, modRootInfo, path)
        selectFile(file)?.let { ParadoxAnalysisInjectionManager.injectFileInfo(it, modRootInfo, path) }
    }

    private fun findCk3GameDirectory(): Path? {
        // `SpecialPathService.getGameDataPath` 应当就已经足够，但是……
        // OK 大肥鱼，你是懂“防御性编程”的，就这样吧……

        SpecialPathService.getInstance().getGameDataPath(ParadoxGameType.Ck3)
            ?.takeIf { it.isDirectory() }
            ?.let { return it }
        System.getProperty("chronicle.test.tiger.gameDir")?.toPathOrNull()
            ?.takeIf { it.isDirectory() }
            ?.let { return it }

        val candidates = listOf(
            "D:\\Program Files\\Steam\\steamapps\\common\\Crusader Kings III",
            "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Crusader Kings III",
            "C:\\Program Files\\Steam\\steamapps\\common\\Crusader Kings III",
        )
        return candidates.asSequence().map(Path::of).firstOrNull { Files.isDirectory(it) }
    }

    private fun findModRootFile(): VirtualFile {
        val path = Path.of("src", "test", "testData", "issues", "396", "tiger-mod").toAbsolutePath().normalize()
        return LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: error("Cannot find mod root: $path")
    }
}
