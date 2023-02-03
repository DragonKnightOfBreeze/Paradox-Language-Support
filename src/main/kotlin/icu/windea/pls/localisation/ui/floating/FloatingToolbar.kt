package icu.windea.pls.localisation.ui.floating

import com.intellij.codeInsight.hint.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.fileEditor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.ui.actions.styling.*
import java.awt.*
import java.awt.event.*
import javax.swing.*
import kotlin.properties.*

//org.intellij.plugins.markdown.ui.floating.FloatingToolbar

/**
 * 当用户光标选中本地化文本(的其中一部分)时,将会显示的悬浮工具栏栏。
 * 提供动作：
 * * 更改文本颜色（所有可用的，基于内置规则文件）
 * @see icu.windea.pls.localisation.ui.actions.styling.FloatingToolbarGroup
 * @see icu.windea.pls.localisation.ui.actions.styling.SetColorAction
 */
class FloatingToolbar(val textEditor: TextEditor) : Disposable {
	private val mouseListener = MouseListener()
	private val keyboardListener = KeyboardListener()
	private val mouseMotionListener = MouseMotionListener()
	
	private var hint: LightweightHint? = null
	private var buttonSize: Int by Delegates.notNull()
	private var lastSelection: String? = null
	
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
		val toolbar = createActionToolbar(textEditor.editor.contentComponent)
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
	
	@Suppress("UnstableApiUsage")
	private fun createActionToolbar(targetComponent: JComponent): ActionToolbar {
		PlsThreadLocals.threadLocalTextEditorContainer.set(textEditor)
		val group = FloatingToolbarGroup()
		val toolbar = object : ActionToolbarImpl(ActionPlaces.EDITOR_TOOLBAR, group, true) {
			override fun addNotify() {
				super.addNotify()
				updateActionsImmediately(true) //这是必要的，否则显示悬浮工具栏时其中的图标不会立即全部显示
			}
		}
		toolbar.targetComponent = targetComponent
		toolbar.setReservePlaceAutoPopupIcon(false)
		return toolbar
	}
	
	private fun showOrUpdateLocation(hint: LightweightHint) {
		HintManagerImpl.getInstanceImpl().showEditorHint(
			hint,
			textEditor.editor,
			getHintPosition(hint),
			HintManager.HIDE_BY_ESCAPE or HintManager.UPDATE_BY_SCROLLING,
			0,
			true
		)
	}
	
	private fun registerListeners() {
		textEditor.editor.addEditorMouseListener(mouseListener)
		textEditor.editor.addEditorMouseMotionListener(mouseMotionListener)
		textEditor.editor.contentComponent.addKeyListener(keyboardListener)
	}
	
	private fun unregisterListeners() {
		textEditor.editor.removeEditorMouseListener(mouseListener)
		textEditor.editor.removeEditorMouseMotionListener(mouseMotionListener)
		textEditor.editor.contentComponent.removeKeyListener(keyboardListener)
	}
	
	private fun canBeShownAtCurrentSelection(): Boolean {
		val file = PsiEditorUtil.getPsiFile(textEditor.editor)
		PsiDocumentManager.getInstance(file.project).commitDocument(textEditor.editor.document)
		val selectionModel = textEditor.editor.selectionModel
		val selectionStart = selectionModel.selectionStart
		val selectionEnd = selectionModel.selectionEnd
		//忽略没有选择文本的情况
		if(selectionStart == selectionEnd) return false
		//忽略跨行的情况
		if(textEditor.editor.document.getLineNumber(selectionStart) != textEditor.editor.document.getLineNumber(selectionEnd)) return false
		val elementAtStart = PsiUtilCore.getElementAtOffset(file, selectionStart)
		val elementAtEnd = PsiUtilCore.getElementAtOffset(file, selectionEnd - 1)
		//开始位置和结束位置（之前）的PSI元素类型必须是string_token
		return elementAtStart.elementType.let { it == STRING_TOKEN || it == COLORFUL_TEXT_START || it == COLOR_ID }
			&& elementAtEnd.elementType.let { it == STRING_TOKEN || it == COLORFUL_TEXT_END }
	}
	
	private fun getHintPosition(hint: LightweightHint): Point {
		val hintPos = HintManagerImpl.getInstanceImpl().getHintPosition(hint, textEditor.editor, HintManager.DEFAULT)
		// because of `hint.setForceShowAsPopup(true)`, HintManager.ABOVE does not place the hint above
		// the hint remains on the line, so we need to move it up ourselves
		val dy = -(hint.component.preferredSize.height + 2)
		val dx = buttonSize * -2
		hintPos.translate(dx, dy)
		return hintPos
	}
	
	private fun updateOnProbablyChangedSelection(onSelectionChanged: (String) -> Unit) {
		val newSelection = textEditor.editor.selectionModel.selectedText
		
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
			if(textEditor.editor.document.isWritable) {
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
			if(textEditor.editor.document.isWritable) {
				super.keyReleased(e)
				if(e.source != textEditor.editor.contentComponent) {
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
			if(textEditor.editor.document.isWritable) {
				val visualPosition = e.visualPosition
				val hoverSelected = textEditor.editor.caretModel.allCarets.any {
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
}