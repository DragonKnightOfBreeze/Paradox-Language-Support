package icu.windea.pls.csv.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

@Suppress("unused")
object ParadoxCsvElementFactory {
    @JvmStatic
    fun createFileFromText(project: Project, text: String): ParadoxCsvFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxCsvLanguage, text)
            .castOrNull<ParadoxCsvFile>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createWhiteSpaceFromText(project: Project, text: String): PsiElement {
        return PsiParserFacade.getInstance(project).createWhiteSpaceFromText(text)
    }

    @JvmStatic
    fun createEmptyHeader(project: Project, length: Int): ParadoxCsvHeader {
        val fileText = "\n" + ParadoxCsvPsiService.getSeparator().toString().repeat(length) + "\n"
        return createFileFromText(project, fileText)
            .findChild<ParadoxCsvHeader>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createEmptyRow(project: Project, length: Int): ParadoxCsvRow {
        val fileText = "a\n" + ParadoxCsvPsiService.getSeparator().toString().repeat(length) + "\n"
        return createFileFromText(project, fileText)
            .findChild<ParadoxCsvRow>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createSeparator(project: Project): PsiElement {
        val fileText = ParadoxCsvPsiService.getSeparator().toString()
        return createFileFromText(project, fileText)
            .findChild<ParadoxCsvRowElement>()
            ?.findChild { it.elementType == SEPARATOR } ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createColumnFromText(project: Project, text: String): ParadoxCsvColumn {
        val fileText = text
        return createFileFromText(project, fileText)
            .findChild<ParadoxCsvRowElement>()
            ?.findChild<ParadoxCsvColumn>() ?: throw IncorrectOperationException()
    }
}
