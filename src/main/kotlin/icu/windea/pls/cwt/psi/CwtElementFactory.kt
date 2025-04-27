package icu.windea.pls.cwt.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*

object CwtElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): CwtFile {
        return PsiFileFactory.getInstance(project).createFileFromText(CwtLanguage.INSTANCE, text)
            .castOrNull<CwtFile>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createRootBlock(project: Project, text: String): CwtRootBlock {
        return createDummyFile(project, text)
            .findChild<CwtRootBlock>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createOption(project: Project, text: String): CwtOption {
        return createRootBlock(project, "## $text")
            .findChild<CwtOptionComment>()
            ?.findChild<CwtOption>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createOptionKey(project: Project, text: String): CwtOptionKey {
        return createOption(project, "$text = v")
            .findChild<CwtOptionKey>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createProperty(project: Project, text: String): CwtProperty {
        return createRootBlock(project, text)
            .findChild<CwtProperty>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyKey(project: Project, text: String): CwtPropertyKey {
        return createProperty(project, "$text = v")
            .findChild<CwtPropertyKey>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createValue(project: Project, text: String): CwtValue {
        return createRootBlock(project, text)
            .findChild<CwtValue>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createString(project: Project, text: String): CwtString {
        return createValue(project, text)
            .castOrNull<CwtString>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createBlock(project: Project, text: String): CwtBlock {
        return createValue(project, text)
            .castOrNull<CwtBlock>() ?: throw IncorrectOperationException()
    }
}
