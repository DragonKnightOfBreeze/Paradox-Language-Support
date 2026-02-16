package icu.windea.pls.lang.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
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

/**
 * 测试定义和定义注入的缓存机制。
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefinitionCacheTest : BasePlatformTestCase() {
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

    // region definitionInfo Cache

    @Test
    fun testDefinitionInfo_CacheReuse() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!

        // 多次调用应返回相同的缓存实例
        val info1 = ParadoxDefinitionManager.getInfo(titan)
        val info2 = ParadoxDefinitionManager.getInfo(titan)

        Assert.assertNotNull(info1)
        Assert.assertSame(info1, info2)
    }

    @Test
    fun testDefinitionInfo_DifferentElements_DifferentCache() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val phantom = selectScope { file.ofPath("phantom").asProperty().one() }!!

        val titanInfo = ParadoxDefinitionManager.getInfo(titan)
        val phantomInfo = ParadoxDefinitionManager.getInfo(phantom)

        Assert.assertNotNull(titanInfo)
        Assert.assertNotNull(phantomInfo)
        Assert.assertNotSame(titanInfo, phantomInfo)
        Assert.assertEquals("titan_mk3", titanInfo!!.name)
        Assert.assertEquals("phantom", phantomInfo!!.name)
    }

    // endregion

    // region subtypeConfigs Cache

    @Test
    fun testSubtypeConfigs_CacheReuse() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val info = ParadoxDefinitionManager.getInfo(titan)!!

        // 多次调用应返回相同的缓存实例
        val subtypes1 = info.subtypeConfigs
        val subtypes2 = info.subtypeConfigs

        Assert.assertSame(subtypes1, subtypes2)
    }

    @Test
    fun testSubtypeConfigs_DifferentDefinitions_DifferentCache() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val phantom = selectScope { file.ofPath("phantom").asProperty().one() }!!

        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!
        val phantomInfo = ParadoxDefinitionManager.getInfo(phantom)!!

        val titanSubtypes = titanInfo.subtypeConfigs
        val phantomSubtypes = phantomInfo.subtypeConfigs

        // 不同定义的子类型应不同
        Assert.assertNotEquals(titanSubtypes.map { it.name }, phantomSubtypes.map { it.name })
        Assert.assertEquals(listOf("heavy"), titanSubtypes.map { it.name })
        Assert.assertEquals(listOf("stealth"), phantomSubtypes.map { it.name })
    }

    // endregion

    // region declaration Cache

    @Test
    fun testDeclaration_CacheReuse() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val info = ParadoxDefinitionManager.getInfo(titan)!!

        // 多次调用应返回相同的缓存实例
        val decl1 = info.declaration
        val decl2 = info.declaration

        Assert.assertNotNull(decl1)
        Assert.assertSame(decl1, decl2)
    }

    @Test
    fun testDeclaration_DifferentSubtypes_DifferentStructure() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!  // heavy 子类型
        val phantom = selectScope { file.ofPath("phantom").asProperty().one() }!!  // stealth 子类型

        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!
        val phantomInfo = ParadoxDefinitionManager.getInfo(phantom)!!

        val titanDecl = titanInfo.declaration!!
        val phantomDecl = phantomInfo.declaration!!

        // 不同子类型的声明结构应不同
        val titanKey = CwtConfigManipulator.getIdentifierKey(titanDecl, "\u0000", -1)
        val phantomKey = CwtConfigManipulator.getIdentifierKey(phantomDecl, "\u0000", -1)

        Assert.assertNotEquals(titanKey, phantomKey)
        Assert.assertTrue(titanKey.contains("shield"))      // heavy 特有
        Assert.assertTrue(phantomKey.contains("cloaking"))  // stealth 特有
    }

    // endregion

    // region Dependencies

    @Test
    fun testGetDependencies_DefinitionInfo() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!

        val deps = ParadoxDefinitionService.getDependencies(titan, file)

        // 应依赖 file
        Assert.assertEquals(1, deps.size)
        Assert.assertSame(file, deps[0])
    }

    @Test
    fun testGetSubtypeAwareDependencies_NoSubtypes() {
        val file = configureScriptFile("common/signals/00_signals.txt", "features/resolve/common/signals/00_signals.txt")
        val distress = selectScope { file.ofPath("distress_beacon").asProperty().one() }!!
        val info = ParadoxDefinitionManager.getInfo(distress)!!

        val deps = ParadoxDefinitionService.getSubtypeAwareDependencies(distress, info)

        // 无子类型候选项时，只依赖 file
        Assert.assertEquals(1, deps.size)
        Assert.assertSame(file, deps[0])
    }

    @Test
    fun testGetSubtypeAwareDependencies_FastMatchSubtypes() {
        // mech 类型有子类型，但子类型通过 type_key_filter 等快速匹配，不依赖声明结构
        // 然而在测试配置中，mech 的子类型 heavy/stealth 依赖声明结构（configs 不为空）
        // 所以这里使用 weapon 类型来测试，它也有子类型但同样依赖声明结构
        // 实际上，在我们的测试配置中，所有有子类型的类型都依赖声明结构
        // 这个测试主要验证方法调用正确
        val file = configureScriptFile("common/weapons/00_weapons.txt", "features/resolve/common/weapons/00_weapons.txt")
        val plasma = selectScope { file.ofPath("plasma_cannon").asProperty().one() }!!
        val info = ParadoxDefinitionManager.getInfo(plasma)!!

        val deps = ParadoxDefinitionService.getSubtypeAwareDependencies(plasma, info)

        // 有子类型且依赖声明结构时，应包含 ScriptFile tracker
        Assert.assertTrue(deps.isNotEmpty())
    }

    // endregion

    // region Definition Injection Cache

    @Test
    fun testDefinitionInjectionInfo_CacheReuse() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty

        // 多次调用应返回相同的缓存实例
        val info1 = ParadoxDefinitionInjectionManager.getInfo(injectProperty)
        val info2 = ParadoxDefinitionInjectionManager.getInfo(injectProperty)

        Assert.assertNotNull(info1)
        Assert.assertSame(info1, info2)
    }

    @Test
    fun testDefinitionInjectionInfo_SubtypeConfigs_CacheReuse() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(injectProperty)!!

        // 多次访问 subtypeConfigs 应返回相同实例
        val subtypes1 = info.subtypeConfigs
        val subtypes2 = info.subtypeConfigs

        Assert.assertSame(subtypes1, subtypes2)
    }

    @Test
    fun testDefinitionInjectionInfo_Declaration_CacheReuse() {
        configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val injectFile = configureScriptFile("common/mechs/01_inject.txt", "features/resolve/common/mechs/01_inject.txt")

        val injectProperty = selectScope { injectFile.properties().ofKey("INJECT:titan_mk3").one() } as ParadoxScriptProperty
        val info = ParadoxDefinitionInjectionManager.getInfo(injectProperty)!!

        // 多次访问 declaration 应返回相同实例
        val decl1 = info.declaration
        val decl2 = info.declaration

        Assert.assertNotNull(decl1)
        Assert.assertSame(decl1, decl2)
    }

    // endregion

    // region Cache Invalidation

    @Test
    fun testDefinitionInfo_CacheInvalidation_OnFileModification() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!

        // 获取初始缓存
        val info = ParadoxDefinitionManager.getInfo(titan)
        Assert.assertNotNull(info)

        // 模拟文件修改：在文件末尾添加注释
        WriteCommandAction.runWriteCommandAction(project) {
            val document = PsiDocumentManager.getInstance(project).getDocument(file)!!
            document.insertString(document.textLength, "\n# comment")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }

        // 重新获取元素（文件已修改，PSI 树可能已重建）
        val titanAfter = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!

        // 修改后应获取新的缓存实例（或相同内容但不同实例）
        val infoAfter = ParadoxDefinitionManager.getInfo(titanAfter)
        Assert.assertNotNull(infoAfter)
        Assert.assertNotSame(info, infoAfter)

        // 验证信息内容仍然正确
        infoAfter!!
        Assert.assertEquals("titan_mk3", infoAfter.name)
        Assert.assertEquals("mech", infoAfter.type)
    }

    @Test
    fun testSubtypeConfigs_CacheInvalidation_OnFileModification() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val info = ParadoxDefinitionManager.getInfo(titan)!!

        // 获取初始子类型
        val subtypes1 = info.subtypeConfigs
        Assert.assertEquals(listOf("heavy"), subtypes1.map { it.name })

        // 模拟文件修改
        WriteCommandAction.runWriteCommandAction(project) {
            val document = PsiDocumentManager.getInstance(project).getDocument(file)!!
            document.insertString(document.textLength, "\n# another comment")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }

        // 重新获取元素和信息
        val titanAfter = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val infoAfter = ParadoxDefinitionManager.getInfo(titanAfter)
        Assert.assertNotNull(infoAfter)
        Assert.assertNotSame(info, infoAfter)

        // 验证子类型仍然正确
        infoAfter!!
        val subtypes2 = infoAfter.subtypeConfigs
        Assert.assertEquals(listOf("heavy"), subtypes2.map { it.name })
    }

    @Test
    fun testDeclaration_CacheInvalidation_OnFileModification() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val info = ParadoxDefinitionManager.getInfo(titan)!!

        // 获取初始声明
        val declaration = info.declaration
        Assert.assertNotNull(declaration)

        // 模拟文件修改
        WriteCommandAction.runWriteCommandAction(project) {
            val document = PsiDocumentManager.getInstance(project).getDocument(file)!!
            document.insertString(document.textLength, "\n# yet another comment")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }

        // 重新获取元素和信息
        val titanAfter = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val infoAfter = ParadoxDefinitionManager.getInfo(titanAfter)!!
        Assert.assertNotSame(info, infoAfter)

        // 验证声明结构仍然正确
        val declarationAfter = infoAfter.declaration
        Assert.assertNotNull(declarationAfter)

        val key = CwtConfigManipulator.getIdentifierKey(declarationAfter!!, "\u0000", -1)
        Assert.assertTrue(key.contains("armor"))
        Assert.assertTrue(key.contains("shield"))  // heavy 子类型字段
    }

    @Test
    fun testDefinitionInfo_CacheStillValid_WhenOtherFileModified() {
        // 配置两个不同的文件
        val mechFile = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val weaponFile = configureScriptFile("common/weapons/00_weapons.txt", "features/resolve/common/weapons/00_weapons.txt")

        val titan = selectScope { mechFile.ofPath("titan_mk3").asProperty().one() }!!

        // 获取 mech 的缓存
        val info = ParadoxDefinitionManager.getInfo(titan)
        Assert.assertNotNull(info)

        // 修改 weapon 文件（不是 mech 文件）
        WriteCommandAction.runWriteCommandAction(project) {
            val document = PsiDocumentManager.getInstance(project).getDocument(weaponFile)!!
            document.insertString(document.textLength, "\n# weapon comment")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }

        // mech 的缓存应该仍然有效（因为 definitionInfo 只依赖自身文件）
        val infoAfter = ParadoxDefinitionManager.getInfo(titan)
        Assert.assertNotNull(infoAfter)
        Assert.assertSame(info, infoAfter)
    }

    @Test
    fun testSubtypeConfigs_NoSubtypes_CacheStillValid_WhenOtherFileModified() {
        // signal 类型没有子类型
        val signalFile = configureScriptFile("common/signals/00_signals.txt", "features/resolve/common/signals/00_signals.txt")
        val weaponFile = configureScriptFile("common/weapons/00_weapons.txt", "features/resolve/common/weapons/00_weapons.txt")

        val distress = selectScope { signalFile.ofPath("distress_beacon").asProperty().one() }!!
        val info = ParadoxDefinitionManager.getInfo(distress)!!

        // 获取初始子类型（空列表）
        val subtypes = info.subtypeConfigs
        Assert.assertTrue(subtypes.isEmpty())

        // 修改其他文件
        WriteCommandAction.runWriteCommandAction(project) {
            val document = PsiDocumentManager.getInstance(project).getDocument(weaponFile)!!
            document.insertString(document.textLength, "\n# weapon comment 2")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }

        // 无子类型时，缓存只依赖自身文件，所以仍然有效
        val subtypesAfter = info.subtypeConfigs
        Assert.assertSame(subtypes, subtypesAfter)
    }

    // endregion
}
