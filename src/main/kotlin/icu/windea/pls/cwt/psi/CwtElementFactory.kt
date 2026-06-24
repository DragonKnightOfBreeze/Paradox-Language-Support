package icu.windea.pls.cwt.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

@Suppress("unused")
object CwtElementFactory {
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
    fun createBlockFromTextFromText(project: Project, text: String): CwtBlock {
        return createValueFromText(project, text)
            .castOrNull<CwtBlock>() ?: throw IncorrectOperationException()
    }
}
