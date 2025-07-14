package icu.windea.pls.csv.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.cwt.*

object ParadoxCsvElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): ParadoxCsvFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxCsvLanguage, text)
            .castOrNull<ParadoxCsvFile>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createColumn(project: Project, text: String): ParadoxCsvColumn {
        return PsiFileFactory.getInstance(project).createFileFromText(CwtLanguage, text)
            ?.findChild<ParadoxCsvRow>()
            ?.findChild<ParadoxCsvColumn>() ?: throw IncorrectOperationException()
    }
}
