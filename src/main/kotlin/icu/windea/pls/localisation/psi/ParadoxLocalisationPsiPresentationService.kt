package icu.windea.pls.localisation.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.icon
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.selectLocale
import javax.swing.Icon

object ParadoxLocalisationPsiPresentationService {
    fun accept(element: PsiElement?, forFile: Boolean = true): Boolean {
        return when (element) {
            null -> false
            is ParadoxLocalisationFile -> forFile
            is ParadoxLocalisationLocale -> true
            is ParadoxLocalisationPropertyList -> true
            is ParadoxLocalisationProperty -> true
            else -> false
        }
    }

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
            // 名字
            is ParadoxLocalisationFile -> element.name
            // 名字
            is ParadoxLocalisationLocale -> element.name
            // 语言环境的名字（可能匿名）
            is ParadoxLocalisationPropertyList -> element.locale?.name.or.anonymous()
            // 名字
            is ParadoxLocalisationProperty -> element.name
            // 回退
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getTreePresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getLongPresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getLocationString(element: PsiElement): String? {
        ParadoxPsiPresentationService.getFileInfoText(element)?.let { return it }
        return element.containingFile?.name
    }

    fun getTreeLocationString(element: PsiElement): String? {
        return when (element) {
            // 语言区域的显示文本
            is ParadoxLocalisationPropertyList -> selectLocale(element)?.text
            else -> null
        }
    }
}
