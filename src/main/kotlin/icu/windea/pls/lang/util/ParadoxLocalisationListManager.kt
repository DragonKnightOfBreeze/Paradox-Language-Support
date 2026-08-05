package icu.windea.pls.lang.util

import com.intellij.openapi.ide.CopyPasteManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import java.awt.datatransfer.StringSelection

object ParadoxLocalisationListManager {
    /**
     * 复制本地化列表到剪贴板（保留语言环境前缀，保留其中的注释和空行）。
     */
    fun copyWithLocale(element: ParadoxLocalisationPropertyList) {
        // 2.1.8 不检查 localeId 是否合法
        val text = element.text.trim() // 去除首尾空白
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }

    /**
     * 复制本地化列表到剪贴板（不保留语言环境前缀，保留其中的注释和空行）。
     */
    fun copyWithoutLocale(element: ParadoxLocalisationPropertyList) {
        // 2.1.8 不检查 localeId 是否合法
        val offset = element.locale?.textLength ?: 0
        val text = element.text.drop(offset).trimIndent().trim() // 去除前缀，去除最小缩进，去除首尾空白
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }
}
