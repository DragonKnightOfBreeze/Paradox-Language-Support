package icu.windea.pls.localisation.ui.floating

import com.intellij.codeInsight.hint.*
import com.intellij.ide.ui.customization.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.fileEditor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.ui.actions.styling.*
import java.awt.*
import java.awt.event.*
import javax.swing.*
import kotlin.properties.*

//org.intellij.plugins.markdown.ui.floating.FloatingToolbar
//不要参考最新的写法进行重构 - 会无法正常显示悬浮工具栏

/**
 * 当用户鼠标选中本地化文本(的其中一部分)时,将会显示的悬浮工具栏栏。
 * 提供动作：
 * * 快速插入引用（`$FOO$`） - 不会检查插入后语法是否合法
 * * 快速插入图标（`£foo£`） - 不会检查插入后语法是否合法
 * * 快速插入命令（`[Foo]`） - 不会检查插入后语法是否合法
 * * 更改文本颜色（将会列出所有可选的颜色代码）
 * @see icu.windea.pls.localisation.ui.actions.styling.CreateReferenceAction
 * @see icu.windea.pls.localisation.ui.actions.styling.CreateIconAction
 * @see icu.windea.pls.localisation.ui.actions.styling.CreateCommandAction
 * @see icu.windea.pls.localisation.ui.actions.styling.SetColorGroup
 * @see icu.windea.pls.localisation.ui.actions.styling.SetColorAction
 */
class FloatingToolbar(
    val textEditor: TextEditor,
    private val actionGroupId: String
) : Disposable {
    private val mouseListener = MouseListener()
    private val keyboardListener = KeyboardListener()
    private val mouseMotionListener = MouseMotionListener()
    
    private var hint: LightweightHint? = null
    private var buttonSize: Int by Delegates.notNull()
    private var lastSelection: String? = null
    
    private val editor: Editor get() = textEditor.editor
    
    init {
        registerListeners()
    }
    
    fun isShown() = hint != null
    
    fun hideIfShown() {
        hint?.hide()
    }
    
    fun showIfHidden() {
        if(hint != null || !canBeShownAtCurrentSelection()) {
            return
        }
        val toolbar = createActionToolbar(editor.contentComponent)
        buttonSize = toolbar.maxButtonHeight
        
        val newHint = LightweightHint(toolbar.component)
        newHint.setForceShowAsPopup(true)
        
        showOrUpdateLocation(newHint)
        newHint.addHintListener { this.hint = null }
        this.hint = newHint
    }
    
    fun updateLocationIfShown() {
        showOrUpdateLocation(hint ?: return)
    }
    
    override fun dispose() {
        unregisterListeners()
        hideIfShown()
        hint = null
    }
    
    private fun createActionToolbar(targetComponent: JComponent): ActionToolbar {
        val group = CustomActionsSchema.getInstance().getCorrectedAction(actionGroupId) as ActionGroup
        val toolbar = object : ActionToolbarImpl(ActionPlaces.EDITOR_TOOLBAR, group, true) {
            override fun addNotify() {
                super.addNotify()
                //NOTE 这是必要的，否则显示悬浮工具栏时，其中的颜色图标不会立即全部显示
                //@Suppress("UnstableApiUsage")
                //updateActionsImmediately(true)
                try {
                    this.function("updateActionsImmediately")(true)
                } catch(e: Exception) {
                    thisLogger().warn(e)
                    updateActionsImmediately()
                }
            }
        }
        toolbar.targetComponent = targetComponent
        toolbar.setReservePlaceAutoPopupIcon(false)
        return toolbar
    }
    
    private fun showOrUpdateLocation(hint: LightweightHint) {
        HintManagerImpl.getInstanceImpl().showEditorHint(
            hint,
            editor,
            getHintPosition(hint),
            HintManager.HIDE_BY_ESCAPE or HintManager.UPDATE_BY_SCROLLING,
            0,
            true
        )
    }
    
    private fun registerListeners() {
        editor.addEditorMouseListener(mouseListener)
        editor.addEditorMouseMotionListener(mouseMotionListener)
        editor.contentComponent.addKeyListener(keyboardListener)
    }
    
    private fun unregisterListeners() {
        editor.removeEditorMouseListener(mouseListener)
        editor.removeEditorMouseMotionListener(mouseMotionListener)
        editor.contentComponent.removeKeyListener(keyboardListener)
    }
    
    private fun canBeShownAtCurrentSelection(): Boolean {
        val file = PsiEditorUtil.getPsiFile(editor)
        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
        val selectionModel = editor.selectionModel
        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd
        //忽略没有选择文本的情况
        if(selectionStart == selectionEnd) return false
        //忽略跨行的情况
        if(editor.document.getLineNumber(selectionStart) != editor.document.getLineNumber(selectionEnd)) return false
        val elementAtStart = file.findElementAt(selectionStart)
        val elementAtEnd = file.findElementAt(selectionEnd - 1)
        //开始位置和结束位置的左边或右边是STRING_TOKEN，向上能查找到同一个ParadoxLocalisationPropertyValue，且选择文本的范围在引号之间
        if(elementAtStart == null || elementAtEnd == null) return false
        if(elementAtStart.elementType != STRING_TOKEN && elementAtStart.prevLeaf(false).elementType != STRING_TOKEN) return false
        if(elementAtEnd.elementType != STRING_TOKEN && elementAtEnd.nextLeaf(false).elementType != STRING_TOKEN) return false
        val propertyValueAtStart = elementAtStart.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        val propertyValueAtEnd = elementAtEnd.parentOfType<ParadoxLocalisationPropertyValue>() ?: return false
        if(propertyValueAtStart !== propertyValueAtEnd) return false
        val propertyValue = propertyValueAtStart
        val textRange = propertyValue.textRange
        val start = if(propertyValue.firstChild.elementType == LEFT_QUOTE) textRange.startOffset + 1 else textRange.startOffset
        val end = if(propertyValue.lastChild.elementType == RIGHT_QUOTE) textRange.endOffset - 1 else textRange.endOffset
        return selectionStart >= start && selectionEnd <= end
    }
    
    private fun getHintPosition(hint: LightweightHint): Point {
        val hintPos = HintManagerImpl.getInstanceImpl().getHintPosition(hint, editor, HintManager.DEFAULT)
        // because of `hint.setForceShowAsPopup(true)`, HintManager.ABOVE does not place the hint above
        // the hint remains on the line, so we need to move it up ourselves
        val dy = -(hint.component.preferredSize.height + 2)
        val dx = buttonSize * -2
        hintPos.translate(dx, dy)
        return hintPos
    }
    
    private fun updateOnProbablyChangedSelection(onSelectionChanged: (String) -> Unit) {
        val newSelection = editor.selectionModel.selectedText
        
        when(newSelection) {
            null -> hideIfShown()
            lastSelection -> Unit
            else -> onSelectionChanged(newSelection)
        }
        
        lastSelection = newSelection
    }
    
    private inner class MouseListener : EditorMouseListener {
        override fun mouseReleased(e: EditorMouseEvent) {
            //仅当文档可编辑时才进行处理
            if(editor.document.isWritable) {
                updateOnProbablyChangedSelection {
                    if(isShown()) {
                        updateLocationIfShown()
                    } else {
                        showIfHidden()
                    }
                }
            }
        }
    }
    
    private inner class KeyboardListener : KeyAdapter() {
        override fun keyReleased(e: KeyEvent) {
            //仅当文档可编辑时才进行处理
            if(editor.document.isWritable) {
                super.keyReleased(e)
                if(e.source != editor.contentComponent) {
                    return
                }
                updateOnProbablyChangedSelection {
                    hideIfShown()
                }
            }
        }
    }
    
    private inner class MouseMotionListener : EditorMouseMotionListener {
        override fun mouseMoved(e: EditorMouseEvent) {
            //仅当文档可编辑时才进行处理
            if(editor.document.isWritable) {
                val visualPosition = e.visualPosition
                val hoverSelected = editor.caretModel.allCarets.any {
                    val beforeSelectionEnd = it.selectionEndPosition.after(visualPosition)
                    val afterSelectionStart = visualPosition.after(it.selectionStartPosition)
                    beforeSelectionEnd && afterSelectionStart
                }
                if(hoverSelected) {
                    showIfHidden()
                }
            }
        }
    }
    
    companion object {
        const val EDITOR_FLOATING_TOOLBAR = "ParadoxLocalisationEditorFloatingToolbar"
    }
}