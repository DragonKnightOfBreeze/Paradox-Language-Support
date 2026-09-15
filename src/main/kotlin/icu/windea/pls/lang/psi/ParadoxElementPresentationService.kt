package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import javax.swing.Icon

object ParadoxElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        when (element) {
            is ParadoxLocalisationProperty -> {
                run {
                    if (element.type == null) return@run
                    return ChronicleIcons.Nodes.Localisation
                }
            }
        }
        return null // TODO 3.0.3
    }

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
