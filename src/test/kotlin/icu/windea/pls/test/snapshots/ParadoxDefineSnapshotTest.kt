package icu.windea.pls.test.snapshots

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.removeSuffixOrNull
import icu.windea.pls.core.toPath
import icu.windea.pls.lang.analysis.ParadoxGameTypeManager
import icu.windea.pls.lang.inspections.PlsInspectionUtil
import icu.windea.pls.lang.inspections.script.common.ConflictingResolvedExpressionInspection
import icu.windea.pls.lang.tools.SpecialPathService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

/**
 * TODO 2.1.8+ 不太可行：存在路径引用和动态引用。
 */
@Ignore
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefineSnapshotTest : BasePlatformTestCase() {
    private val logger = thisLogger()
    private val definesRelPath = "common/defines"

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("snapshots")
        myFixture.enableInspections(ConflictingResolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Suppress("SameParameterValue")
    private fun checkFilePaths(gameType: ParadoxGameType) {
        val rootPath = SpecialPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        Assume.assumeTrue("Root path for ${gameType.title}: (not found)", rootPath != null && rootPath.isDirectory())
        rootPath!!
        println("Root path for ${gameType.title}: ${rootPath}")

        val filePathMap = mutableMapOf<String, Path>()
        ParadoxGameTypeManager.processGamePath(gameType, rootPath, definesRelPath) { path, _ ->
            path.walk().filter { it.extension == "txt" }.forEach { p ->
                val key = getScriptFileKey(p, rootPath)
                filePathMap[key] = p
            }
            true
        }
        Assume.assumeTrue("Define file paths for ${gameType.title}: (empty)", filePathMap.isNotEmpty())
        logger.info("Define file paths for ${gameType.title}: ${filePathMap}")

        val configFilePathMap = mutableMapOf<String, Path>()
        val configRootDirectories = CwtConfigManager.getBuiltInConfigRootDirectories(myFixture.project)
        configRootDirectories.forEach { configRootDirectory ->
            val configRootPath = configRootDirectory.toNioPath()
            val definesConfigPath = configRootPath.resolve(definesRelPath)
            definesConfigPath.walk().filter { it.extension == "cwt" }.forEach { p ->
                val key = getConfigFileKey(p, configRootPath)
                configFilePathMap[key] = p
            }
        }
        assertTrue("Define config file paths for ${gameType.title}: (empty)", configFilePathMap.isNotEmpty())
        logger.info("Define config file paths for ${gameType.title}: ${configFilePathMap}")

        // actual can have additional elements (e.g., global config files)
        val expect = filePathMap.keys.mapNotNull { it.removeSuffixOrNull(".txt") }.toSet()
        val actual = filePathMap.keys.mapNotNull { it.removeSuffixOrNull(".cwt") }.filter { it in expect }.toSet()

        assertSameElements("Mismatched define file paths and define config file paths", expect, actual)
    }

    @Suppress("SameParameterValue")
    private fun checkScriptFiles(gameType: ParadoxGameType) {
        val rootPath = SpecialPathService.getInstance().getSteamGamePath(gameType.steamId, gameType.title)
        Assume.assumeTrue("Root path for ${gameType.title}: (not found)", rootPath != null && rootPath.isDirectory())
        rootPath!!

        val filePathMap = mutableMapOf<String, Path>()
        ParadoxGameTypeManager.processGamePath(gameType, rootPath, definesRelPath) { path, _ ->
            path.walk().filter { it.extension == "txt" }.forEach { p ->
                val key = getScriptFileKey(p, rootPath)
                filePathMap[key] = p
            }
            true
        }
        Assume.assumeTrue("Define file paths for ${gameType.title}: (empty)", filePathMap.isNotEmpty())

        myFixture.enableInspections(*PlsInspectionUtil.getExpressionInspectionTypesForScriptFiles())

        myFixture.testDataPath = rootPath.toString()
        val sourceAndTargetPaths = mutableMapOf<String, String>()
        for ((key, filePath) in filePathMap) {
            val sourcePath = filePath.relativeTo(rootPath).toString()
            val targetPath = key
            myFixture.copyFileToProject(sourcePath, targetPath)
            sourceAndTargetPaths[sourcePath] = targetPath
            logger.info("Copy file (source -> target): $sourcePath -> $targetPath")
        }
        for ((sourcePath, targetPath) in sourceAndTargetPaths) {
            myFixture.checkResultByFile(targetPath, sourcePath, true)
        }
    }

    private fun getScriptFileKey(path: Path, rootPath: Path): String {
        val relPathToRoot = path.relativeTo(rootPath).toString().normalizePath()
        val entry = relPathToRoot.substringBefore(definesRelPath, "").trim('/')
        val relPath = relPathToRoot.substringAfter(definesRelPath, "").trim('/')
        return definesRelPath.toPath().resolve(entry).resolve(relPath).toString().normalizePath()
    }

    private fun getConfigFileKey(path: Path, rootPath: Path): String {
        val relPathToRoot = path.relativeTo(rootPath).toString().normalizePath()
        return relPathToRoot
    }

    @Test
    fun checkFilePaths_stellaris() {
        checkFilePaths(ParadoxGameType.Stellaris)
    }

    @Test
    fun checkScriptFiles_stellaris() {
        checkScriptFiles(ParadoxGameType.Stellaris)
    }
}
