package icu.windea.pls.config.util

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.findChild
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxGameType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtMemberConfigInlinedRecursiveVisitorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private fun prepareCases(): Pair<CwtFile, CwtConfigGroup> {
        myFixture.configureByFile("features/config/inlined_visitor_cases.test.cwt")
        val file = myFixture.file as CwtFile
        val configGroup = CwtConfigGroupImpl(project, ParadoxGameType.Stellaris)

        // 解析文件配置并注册别名到配置组
        val filePath = "common/test/inlined_visitor_cases.cwt"
        val fileConfig = CwtFileConfig.resolve(file, configGroup, filePath)

        // 手动注册 single_alias 和 alias 到初始化器，然后加入规则分组数据
        val initializer = configGroup.initializer
        fileConfig.properties.forEach { prop ->
            val singleAliasConfig = CwtSingleAliasConfig.resolve(prop)
            if (singleAliasConfig != null) {
                initializer.singleAliases[singleAliasConfig.name] = singleAliasConfig
            }
            val aliasConfig = CwtAliasConfig.resolve(prop)
            if (aliasConfig != null) {
                val subtype = aliasConfig.subName
                val aliasGroup = initializer.aliasGroups.getOrPut(aliasConfig.name) { FastMap() }
                val configs = aliasGroup.getOrPut(subtype) { FastList() }
                configs.add(aliasConfig)
            }
        }
        initializer.copyUserDataTo(configGroup)

        return file to configGroup
    }

    private fun label(config: CwtMemberConfig<*>): String {
        return when (config) {
            is CwtPropertyConfig -> "P:${config.key}"
            is CwtValueConfig -> "V:${config.value}"
        }
    }

    @Test
    fun testInlineSingleAlias_expandsToDefinedConfig() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "prop_with_sa" }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        val visited = mutableListOf<String>()
        val visitor = object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited += "P:${config.key}"
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                visited += "V:${config.value}"
                return super.visitValue(config)
            }
        }

        assertTrue(config.accept(visitor))
        // 应访问：原属性 -> 内联的 single_alias 内容 (simple_key = int)
        assertTrue(visited.contains("P:prop_with_sa"))
        assertTrue(visited.contains("P:simple_key"))
    }

    @Test
    fun testInlineNestedSingleAlias_recursiveExpansion() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "prop_with_nested_sa" }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        val visited = mutableListOf<String>()
        val visitor = object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited += "P:${config.key}"
                return super.visitProperty(config)
            }
        }

        assertTrue(config.accept(visitor))
        // 应访问：prop_with_nested_sa -> nested (from sa_nested) -> simple_key (from sa_simple)
        assertTrue(visited.contains("P:prop_with_nested_sa"))
        assertTrue(visited.contains("P:nested"))
        assertTrue(visited.contains("P:simple_key"))
    }

    @Test
    fun testInlineAlias_expandsAliasGroup() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name.startsWith("alias_name") }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        val visited = mutableListOf<String>()
        val visitor = object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited += "P:${config.key}"
                return super.visitProperty(config)
            }
        }

        assertTrue(config.accept(visitor))
        // 应访问原属性和别名组中定义的所有配置
        assertTrue(visited.contains("P:alias_name[test_alias]"))
        // 别名组中有 basic, with_block, recursive 三个子类型，应该都被访问
        assertTrue(visited.any { it.contains("alias_prop") || it.contains("block_key") })
    }

    @Test
    fun testForSingleAliasFlag_controlsInlining() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "prop_with_sa" }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        // forSingleAlias = false 时，不应展开 single_alias
        val visited1 = mutableListOf<String>()
        val visitor1 = object : CwtMemberConfigInlinedRecursiveVisitor(forSingleAlias = false) {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited1 += "P:${config.key}"
                return super.visitProperty(config)
            }
        }

        assertTrue(config.accept(visitor1))
        // 应只访问原属性，不展开 single_alias
        assertTrue(visited1.contains("P:prop_with_sa"))
        assertFalse(visited1.contains("P:simple_key"))

        // forSingleAlias = true（默认）时，应展开
        val visited2 = mutableListOf<String>()
        val visitor2 = object : CwtMemberConfigInlinedRecursiveVisitor(forSingleAlias = true) {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited2 += "P:${config.key}"
                return super.visitProperty(config)
            }
        }

        assertTrue(config.accept(visitor2))
        assertTrue(visited2.contains("P:simple_key"))
    }

    @Test
    fun testForAliasFlag_controlsInlining() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name.startsWith("alias_name") }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        // forAlias = false 时，不应展开 alias
        val visited1 = mutableListOf<String>()
        val visitor1 = object : CwtMemberConfigInlinedRecursiveVisitor(forAlias = false) {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited1 += "P:${config.key}"
                return super.visitProperty(config)
            }
        }

        assertTrue(config.accept(visitor1))
        // 应只访问原属性，不展开 alias
        assertTrue(visited1.contains("P:alias_name[test_alias]"))
        assertFalse(visited1.any { it.contains("alias_prop") || it.contains("block_key") })

        // forAlias = true（默认）时，应展开
        val visited2 = mutableListOf<String>()
        val visitor2 = object : CwtMemberConfigInlinedRecursiveVisitor(forAlias = true) {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited2 += "P:${config.key}"
                return super.visitProperty(config)
            }
        }

        assertTrue(config.accept(visitor2))
        assertTrue(visited2.any { it.contains("alias_prop") || it.contains("block_key") })
    }

    @Test
    fun testMixedBlock_inlinesAllReferences() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "mixed_block" }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        val visited = mutableListOf<String>()
        val visitor = object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited += "P:${config.key}"
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                visited += "V:${config.value}"
                return super.visitValue(config)
            }
        }

        assertTrue(config.accept(visitor))
        // 应访问：mixed_block 本身、直接子节点、以及内联的配置
        assertTrue(visited.contains("P:mixed_block"))
        assertTrue(visited.contains("P:sa_ref"))
        assertTrue(visited.contains("P:simple_key")) // 从 sa_simple 内联
        assertTrue(visited.contains("P:normal_key"))
        assertTrue(visited.any { it.startsWith("P:alias_name") })
    }

    @Test
    fun testShortCircuit_stopsOnFalse() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "mixed_block" }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        val visited = mutableListOf<String>()
        var stopKey = "sa_ref"

        val visitor = object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                visited += "P:${config.key}"
                if (config.key == stopKey) return false
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                visited += "V:${config.value}"
                return super.visitValue(config)
            }
        }

        assertFalse(config.accept(visitor))
        // 遍历到 sa_ref 时停止，不应继续访问后续节点
        assertTrue(visited.contains("P:sa_ref"))
        // 停止后不应访问内联的配置或后续的兄弟节点
        assertFalse(visited.contains("P:simple_key"))
    }

    @Test
    fun testVisitFinished_calledAfterChildren() {
        val (file, group) = prepareCases()
        val p = file.block!!.findChild<CwtProperty> { it.name == "prop_with_sa" }!!
        val config = CwtPropertyConfig.resolve(p, file, group)!!

        val started = mutableListOf<String>()
        val finished = mutableListOf<String>()

        val visitor = object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                started += "P:${config.key}"
                return super.visitProperty(config)
            }

            override fun visitFinished(config: CwtMemberConfig<*>): Boolean {
                finished += label(config)
                return true
            }
        }

        assertTrue(config.accept(visitor))
        // 验证 finished 在子节点之后调用
        assertTrue(started.isNotEmpty())
        assertTrue(finished.isNotEmpty())
        // simple_key 应在 prop_with_sa 之前完成
        val simpleKeyFinishedIdx = finished.indexOfFirst { it == "P:simple_key" }
        val propFinishedIdx = finished.indexOfFirst { it == "P:prop_with_sa" }
        assertTrue(simpleKeyFinishedIdx >= 0)
        assertTrue(propFinishedIdx >= 0)
        assertTrue(simpleKeyFinishedIdx < propFinishedIdx)
    }
}
