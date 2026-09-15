package icu.windea.pls.lang.psi

import com.intellij.psi.PsiFile
import icu.windea.pls.lang.ParadoxFileType

interface ParadoxFile : PsiFile {
    override fun getFileType(): ParadoxFileType
}
