package icu.windea.pls.csv.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.csv.navigation.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

class ParadoxCsvFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null
) : PsiFileBase(viewProvider, ParadoxCsvLanguage) {
    override fun getFileType() = ParadoxCsvFileType

    val rows: List<ParadoxCsvRow> get() = findChildren<_>()

    override fun getPresentation(): ItemPresentation {
        return ParadoxCsvFilePresentation(this)
    }

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || (another is ParadoxCsvFile && fileInfo == another.fileInfo)
    }
}
