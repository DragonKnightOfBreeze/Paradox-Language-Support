package icu.windea.pls.csv.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.csv.ParadoxCsvFileType
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.lang.psi.ParadoxFile

interface ParadoxCsvFile : ParadoxFile, PsiFile {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_CSV_FILE", ParadoxCsvLanguage)
    }

    val header: ParadoxCsvHeader?

    val rows: List<ParadoxCsvRow>

    override fun getFileType() = ParadoxCsvFileType
}
