package icu.windea.pls.localisation.navigation

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.icon
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import javax.swing.Icon

object ParadoxLocalisationNavigationManager {
    fun accept(element: PsiElement?, forFile: Boolean = true): Boolean {
        return when(element) {
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
        when(element) {
            is ParadoxLocalisationProperty -> {
                run {
                    if(element.type == null) return@run
                    return PlsIcons.Nodes.Localisation
                }
            }
        }
        return null
    }

    fun getLongPresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            // 名字
            is ParadoxLocalisationFile -> element.name
            // 名字
            is ParadoxLocalisationLocale -> element.name
            // 语言环境的名字，或者匿名
            is ParadoxLocalisationPropertyList -> element.locale?.name.or.anonymous()
            // 名字
            is ParadoxLocalisationProperty -> element.name
            else -> null
        }
    }

    fun getLocationString(element: PsiElement): String? {
        return when (element) {
            // 语言区域的展示文本
            is ParadoxLocalisationPropertyList -> selectLocale(element)?.text
            else -> null
        }
    }
}
