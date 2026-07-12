package icu.windea.pls.lang.ui.actions.styling

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.prevLeaf
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.model.ParadoxTextColorInfo

// 动态获取可用的 SetColorAction
private val setColorActionCache = CacheBuilder().weakKeys().build<ParadoxTextColorInfo, SetColorAction> { key -> SetColorAction(key) }

class SetColorGroup : DefaultActionGroup() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxLocalisationFile) return
        if (!isAvailable(editor, file)) return
        val colorInfos = ParadoxTextColorManager.getInfos(file.project, file)
        if (colorInfos.isEmpty()) return
        e.presentation.isEnabledAndVisible = true
        val actions = colorInfos.map { setColorActionCache.get(it) }
        synchronized(this) {
            removeAll()
            addAll(actions)
        }
    }

    private fun isAvailable(editor: Editor, file: ParadoxLocalisationFile): Boolean {
        if (!PsiDocumentManager.getInstance(file.project).isCommitted(editor.document)) return false

        val selectionModel = editor.selectionModel
        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd

        // 忽略没有选择文本的情况
        if (selectionStart == selectionEnd) return false

        val startElement = file.findElementAt(selectionStart) ?: return false
        val endElement = file.findElementAt(selectionEnd - 1) ?: return false

        // 要求开始位置和结束位置的左边或右边是 TEXT_TOKEN/LEFT_QUOTE/RIGHT_QUOTE
        val stringOrQuoteTokens = ParadoxLocalisationTokenSets.STRING_OR_QUOTE_TOKENS
        if (startElement.elementType !in stringOrQuoteTokens && startElement.prevLeaf(false).elementType !in stringOrQuoteTokens) return false
        if (endElement.elementType !in stringOrQuoteTokens && endElement.nextLeaf(false).elementType !in stringOrQuoteTokens) return false

        // 要求向上能查找到同一个 ParadoxLocalisationPropertyValue
        val startPropertyValue = startElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        val endPropertyValue = endElement.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        if (startPropertyValue !== endPropertyValue) return false

        // 要求选择文本的范围在引号之间
        val propertyValue = startPropertyValue
        val textRange = propertyValue.textRange
        val start = if (propertyValue.firstChild.elementType == LEFT_QUOTE) textRange.startOffset + 1 else textRange.startOffset
        val end = if (propertyValue.lastChild.elementType == RIGHT_QUOTE) textRange.endOffset - 1 else textRange.endOffset
        if (selectionStart < start || selectionEnd > end) return false

        return true
    }
}
