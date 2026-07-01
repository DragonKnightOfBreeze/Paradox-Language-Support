package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.lang.fileInfo

object ParadoxPsiPresentationService {
    fun getFileInfoText(element: PsiElement): String? {
        val fileInfo = element.fileInfo ?: return null
        val path = fileInfo.path.path
        val entry = fileInfo.entry
        return when {
            entry.isEmpty() -> path
            else -> "$path of $entry"
        }
    }

    fun getLongFileInfoText(element: PsiElement): String? {
        val fileInfo = element.fileInfo ?: return null
        val qualifiedName = fileInfo.rootInfo.qualifiedName
        val path = fileInfo.path.path
        val entry = fileInfo.entry
        return when {
            entry.isEmpty() -> "$path in $qualifiedName"
            else -> "$path of $entry in $qualifiedName"
        }
    }
}
