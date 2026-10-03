package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * [ParadoxTemplateMatchService] 的语义级别匹配测试。
 *
 * 使用自行编写的规则文件和脚本文件（均位于 `features/match`）：
 * - `job` 类型用于验证 `<job>` 这类定义引用片段，要求存在实际的 job 定义（`solder`、`officer`）。
 * - `value[x]` 这类动态值片段仅要求形如标识符，不要求存在实际定义（即任意匹配）。
 *
 * @see ParadoxTemplateMatchService
 * @see CwtTemplateExpression
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxTemplateMatchServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/match")
        markConfigDirectory("features/match/.config")
        initInjectedConfigGroups(project, gameType) // only injected config files
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configureJobScriptFile(): ParadoxScriptFile {
        markFileInfo(gameType, "common/test_jobs/00_test_jobs.txt")
        myFixture.configureByFile("features/match/common/test_jobs/00_test_jobs.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        return myFixture.file as ParadoxScriptFile
    }

    private fun matches(input: String, template: String, element: PsiElement): Boolean {
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val templateExpression = CwtTemplateExpression.resolve(template)
        return ParadoxTemplateMatchService.matches(input, templateExpression, element, configGroup)
    }

    // region definition snippets

    @Test
    fun definitionSnippet_existingDefinition_matches() {
        val file = configureJobScriptFile()
        expectScope {
            matches("job_solder_add", "job_<job>_add", file).expectTrue()
            matches("job_officer_add", "job_<job>_add", file).expectTrue()
        }
    }

    @Test
    fun definitionSnippet_missingDefinition_notMatches() {
        val file = configureJobScriptFile()
        expectScope {
            matches("job_unknown_add", "job_<job>_add", file).expectFalse()
        }
    }

    @Test
    fun definitionSnippet_wrongFormat_notMatches() {
        val file = configureJobScriptFile()
        expectScope {
            // 缺少常量片段
            matches("job_solder", "job_<job>_add", file).expectFalse()
            matches("solder_add", "job_<job>_add", file).expectFalse()
            // 常量片段不匹配
            matches("job_solder_remove", "job_<job>_add", file).expectFalse()
        }
    }

    // endregion

    // region dynamic value snippets

    @Test
    fun valueSnippet_matchesAny() {
        val file = configureJobScriptFile()
        expectScope {
            // 动态值片段不要求存在实际定义（任意匹配）
            matches("any_thing_suf", "value[x]_suf", file).expectTrue()
            matches("other_suf", "value[x]_suf", file).expectTrue()
        }
    }

    @Test
    fun valueSnippet_constantMismatch_notMatches() {
        val file = configureJobScriptFile()
        expectScope {
            matches("any_thing_nope", "value[x]_suf", file).expectFalse()
        }
    }

    // endregion

    // region mixed snippets

    @Test
    fun mixedSnippets_matches() {
        val file = configureJobScriptFile()
        expectScope {
            // 同时包含定义引用片段和动态值片段
            matches("job_solder_active", "job_<job>_value[x]", file).expectTrue()
            matches("job_officer_active", "job_<job>_value[x]", file).expectTrue()
            // 定义引用片段未解析，整体不匹配
            matches("job_unknown_active", "job_<job>_value[x]", file).expectFalse()
        }
    }

    // endregion

    // region edge cases

    @Test
    fun invalidTemplate_notMatches() {
        val file = configureJobScriptFile()
        expectScope {
            // 单独一个常量片段不构成模板表达式
            matches("plain", "plain", file).expectFalse()
            // 单独一个动态片段不构成模板表达式
            matches("plain", "value[x]", file).expectFalse()
        }
    }

    // endregion
}
