package icu.windea.pls.csv.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.csv.navigation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

class ParadoxCsvFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null
) : PsiFileBase(viewProvider, ParadoxCsvLanguage) {
    companion object {
        @JvmField
        val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_CSV_FILE", ParadoxCsvLanguage)
    }

    val header: ParadoxCsvHeader? get() = findChild<_>()

    val rows: List<ParadoxCsvRow> get() = findChildren<_>()

    override fun getFileType() = ParadoxCsvFileType

    override fun getPresentation() = ParadoxCsvItemPresentation(this)

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || another is ParadoxCsvFile && ParadoxFileManager.isEquivalentFile(this, another)
    }
}
