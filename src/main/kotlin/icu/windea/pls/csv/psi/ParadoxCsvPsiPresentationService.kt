package icu.windea.pls.csv.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.truncate
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.settings.ChronicleInternalSettings
import icu.windea.pls.model.constants.ChronicleStrings
import javax.swing.Icon

object ParadoxCsvPsiPresentationService {
    fun accept(element: PsiElement?, forFile: Boolean = true): Boolean {
        return when (element) {
            null -> false
            is ParadoxCsvFile -> forFile
            is ParadoxCsvColumnContainer -> true
            is ParadoxCsvColumn -> true
            else -> false
        }
    }

    fun getIcon(element: PsiElement): Icon? {
        return getPatchedIcon(element) ?: element.icon
    }

    @Suppress("unused")
    fun getPatchedIcon(element: PsiElement): Icon? {
        return null
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            // 文件名
            is ParadoxCsvFile -> element.name
            // 特殊标记
            is ParadoxCsvHeader -> ChronicleStrings.headerText
            // 特殊标记
            is ParadoxCsvRow -> ChronicleStrings.rowText
            // 截断后的名字
            is ParadoxCsvColumn -> element.name.formatted()
            // 回退
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getTreePresentableText(element: PsiElement): String? {
        return getPresentableText(element)
    }

    fun getLongPresentableText(element: PsiElement): String? {
        val p = getPresentableText(element) ?: return null
        val l = getTreeLocationString(element) ?: return p
        return "$p ($l)"
    }

    fun getLocationString(element: PsiElement): String? {
        ParadoxPsiPresentationService.getFileInfoText(element)?.let { return it }
        return element.containingFile?.name
    }

    fun getTreeLocationString(element: PsiElement): String? {
        return when (element) {
            // 截断后的对应的表头列的名字
            is ParadoxCsvColumn -> ParadoxCsvPsiService.getHeaderColumn(element)?.name?.formatted()
            else -> null
        }
    }

    private fun String.formatted(): String {
        return when {
            isEmpty() -> "\"\""
            else -> truncate(ChronicleInternalSettings.getInstance().textLengthLimitForPresentation)
        }
    }
}
