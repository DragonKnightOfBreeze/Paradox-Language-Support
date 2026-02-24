package icu.windea.pls.csv.navigation

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.truncate
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.csv.psi.getHeaderColumn
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.model.constants.PlsStrings
import javax.swing.Icon

object ParadoxCsvNavigationManager {
    fun accept(element: PsiElement?, forFile: Boolean = true): Boolean {
        return when (element) {
            null -> false
            is ParadoxCsvFile -> forFile
            is ParadoxCsvRowElement -> true
            is ParadoxCsvColumn -> true
            else -> false
        }
    }

    fun getIcon(element: PsiElement): Icon? {
        // 直接复用 PSI 的图标
        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            // 文件名
            is ParadoxCsvFile -> element.name
            // 特殊标记
            is ParadoxCsvHeader -> PlsStrings.headerMarker
            // 特殊标记
            is ParadoxCsvRow -> PlsStrings.rowMarker
            // 截断后的名字
            is ParadoxCsvColumn -> element.name.formatted()
            // 回退
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getLongPresentableText(element: PsiElement): String? {
        val p = getPresentableText(element) ?: return null
        val l = getLocalLocationString(element) ?: return p
        return "$p ($l)"
    }

    fun getLocationString(element: PsiElement): String? {
        ParadoxPsiManager.getFileInfoText(element)?.let { return it }
        return element.containingFile?.name
    }

    fun getLocalLocationString(element: PsiElement): String? {
        return when (element) {
            // 截断后的对应的表头列的名字
            is ParadoxCsvColumn -> element.getHeaderColumn()?.name?.formatted()
            else -> null
        }
    }

    private fun String.formatted(): String {
        return when {
            isEmpty() -> "\"\""
            else -> truncate(PlsInternalSettings.getInstance().textLengthLimitForPresentation)
        }
    }
}
