package icu.windea.pls.csv.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.core.findChild
import icu.windea.pls.core.findChildren
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.csv.ParadoxCsvFileType
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxGameType

class ParadoxCsvFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null,
) : PsiFileBase(viewProvider, ParadoxCsvLanguage), ParadoxFile {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_CSV_FILE", ParadoxCsvLanguage)
    }

    val header: ParadoxCsvHeader? get() = findChild<_>()

    val rows: List<ParadoxCsvRow> get() = findChildren<_>()

    override fun getFileType() = ParadoxCsvFileType

    override fun getPresentation() = ParadoxCsvPsiPresentation(this)

    override fun toString() = PsiService.toPresentableString(this)

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || another is ParadoxCsvFile && ParadoxFileManager.isEquivalentFile(this, another)
    }
}
