package icu.windea.pls.lang.util

import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
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

    private fun configureScriptFile(relPath: String, @TestDataFile testDataPath: String): ParadoxScriptFile {
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

        Assert.assertEquals("mech", info.type)
        Assert.assertEquals(listOf("heavy"), info.subtypes)
        Assert.assertEquals(listOf("mech", "heavy"), info.types)
        Assert.assertEquals("mech, heavy", info.typeText)
    }

    @Test
    fun testSubtypes_ReplaceMode_FromSelfDeclaration() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // REPLACE:phantom - 使用 replace_mode，从自身声明检测子类型
        // 自身声明中有 cloaking = yes，匹配 stealth 子类型
        val replaceProperty = selectScope { injectFile.properties().ofKey("REPLACE:phantom").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(replaceProperty)!!

        Assert.assertTrue(ParadoxDefinitionInjectionManager.isReplaceMode(info))
        Assert.assertEquals("mech", info.type)
        Assert.assertEquals(listOf("stealth"), info.subtypes)
        Assert.assertEquals(listOf("mech", "stealth"), info.types)
        Assert.assertEquals("mech, stealth", info.typeText)
    }

    @Test
    fun testSubtypes_ReplaceOrCreateMode_FromSelfDeclaration() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // REPLACE_OR_CREATE:new_mech - 使用 replace_mode，从自身声明检测子类型
        // 自身声明中没有 cloaking = yes 也没有 weight_class = heavy，所以没有子类型
        val replaceOrCreateProperty = selectScope { injectFile.properties().ofKey("REPLACE_OR_CREATE:new_mech").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(replaceOrCreateProperty)!!

        Assert.assertTrue(ParadoxDefinitionInjectionManager.isReplaceMode(info))
        Assert.assertEquals("mech", info.type)
        Assert.assertEquals(emptyList<String>(), info.subtypes)  // 没有匹配的子类型
        Assert.assertEquals(listOf("mech"), info.types)
        Assert.assertEquals("mech", info.typeText)
    }

    @Test
    fun testSubtypes_InjectMode_NotSelfSubtypeMode() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // INJECT:titan_mk3 - 不是 replace_mode，从目标定义获取子类型
        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(injectProperty)!!

        Assert.assertFalse(ParadoxDefinitionInjectionManager.isReplaceMode(info))
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

        // REPLACE_OR_CREATE 是 relax mode
        val replaceOrCreateProperty = selectScope { injectFile.properties().ofKey("REPLACE_OR_CREATE:new_mech").one() } as ParadoxScriptProperty
        val replaceOrCreateInfo = ParadoxDefinitionInjectionManager.getInfo(replaceOrCreateProperty)!!
        Assert.assertTrue(replaceOrCreateInfo.isRelaxMode())
    }

    // endregion

    // region Declaration

    @Test
    fun testDeclaration_InjectMode_InheritsFromTarget() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // INJECT:titan_mk3 - 应继承目标定义的声明（含 heavy 子类型字段）
        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(injectProperty)!!

        val declaration = info.declaration
        Assert.assertNotNull(declaration)
        declaration!!

        val key = CwtConfigManipulator.getIdentifierKey(declaration, "\u0000", -1)
        // 应包含基础字段
        Assert.assertTrue(key.contains("armor"))
        Assert.assertTrue(key.contains("speed"))
        // 应包含 heavy 子类型字段（从目标定义继承）
        Assert.assertTrue(key.contains("shield"))
        Assert.assertTrue(key.contains("weight_class"))
        // 不应包含 stealth 子类型字段
        Assert.assertFalse(key.contains("cloaking"))
    }

    @Test
    fun testDeclaration_ReplaceMode_InheritsFromTarget() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // REPLACE:phantom - 应继承目标定义的声明（含 stealth 子类型字段）
        val replaceProperty = selectScope { injectFile.properties().ofKey("REPLACE:phantom").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(replaceProperty)!!

        val declaration = info.declaration
        Assert.assertNotNull(declaration)
        declaration!!

        val key = CwtConfigManipulator.getIdentifierKey(declaration, "\u0000", -1)
        // 应包含基础字段
        Assert.assertTrue(key.contains("armor"))
        Assert.assertTrue(key.contains("speed"))
        // 应包含 stealth 子类型字段（从目标定义继承）
        Assert.assertTrue(key.contains("cloaking"))
        // 不应包含 heavy 子类型字段
        Assert.assertFalse(key.contains("shield"))
        Assert.assertFalse(key.contains("weight_class"))
    }

    @Test
    fun testDeclaration_MatchesTargetStructure() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val baseFile = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        // 获取目标定义的声明
        val titan = selectScope { baseFile.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!
        val titanKey = CwtConfigManipulator.getIdentifierKey(titanInfo.declaration!!, "\u0000", -1)

        // 获取注入的声明
        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty
        val injectInfo = ParadoxDefinitionInjectionManager.getInfo(injectProperty)!!
        val injectKey = CwtConfigManipulator.getIdentifierKey(injectInfo.declaration!!, "\u0000", -1)

        // 注入的声明结构应与目标定义的声明结构一致
        Assert.assertEquals(titanKey, injectKey)
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
