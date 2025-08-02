package icu.windea.pls.csv.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.lang.util.*

object ParadoxCsvElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): ParadoxCsvFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxCsvLanguage, text)
            .castOrNull<ParadoxCsvFile>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createEmptyRow(project: Project, length: Int): ParadoxCsvRowElement {
        return createDummyFile(project, ParadoxCsvManager.getSeparator().toString().repeat(length))
            .findChild<ParadoxCsvRowElement>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createColumn(project: Project, text: String): ParadoxCsvColumn {
        return createDummyFile(project, text + ParadoxCsvManager.getSeparator())
            .findChild<ParadoxCsvRowElement>()
            ?.findChild<ParadoxCsvColumn>() ?: throw IncorrectOperationException()
    }
}
