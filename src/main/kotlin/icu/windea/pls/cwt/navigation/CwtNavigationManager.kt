package icu.windea.pls.cwt.navigation

import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.truncate
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue
import icu.windea.pls.lang.settings.PlsInternalSettings
import javax.swing.Icon

object CwtNavigationManager {
    fun accept(element: PsiElement?, forFile: Boolean = true): Boolean {
        return when (element) {
            null -> false
            is CwtFile -> forFile
            is CwtProperty -> true
            is CwtValue -> element.isBlockValue()
            else -> false
        }
    }

    fun getIcon(element: PsiElement): Icon? {
        // 直接复用 PSI 的图标
        return element.icon
    }

    fun getLongPresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            // 名字
            is CwtFile -> element.name
            // 名字
            is CwtProperty -> element.name
            // 截断后的名字
            is CwtValue -> element.name.formatted()
            else -> null
        }
    }

    @Suppress("unused")
    fun getLocationString(element: PsiElement): String? {
        return null
    }

    private fun String.formatted(): String {
        return when {
            isEmpty() -> "\"\""
            else -> truncate(PlsInternalSettings.getInstance().textLengthLimitForPresentation)
        }
    }
}
