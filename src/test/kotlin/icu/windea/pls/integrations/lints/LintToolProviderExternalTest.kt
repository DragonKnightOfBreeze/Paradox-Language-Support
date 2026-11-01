package icu.windea.pls.integrations.lints

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ChronicleIntegrationSettings
import icu.windea.pls.integrations.ExternalToolDownloadDescriptor
import icu.windea.pls.integrations.ExternalToolDownloadTestService
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleAssume
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * 外部检查工具提供者的轻量测试。
 *
 * 仅验证 `LintToolProvider` 的可用性和路径校验，不验证 inspection 的端到端行为。
 *
 * @see TigerLintToolProvider
 * @see ExternalToolDownloadTestService
 */
@RunWith(JUnit4::class)
class LintToolProviderExternalTest : BasePlatformTestCase() {
    private val lintState get() = ChronicleIntegrationSettings.getInstance().state.lint

    private var originalEnableTiger = false
    private var originalCk3TigerPath: String? = ""
    private var originalCk3TigerConfPath: String? = ""
    private var originalIrTigerPath: String? = ""
    private var originalIrTigerConfPath: String? = ""
    private var originalVic3TigerPath: String? = ""
    private var originalVic3TigerConfPath: String? = ""

    @Before
    fun doSetUp() {
        ChronicleAssume.includeExternalTools()
        originalEnableTiger = lintState.enableTiger
        originalCk3TigerPath = lintState.ck3TigerPath
        originalCk3TigerConfPath = lintState.ck3TigerConfPath
        originalIrTigerPath = lintState.irTigerPath
        originalIrTigerConfPath = lintState.irTigerConfPath
        originalVic3TigerPath = lintState.vic3TigerPath
        originalVic3TigerConfPath = lintState.vic3TigerConfPath
        lintState.enableTiger = true
    }

    @After
    fun doTearDown() {
        lintState.enableTiger = originalEnableTiger
        lintState.ck3TigerPath = originalCk3TigerPath
        lintState.ck3TigerConfPath = originalCk3TigerConfPath
        lintState.irTigerPath = originalIrTigerPath
        lintState.irTigerConfPath = originalIrTigerConfPath
        lintState.vic3TigerPath = originalVic3TigerPath
        lintState.vic3TigerConfPath = originalVic3TigerConfPath
    }

    @Test
    fun ck3TigerToolProvider_test() {
        assertTigerProvider(
            provider = TigerLintToolProvider.Ck3(),
            descriptor = ExternalToolDownloadTestService.ck3Tiger,
            gameType = ParadoxGameType.Ck3,
            setPath = { lintState.ck3TigerPath = it },
            setConfPath = { lintState.ck3TigerConfPath = it },
        )
    }

    @Test
    fun irTigerToolProvider_test() {
        assertTigerProvider(
            provider = TigerLintToolProvider.Ir(),
            descriptor = ExternalToolDownloadTestService.irTiger,
            gameType = ParadoxGameType.Ir,
            setPath = { lintState.irTigerPath = it },
            setConfPath = { lintState.irTigerConfPath = it },
        )
    }

    @Test
    fun vic3TigerToolProvider_test() {
        assertTigerProvider(
            provider = TigerLintToolProvider.Vic3(),
            descriptor = ExternalToolDownloadTestService.vic3Tiger,
            gameType = ParadoxGameType.Vic3,
            setPath = { lintState.vic3TigerPath = it },
            setConfPath = { lintState.vic3TigerConfPath = it },
        )
    }

    private fun assertTigerProvider(
        provider: TigerLintToolProvider,
        descriptor: ExternalToolDownloadDescriptor?,
        gameType: ParadoxGameType,
        setPath: (String) -> Unit,
        setConfPath: (String) -> Unit,
    ) {
        Assume.assumeTrue("${provider.name} is not supported on this OS", descriptor != null)
        val executablePath = ExternalToolDownloadTestService.ensureTool(descriptor!!)
        setPath(executablePath.absolutePathString())

        Assert.assertTrue(provider.isEnabled())
        Assert.assertTrue(provider.isSupported(gameType))
        Assert.assertTrue(provider.isAvailable(gameType))
        Assert.assertEquals(executablePath.toAbsolutePath(), provider.getExecutablePath()?.toAbsolutePath())
        Assert.assertTrue(provider.validateExecutablePath(executablePath))
        Assert.assertFalse(provider.validateExecutablePath(Path.of("missing-${provider.name}")))

        val dir = Path.of("build", "tmp", "lint-tool-provider-test").also { it.createDirectories() }
        val confPath = dir.resolve("${provider.name}.conf").also { it.writeText("") }
        setConfPath(confPath.absolutePathString())
        Assert.assertEquals(confPath.toAbsolutePath(), provider.getConfPath()?.toAbsolutePath())
        Assert.assertTrue(provider.validateConfPath(confPath))

        val invalidConfPath = dir.resolve("${provider.name}.txt").also { it.writeText("") }
        setConfPath(invalidConfPath.absolutePathString())
        Assert.assertFalse(provider.validateConfPath(invalidConfPath))
    }
}
