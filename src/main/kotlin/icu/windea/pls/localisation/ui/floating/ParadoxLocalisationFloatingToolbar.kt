package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.ui.customization.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import kotlinx.coroutines.*

//org.intellij.plugins.markdown.ui.floating.MarkdownFloatingToolbar

/**
 * 当用户鼠标选中本地化文本（的其中一部分）时,将会显示的悬浮工具栏栏。
 *
 * 提供动作：
 * * 快速插入引用 - 不会检查插入后语法是否合法
 * * 快速插入图标 - 不会检查插入后语法是否合法
 * * 快速插入命令 - 不会检查插入后语法是否合法
 * * 更改文本颜色（将会列出所有可选的颜色代码）
 *
 * @see icu.windea.pls.localisation.ui.actions.styling.CreateReferenceAction
 * @see icu.windea.pls.localisation.ui.actions.styling.CreateIconAction
 * @see icu.windea.pls.localisation.ui.actions.styling.CreateCommandAction
 * @see icu.windea.pls.localisation.ui.actions.styling.SetColorGroup
 * @see icu.windea.pls.localisation.ui.actions.styling.SetColorAction
 */
class ParadoxLocalisationFloatingToolbar(
    editor: Editor,
    coroutineScope: CoroutineScope
) : FloatingToolbar(editor, coroutineScope) {
    override fun canBeShownAtCurrentSelection(): Boolean {
        if (!isEnabled()) return false
        val project = editor.project ?: return false
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return false
        if (!PsiDocumentManager.getInstance(file.project).isCommitted(editor.document)) return false

        val selectionModel = editor.selectionModel
        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd
        //忽略没有选择文本的情况
        if (selectionStart == selectionEnd) return false
        //忽略跨行的情况
        if (editor.document.getLineNumber(selectionStart) != editor.document.getLineNumber(selectionEnd)) return false
        val elementAtStart = file.findElementAt(selectionStart)
        val elementAtEnd = file.findElementAt(selectionEnd - 1)
        //要求开始位置和结束位置的左边或右边是STRING_TOKEN/LEFT_QUOTE/RIGHT_QUOTE，向上能查找到同一个ParadoxLocalisationPropertyValue
        if (elementAtStart == null || elementAtEnd == null) return false
        val stringTokenOrQuote = ParadoxLocalisationTokenSets.STRING_TOKEN_OR_QUOTE
        if (elementAtStart.elementType !in stringTokenOrQuote && elementAtStart.prevLeaf(false).elementType !in stringTokenOrQuote) return false
        if (elementAtEnd.elementType !in stringTokenOrQuote && elementAtEnd.nextLeaf(false).elementType !in stringTokenOrQuote) return false
        val propertyValueAtStart = elementAtStart.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        val propertyValueAtEnd = elementAtEnd.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        if (propertyValueAtStart !== propertyValueAtEnd) return false
        val propertyValue = propertyValueAtStart
        val textRange = propertyValue.textRange
        //要求选择文本的范围在引号之间
        val start = if (propertyValue.firstChild.elementType == LEFT_QUOTE) textRange.startOffset + 1 else textRange.startOffset
        val end = if (propertyValue.lastChild.elementType == RIGHT_QUOTE) textRange.endOffset - 1 else textRange.endOffset
        return selectionStart >= start && selectionEnd <= end
    }

    override fun isEnabled(): Boolean {
        return shouldShowFloatingToolbar()
    }

    override fun createActionGroup(): ActionGroup? {
        return CustomActionsSchema.getInstance().getCorrectedAction("Pls.ParadoxLocalisation.Toolbar.Floating") as? ActionGroup
    }

    private fun shouldShowFloatingToolbar(): Boolean {
        return PlsFacade.getSettings().others.showLocalisationFloatingToolbar
    }
}
