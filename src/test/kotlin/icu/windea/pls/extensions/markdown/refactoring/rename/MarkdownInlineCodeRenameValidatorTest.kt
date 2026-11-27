@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.refactoring.rename

import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.api.RenameValidationResult
import icu.windea.pls.extensions.markdown.MarkdownTestBase
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownInlineCodeRenameValidator
 */
@RunWith(JUnit4::class)
class MarkdownInlineCodeRenameValidatorTest : MarkdownTestBase() {
    // region scripted variable

    @Test
    fun testScriptedVariable_validName() {
        val target = findScriptedVariable()

        assertEquals(RenameValidationResult.ok(), validator(target).validate("farewell"))
    }

    @Test
    fun testScriptedVariable_emptyName() {
        val target = findScriptedVariable()

        assertEquals(RenameValidationResult.invalid(), validator(target).validate(""))
    }

    @Test
    fun testScriptedVariable_nameWithBlank() {
        val target = findScriptedVariable()

        assertEquals(RenameValidationResult.invalid(), validator(target).validate("farewell name"))
    }

    @Test
    fun testScriptedVariable_nameWithUnsupportedChar() {
        val target = findScriptedVariable()

        assertEquals(RenameValidationResult.invalid(), validator(target).validate("farewell-name"))
    }

    // endregion

    // region localisation property

    @Test
    fun testLocalisationProperty_validName() {
        val target = findLocalisationProperty()

        assertEquals(RenameValidationResult.ok(), validator(target).validate("title_renamed"))
    }

    @Test
    fun testLocalisationProperty_validNameWithExtraChars() {
        val target = findLocalisationProperty()

        assertEquals(RenameValidationResult.ok(), validator(target).validate("title-renamed.v2"))
    }

    @Test
    fun testLocalisationProperty_emptyName() {
        val target = findLocalisationProperty()

        assertEquals(RenameValidationResult.invalid(), validator(target).validate(""))
    }

    @Test
    fun testLocalisationProperty_nameWithBlank() {
        val target = findLocalisationProperty()

        assertEquals(RenameValidationResult.invalid(), validator(target).validate("title renamed"))
    }

    // endregion

    // region definition

    @Test
    fun testDefinition_validName() {
        val target = findDefinition()

        assertEquals(RenameValidationResult.ok(), validator(target).validate("windea_renamed"))
    }

    @Test
    fun testDefinition_validNameWithExtraChars() {
        val target = findDefinition()

        assertEquals(RenameValidationResult.ok(), validator(target).validate("windea-renamed.v2"))
    }

    @Test
    fun testDefinition_emptyName() {
        val target = findDefinition()

        assertEquals(RenameValidationResult.invalid(), validator(target).validate(""))
    }

    @Test
    fun testDefinition_nameWithUnsupportedChar() {
        val target = findDefinition()

        assertEquals(RenameValidationResult.invalid(), validator(target).validate("windea!"))
    }

    // endregion

    // region other target

    @Test
    fun testOtherTarget_acceptsAnyNonEmptyName() {
        val target = myFixture.addFileToProject("common/tests/plain.md", "content").virtualFile
        val element = PsiManager.getInstance(project).findFile(target) ?: error("Expected a PSI file")

        assertEquals(RenameValidationResult.ok(), validator(element).validate("anything at all"))
        assertEquals(RenameValidationResult.invalid(), validator(element).validate(""))
    }

    // endregion

    // region symbol integration

    @Test
    fun testSymbolValidator() {
        prepareInlineCodeTargets()
        configureMarkdown("`windea`")

        val validator = findInlineCodeSymbol().validator()
        assertTrue(validator is MarkdownInlineCodeRenameValidator)
        assertEquals(RenameValidationResult.ok(), validator.validate("windea_renamed"))
        assertEquals(RenameValidationResult.invalid(), validator.validate("windea!"))
    }

    // endregion

    private fun validator(element: com.intellij.psi.PsiElement) = MarkdownInlineCodeRenameValidator(element)

    private fun findScriptedVariable(): ParadoxScriptScriptedVariable {
        val file = addScriptedVariableFile("common/scripted_variables/00_test.txt", "@greeting = \"hello\"")
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: error("Expected a PSI file")
        return PsiTreeUtil.findChildOfType(psiFile, ParadoxScriptScriptedVariable::class.java)
            ?: error("Expected a scripted variable")
    }

    private fun findLocalisationProperty(): ParadoxLocalisationProperty {
        val file = copyFixture("localisation/main_l_english.yml")
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: error("Expected a PSI file")
        return PsiTreeUtil.findChildOfType(psiFile, ParadoxLocalisationProperty::class.java)
            ?: error("Expected a localisation property")
    }

    private fun findDefinition(): ParadoxDefinitionElement {
        val file = copyFixture("common/characters/00_characters.txt")
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: error("Expected a PSI file")
        return PsiTreeUtil.findChildOfType(psiFile, ParadoxDefinitionElement::class.java)
            ?: error("Expected a definition")
    }
}
