package icu.windea.pls.csv.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvFile

class ParadoxCsvFileImpl(
    viewProvider: FileViewProvider,
) : PsiFileBase(viewProvider, ParadoxCsvLanguage), ParadoxCsvFile {
    override val header get() = ParadoxCsvPsiImplUtil.getHeader(this)

    override val rows get() = ParadoxCsvPsiImplUtil.getRows(this)

    override fun isEquivalentTo(another: PsiElement?) = ParadoxCsvPsiImplUtil.isEquivalentTo(this, another)

    override fun getPresentation() = ParadoxCsvPsiImplUtil.getPresentation(this)

    override fun getResolveScope() = ParadoxCsvPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = ParadoxCsvPsiImplUtil.getUseScope(this)

    override fun toString() = ParadoxCsvPsiImplUtil.toString(this)
}
