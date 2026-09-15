package icu.windea.pls.lang.psi

import com.intellij.psi.PsiFile
import icu.windea.pls.lang.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType

interface ParadoxFile : PsiFile {
    val gameType: ParadoxGameType? get() = null

    override fun getFileType(): ParadoxFileType
}
