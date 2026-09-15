package icu.windea.pls.csv.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.csv.ParadoxCsvFileType
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.impl.ParadoxCsvPsiImplUtil
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.model.ParadoxGameType

class ParadoxCsvFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null,
) : PsiFileBase(viewProvider, ParadoxCsvLanguage), ParadoxFile {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_CSV_FILE", ParadoxCsvLanguage)
    }

    val header: ParadoxCsvHeader? get() = ParadoxCsvPsiImplUtil.getHeader(this)

    val rows: List<ParadoxCsvRow> get() = ParadoxCsvPsiImplUtil.getRows(this)

    override fun getFileType() = ParadoxCsvFileType

    override fun getPresentation() = ParadoxCsvPsiImplUtil.getPresentation(this)

    override fun isEquivalentTo(another: PsiElement?) = ParadoxCsvPsiImplUtil.isEquivalentTo(this, another)

    override fun getResolveScope() = ParadoxCsvPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = ParadoxCsvPsiImplUtil.getUseScope(this)

    override fun toString() = ParadoxCsvPsiImplUtil.toString(this)
}
