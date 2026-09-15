package icu.windea.pls.csv.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.select.oneBy
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.csv.text.ParadoxCsv

@Suppress("unused")
object ParadoxCsvElementFactory {
    // create from text

    @JvmStatic
    fun createFileFromText(project: Project, text: String): ParadoxCsvFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxCsvLanguage, text).castOrNull()
            ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createWhiteSpaceFromText(project: Project, text: String): PsiElement {
        return PsiParserFacade.getInstance(project).createWhiteSpaceFromText(text)
    }

    @JvmStatic
    fun createColumnFromText(project: Project, text: String): ParadoxCsvColumn {
        val fileText = text
        return createFileFromText(project, fileText)
            .children().oneBy<ParadoxCsvColumnContainer>()
            .children().oneBy<ParadoxCsvColumn>()
            ?: throw IncorrectOperationException()
    }

    // create smartly

    @JvmStatic
    fun createEmptyHeader(project: Project, length: Int): ParadoxCsvHeader {
        val fileText = "\n" + ParadoxCsvPsiService.getSeparator().toString().repeat(length) + "\n"
        return createFileFromText(project, fileText)
            .children().oneBy<ParadoxCsvHeader>()
            ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createEmptyRow(project: Project, length: Int): ParadoxCsvRow {
        val fileText = "a\n" + ParadoxCsvPsiService.getSeparator().toString().repeat(length) + "\n"
        return createFileFromText(project, fileText)
            .children().oneBy<ParadoxCsvRow>()
            ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createSeparator(project: Project): PsiElement {
        val fileText = ParadoxCsvPsiService.getSeparator().toString()
        return createFileFromText(project, fileText)
            .children().oneBy<ParadoxCsvColumnContainer>()
            .children().oneBy(SEPARATOR)
            ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createColumn(project: Project, value: String): ParadoxCsvColumn {
        val text = value.quoteIfNeeded(QuotePatterns.ParadoxCsv)
        return createColumn(project, text)
    }
}
