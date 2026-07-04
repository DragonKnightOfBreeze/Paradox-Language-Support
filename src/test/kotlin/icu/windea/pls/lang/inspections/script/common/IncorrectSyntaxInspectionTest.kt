package icu.windea.pls.lang.inspections.script.common

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.settings.ChronicleConfigSettings
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.HighlightingTestScope
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.createRootInfo
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see IncorrectSyntaxInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class IncorrectSyntaxInspectionTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/inspections")
        markConfigDirectory("features/inspections/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris, ParadoxGameType.Eu5)
        myFixture.enableInspections(IncorrectSyntaxInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun comparisonOperator() {
        // 比较运算符，值不可表示数值 - 应触发警告
        // 比较运算符，值可表示数值 - 不应触发警告
        // 非比较运算符 - 不应触发警告

        val tag = ChronicleEpBundle.message("incorrectSyntax.comparison.desc.1").toWarningTag()

        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            key_bool_ge ${tag.start}>=${tag.end} yes
            key_bool_ne ${tag.start}!=${tag.end} no
            key_bool_ne ${tag.start}!=${tag.end} no
            key_bool_ne ${tag.start}!=${tag.end} rgb { 142 188 241 }

            key_int_gt > 5
            key_float_ge >= 3.14
            key_block_le < @[ 1 + 1]
            key_str_lt < some_text
            key_block_gt > { }
            key_block_le <= { }

            key_eq_block = { }
            key_eq_bool = yes
            key_eq_int = 5
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun comparisonOperator_semantic() {
        // 匹配的规则使用 = 分隔符，比较运算符不被允许 - 应触发警告
        // 匹配的规则使用 == 分隔符，比较运算符被允许 - 不应触发警告

        val tag = ChronicleEpBundle.message("incorrectSyntax.comparison.desc.2").toWarningTag()

        val settings = ChronicleConfigSettings.getInstance().state.features
        settings.checkComparisonOperators = true
        try {
            markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test.txt")
            myFixture.configureByText("test.txt", """
                test_entity_1 = {
                    value_with_equal = 5
                    value_with_equal ${tag.start}!=${tag.end} 5
                    value_with_double_equal = 5
                    value_with_double_equal != 5
                }
        """.trimIndent())
            myFixture.checkHighlighting()
        } finally {
            settings.checkComparisonOperators = false
        }
    }

    @Test
    fun safeAssignOperator() {
        markFileInfo(createRootInfo(ParadoxGameType.Core), "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            owner ?= {}
            owner? = {}
            1 <warning descr="Unexpected safe assign operator (`?=`) (key of containing property should be a string literal)">?=</warning> {}
            1<warning descr="Unexpected safe call assign operator (`? =`) (key of containing property should be a string literal)">? =</warning> {}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun safeAssignOperator_eu5() {
        markFileInfo(createRootInfo(ParadoxGameType.Eu5), "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            owner <warning descr="Unexpected safe assign operator (`?=`) (key of containing property should be a scope link)">?=</warning> {}
            owner<warning descr="Safe call assign operators (`? =`) are not supported in Europa Universalis V">? =</warning> {}
            1 <warning descr="Unexpected safe assign operator (`?=`) (key of containing property should be a string literal)">?=</warning> {}
            1<warning descr="Safe call assign operators (`? =`) are not supported in Europa Universalis V">? =</warning> {}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun safeAssignOperator_stellaris() {
        markFileInfo(createRootInfo(ParadoxGameType.Stellaris, "4.4.4444"), "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            owner <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
            owner<warning descr="Unexpected safe call assign operator (`? =`) (key of containing property should be a scope link)">? =</warning> {}
            1 <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
            1<warning descr="Unexpected safe call assign operator (`? =`) (key of containing property should be a string literal)">? =</warning> {}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun safeAssignOperator_stellaris_old() {
        markFileInfo(createRootInfo(ParadoxGameType.Stellaris, "3.1415"), "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            owner <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
            owner<warning descr="Safe call assign operators (`? =`) are not supported in Stellaris (game version < 4.4)">? =</warning> {}
            1 <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
            1<warning descr="Safe call assign operators (`? =`) are not supported in Stellaris (game version < 4.4)">? =</warning> {}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun safeAssignOperator_semantic() {
        markFileInfo(createRootInfo(ParadoxGameType.Core), "common/scripted_triggers/test.txt")
        myFixture.configureByText("test.txt", """
            scripted_trigger_1 = {
                owner ?= {}
                owner? = {}
                1 <warning descr="Unexpected safe assign operator (`?=`) (key of containing property should be a string literal)">?=</warning> {}
                1<warning descr="Unexpected safe call assign operator (`? =`) (key of containing property should be a string literal)">? =</warning> {}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun safeAssignOperator_eu5_semantic() {
        markFileInfo(createRootInfo(ParadoxGameType.Eu5), "common/scripted_triggers/test.txt")
        myFixture.configureByText("test.txt", """
            scripted_trigger_1 = {
                owner ?= {}
                owner<warning descr="Safe call assign operators (`? =`) are not supported in Europa Universalis V">? =</warning> {}
                1 <warning descr="Unexpected safe assign operator (`?=`) (key of containing property should be a string literal)">?=</warning> {}
                1<warning descr="Safe call assign operators (`? =`) are not supported in Europa Universalis V">? =</warning> {}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun safeAssignOperator_stellaris_semantic() {
        markFileInfo(createRootInfo(ParadoxGameType.Stellaris, "4.4.4444"), "common/scripted_triggers/test.txt")
        myFixture.configureByText("test.txt", """
            scripted_trigger_1 = {
                owner <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
                owner? = {}
                1 <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
                1<warning descr="Unexpected safe call assign operator (`? =`) (key of containing property should be a string literal)">? =</warning> {}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    @Test
    fun safeAssignOperator_stellaris_old_semantic() {
        markFileInfo(createRootInfo(ParadoxGameType.Stellaris, "3.1415"), "common/scripted_triggers/test.txt")
        myFixture.configureByText("test.txt", """
            scripted_trigger_1 = {
                owner <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
                owner<warning descr="Safe call assign operators (`? =`) are not supported in Stellaris (game version < 4.4)">? =</warning> {}
                1 <warning descr="Safe assign operators (`?=`) are not supported in Stellaris">?=</warning> {}
                1<warning descr="Safe call assign operators (`? =`) are not supported in Stellaris (game version < 4.4)">? =</warning> {}
            }
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}
