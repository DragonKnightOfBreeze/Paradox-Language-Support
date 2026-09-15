package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.lang.psi.ParadoxElementPresentationService
import javax.swing.Icon

object ParadoxScriptElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getIcon(element)?.let { return it }

        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getPresentableText(element)?.let { return it }

        return when (element) {
            is ParadoxScriptFile -> element.name
            is ParadoxScriptProperty -> element.name
            is ParadoxScriptScriptedVariable -> "@" + element.name.or.unresolved()
            is PsiPresentableTextAwareElement -> element.presentableText
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getLocationString(element: PsiElement): String? {
        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getLocationString(element)?.let { return it }

        return element.containingFile?.name
    }

    fun getTreeLocationString(element: PsiElement): String? {
        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getTreeLocationString(element)?.let { return it }

        return null
    }

    fun getPresentableTextInNavBar(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getElementInfoInBreadCrumbs(element: PsiElement): String {
        return getPresentableText(element).orEmpty()
    }
}
