package icu.windea.pls.localisation.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.icon
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.psi.ParadoxElementPresentationService
import icu.windea.pls.lang.selectLocale
import javax.swing.Icon

object ParadoxLocalisationElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        return getPatchedIcon(element) ?: element.icon
    }

    fun getPatchedIcon(element: PsiElement): Icon? {
        when (element) {
            is ParadoxLocalisationProperty -> {
                run {
                    if (element.type == null) return@run
                    return ChronicleIcons.Nodes.Localisation
                }
            }
        }
        return null
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            is ParadoxLocalisationFile -> element.name
            is ParadoxLocalisationLocale -> element.name
            is ParadoxLocalisationPropertyList -> element.locale?.name.or.anonymous()
            is ParadoxLocalisationProperty -> element.name
            is PsiPresentableTextAwareElement -> element.presentableText
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getLocationString(element: PsiElement): String? {
        ParadoxElementPresentationService.getFileInfoText(element)?.let { return it }
        return element.containingFile?.name
    }

    fun getPresentableTextInTree(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getLocationStringInTree(element: PsiElement): String? {
        return when (element) {
            is ParadoxLocalisationPropertyList -> selectLocale(element)?.text
            else -> null
        }
    }

    fun getLongPresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }
}
