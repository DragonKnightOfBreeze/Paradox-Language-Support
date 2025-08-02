package icu.windea.pls.csv.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.lang.util.*

object ParadoxCsvElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): ParadoxCsvFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxCsvLanguage, text)
            .castOrNull<ParadoxCsvFile>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createEmptyHeader(project: Project, length: Int): ParadoxCsvHeader {
        val fileText = "\n" + ParadoxCsvManager.getSeparator().toString().repeat(length) + "\n"
        return createDummyFile(project, fileText)
            .findChild<ParadoxCsvHeader>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createEmptyRow(project: Project, length: Int): ParadoxCsvRow {
        val fileText = "a\n" + ParadoxCsvManager.getSeparator().toString().repeat(length) + "\n"
        return createDummyFile(project, fileText)
            .findChild<ParadoxCsvRow>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createColumn(project: Project, text: String): ParadoxCsvColumn {
        val fileText = text
        return createDummyFile(project, fileText)
            .findChild<ParadoxCsvRowElement>()
            ?.findChild<ParadoxCsvColumn>() ?: throw IncorrectOperationException()
    }

    fun createSeparator(project: Project): PsiElement {
        val fileText = ";"
        return createDummyFile(project, fileText)
            .findChild<ParadoxCsvRowElement>()
            ?.findChild { it.elementType == SEPARATOR } ?: throw IncorrectOperationException()
    }
}
