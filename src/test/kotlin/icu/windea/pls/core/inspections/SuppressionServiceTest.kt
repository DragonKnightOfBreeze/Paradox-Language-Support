package icu.windea.pls.core.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see SuppressionService
 */
@RunWith(JUnit4::class)
class SuppressionServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region getCommentsForSuppression

    @Test
    fun getCommentsForSuppression_forFile() {
        // 文件顶层的注释会被收集（兼容 `PsiRootBlock`），顺序从前到后
        myFixture.configureByText("test.cwt", """
            # comment 1
            # comment 2
            key = value
        """.trimIndent())
        val file = myFixture.file
        val comments = SuppressionService.getCommentsForSuppression(file).toList()
        assertEquals(2, comments.size)
        assertEquals("comment 1", comments[0].commentText)
        assertEquals("comment 2", comments[1].commentText)
    }

    @Test
    fun getCommentsForSuppression_forFile_blankLineDoesNotSeparate() {
        // 与 `PsiService.getAttachedComments` 不同，空白行（仍属于空白）不会中断收集
        myFixture.configureByText("test.cwt", """
            # comment

            key = value
        """.trimIndent())
        val file = myFixture.file
        val comments = SuppressionService.getCommentsForSuppression(file).toList()
        assertEquals(1, comments.size)
        assertEquals("comment", comments[0].commentText)
    }

    @Test
    fun getCommentsForSuppression_forProperty() {
        // 紧邻属性的注释会被收集，顺序从近到远
        myFixture.configureByText("test.cwt", """
            # comment 1
            # comment 2
            key = <caret>value
        """.trimIndent())
        val property = findPropertyAtCaret()
        val comments = SuppressionService.getCommentsForSuppression(property).toList()
        assertEquals(2, comments.size)
        assertEquals("comment 2", comments[0].commentText)
        assertEquals("comment 1", comments[1].commentText)
    }

    @Test
    fun getCommentsForSuppression_forProperty_blankLineDoesNotSeparate() {
        // 与 `PsiService.getAttachedComments` 不同，空白行（仍属于空白）不会中断收集
        myFixture.configureByText("test.cwt", """
            # comment

            key = <caret>value
        """.trimIndent())
        val property = findPropertyAtCaret()
        val comments = SuppressionService.getCommentsForSuppression(property).toList()
        assertEquals(1, comments.size)
        assertEquals("comment", comments[0].commentText)
    }

    @Test
    fun getCommentsForSuppression_forProperty_stopsAtPreviousProperty() {
        // 遇到上一个非注释、非空白的元素（如属性）时停止
        myFixture.configureByText("test.cwt", """
            key_a = value_a
            # comment
            key_b = <caret>value_b
        """.trimIndent())
        val property = findPropertyAtCaret()
        val comments = SuppressionService.getCommentsForSuppression(property).toList()
        assertEquals(1, comments.size)
        assertEquals("comment", comments[0].commentText)
    }

    // endregion

    // region isSuppressedInComment

    @Test
    fun isSuppressedInComment_forFile() {
        myFixture.configureByText("test.cwt", """
            # noinspection ParadoxScriptIncorrectSyntax
            key = value
        """.trimIndent())
        val file = myFixture.file
        assertTrue(SuppressionService.isSuppressedInComment(file, "ParadoxScriptIncorrectSyntax"))
        assertFalse(SuppressionService.isSuppressedInComment(file, "ParadoxScriptUnresolvedExpression"))
    }

    @Test
    fun isSuppressedInComment_forFile_multipleTools() {
        myFixture.configureByText("test.cwt", """
            # noinspection ToolA, ToolB
            key = value
        """.trimIndent())
        val file = myFixture.file
        assertTrue(SuppressionService.isSuppressedInComment(file, "ToolA"))
        assertTrue(SuppressionService.isSuppressedInComment(file, "ToolB"))
        assertFalse(SuppressionService.isSuppressedInComment(file, "ToolC"))
    }

    @Test
    fun isSuppressedInComment_forFile_regularComment() {
        myFixture.configureByText("test.cwt", """
            # just a regular comment
            key = value
        """.trimIndent())
        val file = myFixture.file
        assertFalse(SuppressionService.isSuppressedInComment(file, "SomeToolId"))
    }

    @Test
    fun isSuppressedInComment_forProperty() {
        myFixture.configureByText("test.cwt", """
            # noinspection ParadoxScriptIncorrectSyntax
            key = <caret>value
        """.trimIndent())
        val property = findPropertyAtCaret()
        assertTrue(SuppressionService.isSuppressedInComment(property, "ParadoxScriptIncorrectSyntax"))
        assertFalse(SuppressionService.isSuppressedInComment(property, "ParadoxScriptUnresolvedExpression"))
    }

    @Test
    fun isSuppressedInComment_forProperty_multipleTools() {
        myFixture.configureByText("test.cwt", """
            # noinspection ToolA, ToolB
            key = <caret>value
        """.trimIndent())
        val property = findPropertyAtCaret()
        assertTrue(SuppressionService.isSuppressedInComment(property, "ToolA"))
        assertTrue(SuppressionService.isSuppressedInComment(property, "ToolB"))
        assertFalse(SuppressionService.isSuppressedInComment(property, "ToolC"))
    }

    @Test
    fun isSuppressedInComment_forProperty_regularComment() {
        myFixture.configureByText("test.cwt", """
            # just a regular comment
            key = <caret>value
        """.trimIndent())
        val property = findPropertyAtCaret()
        assertFalse(SuppressionService.isSuppressedInComment(property, "SomeToolId"))
    }

    // endregion

    private val PsiElement.commentText get() = text.trimStart('#').trim()

    private fun findPropertyAtCaret(): CwtProperty {
        return myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
    }
}
