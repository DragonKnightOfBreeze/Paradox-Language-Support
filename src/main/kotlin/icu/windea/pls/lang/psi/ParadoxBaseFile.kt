package icu.windea.pls.lang.psi

import com.intellij.psi.PsiFile
import icu.windea.pls.lang.ParadoxBaseFileType

interface ParadoxBaseFile : PsiFile {
    override fun getFileType(): ParadoxBaseFileType
}
