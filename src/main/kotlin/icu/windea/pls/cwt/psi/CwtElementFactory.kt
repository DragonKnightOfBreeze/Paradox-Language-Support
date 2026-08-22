package icu.windea.pls.cwt.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.ParadoxCsvPsiManipulationService
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

@Suppress("unused")
object CwtElementFactory {
    // create from text

    @JvmStatic
    fun createFileFromText(project: Project, text: String): CwtFile {
        return PsiFileFactory.getInstance(project).createFileFromText(CwtLanguage, text)
            .castOrNull<CwtFile>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createWhiteSpaceFromText(project: Project, text: String): PsiElement {
        return PsiParserFacade.getInstance(project).createWhiteSpaceFromText(text)
    }

    @JvmStatic
    fun createRootBlockFromText(project: Project, text: String): CwtRootBlock {
        return createFileFromText(project, text)
            .findChild<CwtRootBlock>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createOptionFromText(project: Project, text: String): CwtOption {
        return createRootBlockFromText(project, "## $text")
            .findChild<CwtOptionComment>()
            ?.findChild<_>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createOptionKeyFromText(project: Project, text: String): CwtOptionKey {
        return createOptionFromText(project, "$text = v")
            .findChild<CwtOptionKey>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyFromText(project: Project, text: String): CwtProperty {
        return createRootBlockFromText(project, text)
            .findChild<CwtProperty>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyKeyFromText(project: Project, text: String): CwtPropertyKey {
        return createPropertyFromText(project, "$text = v")
            .findChild<CwtPropertyKey>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createValueFromText(project: Project, text: String): CwtValue {
        return createRootBlockFromText(project, text)
            .findChild<CwtValue>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createStringFromText(project: Project, text: String): CwtString {
        return createValueFromText(project, text)
            .castOrNull<CwtString>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createBlockFromText(project: Project, text: String): CwtBlock {
        return createValueFromText(project, text)
            .castOrNull<CwtBlock>() ?: throw IncorrectOperationException()
    }

    // create smartly

    @JvmStatic
    fun createOption(project: Project, key: String, value: String): CwtOption {
        val text = "${ParadoxCsvPsiManipulationService.quoteIfNeeded(key)} = ${ParadoxCsvPsiManipulationService.quoteIfNeeded(value)}"
        return createOptionFromText(project, text)
    }

    @JvmStatic
    fun createProperty(project: Project, key: String, value: String, separatorString: String = " = "): CwtProperty {
        val text = "${CwtPsiManipulationService.quoteIfNeeded(key)}$separatorString${CwtPsiManipulationService.quoteIfNeeded(value)}"
        return createPropertyFromText(project, text)
    }

    @JvmStatic
    fun createOptionKey(project: Project, value: String): CwtOptionKey {
        val text = CwtPsiManipulationService.quoteIfNeeded(value)
        return createOptionKeyFromText(project, text)
    }

    @JvmStatic
    fun createPropertyKey(project: Project, value: String): CwtPropertyKey {
        val text = CwtPsiManipulationService.quoteIfNeeded(value)
        return createPropertyKeyFromText(project, text)
    }

    @JvmStatic
    fun createString(project: Project, value: String): CwtString {
        val text = CwtPsiManipulationService.quoteIfNeeded(value)
        return createStringFromText(project, text)
    }
}
