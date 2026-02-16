package icu.windea.pls.lang.util

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefinitionInjectionManagerTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/resolve")
        markConfigDirectory("features/resolve/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun clear() = clearIntegrationTest()

    private fun configureScriptFile(relPath: String, testDataPath: String): ParadoxScriptFile {
        markFileInfo(ParadoxGameType.Stellaris, relPath)
        myFixture.configureByFile(testDataPath)
        return myFixture.file as ParadoxScriptFile
    }

    // region Basic Injection Info

    @Test
    fun testGetInfo_InjectMode() {
        // 先配置目标定义文件，使定义存在
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // INJECT:titan_mk3
        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() }
        Assert.assertNotNull(injectProperty)
        injectProperty as ParadoxScriptProperty

        val info = ParadoxDefinitionInjectionManager.getInfo(injectProperty)
        Assert.assertNotNull(info)
        info!!

        Assert.assertEquals("INJECT", info.mode)
        Assert.assertEquals("titan_mk3", info.target)
        Assert.assertEquals("mech", info.type)
        Assert.assertEquals(ParadoxGameType.Stellaris, info.gameType)
        Assert.assertEquals("INJECT:titan_mk3", info.expression)
    }

    @Test
    fun testGetInfo_ReplaceMode() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // REPLACE:phantom
        val replaceProperty = selectScope { injectFile.properties().ofKey("REPLACE:phantom").one() }
        Assert.assertNotNull(replaceProperty)
        replaceProperty as ParadoxScriptProperty

        val info = ParadoxDefinitionInjectionManager.getInfo(replaceProperty)
        Assert.assertNotNull(info)
        info!!

        Assert.assertEquals("REPLACE", info.mode)
        Assert.assertEquals("phantom", info.target)
        Assert.assertEquals("mech", info.type)
    }

    // endregion

    // region Injection Subtypes

    @Test
    fun testSubtypes_FromTargetDefinition() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // INJECT:titan_mk3 - 目标定义 titan_mk3 有子类型 "heavy"
        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(injectProperty)!!

        val subtypes = info.subtypes
        Assert.assertEquals(listOf("heavy"), subtypes)
        Assert.assertEquals("mech.heavy", info.typeText)
    }

    @Test
    fun testSubtypes_ReplaceMode_FromTargetDefinition() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // REPLACE:phantom - 目标定义 phantom 有子类型 "stealth"
        val replaceProperty = selectScope { injectFile.properties().ofKey("REPLACE:phantom").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(replaceProperty)!!

        val subtypes = info.subtypes
        Assert.assertEquals(listOf("stealth"), subtypes)
    }

    // endregion

    // region Relax Mode

    @Test
    fun testIsRelaxMode() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // INJECT 不是 relax mode
        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty
        val injectInfo = ParadoxDefinitionInjectionManager.getInfo(injectProperty)!!
        Assert.assertFalse(injectInfo.isRelaxMode())

        // REPLACE 不是 relax mode
        val replaceProperty = selectScope { injectFile.properties().ofKey("REPLACE:phantom").one() } as ParadoxScriptProperty
        val replaceInfo = ParadoxDefinitionInjectionManager.getInfo(replaceProperty)!!
        Assert.assertFalse(replaceInfo.isRelaxMode())
    }

    // endregion

    // region Support Check

    @Test
    fun testIsSupported() {
        // 我们在测试配置中定义了 directive[definition_injection]，所以 Stellaris 应被支持
        Assert.assertTrue(ParadoxDefinitionInjectionManager.isSupported(ParadoxGameType.Stellaris))
    }

    // endregion
}
