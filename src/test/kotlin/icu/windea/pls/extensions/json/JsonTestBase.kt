@file:Suppress("UnstableApiUsage", "JUnitMixedFramework")

package icu.windea.pls.extensions.json

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxExecutableFileBasedGameMetadata
import icu.windea.pls.model.analysis.ParadoxMetadataJsonBasedModMetadata
import icu.windea.pls.model.analysis.ParadoxMetadataJsonInfo
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import java.nio.file.Path

/**
 * JSON 扩展测试的公共基类。
 *
 * 提供创建注入文件信息所需的游戏/模组根信息，并统一管理测试作用域状态。
 */
abstract class JsonTestBase : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setUpJson() {
        markIntegrationTest()
        markRootDirectory("chronicle")
    }

    @After
    fun tearDownJson() {
        clearIntegrationTest()
    }

    protected fun createJsonFile(name: String, text: String = "{}"): VirtualFile {
        return myFixture.addFileToProject(name, text).virtualFile
    }

    protected fun createGameRootInfo(): ParadoxRootInfo.Game {
        val rootFile = myFixture.tempDirFixture.findOrCreateDir("game")
        val rootPath = Path.of("game")
        val metadata = ParadoxExecutableFileBasedGameMetadata(
            gameType = ParadoxGameType.Stellaris,
            rootPath = rootPath,
            executablePath = rootPath.resolve("stellaris.exe"),
            branchPath = null,
        )
        return ParadoxRootInfo.Game(rootFile, metadata)
    }

    protected fun createModRootInfo(): ParadoxRootInfo.Mod {
        val rootFile = myFixture.tempDirFixture.findOrCreateDir("mod")
        val rootPath = Path.of("mod")
        val info = ParadoxMetadataJsonInfo(
            name = "Test Mod",
            id = "test.mod",
            gameId = ParadoxGameType.Vic3.gameId,
        )
        val metadata = ParadoxMetadataJsonBasedModMetadata(
            rootPath = rootPath,
            infoPath = rootPath.resolve(".metadata/metadata.json"),
            info = info,
        )
        return ParadoxRootInfo.Mod(rootFile, metadata)
    }
}
