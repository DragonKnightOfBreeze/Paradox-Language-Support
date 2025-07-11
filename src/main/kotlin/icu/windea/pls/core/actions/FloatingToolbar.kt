package icu.windea.pls.core.actions

import com.intellij.codeInsight.hint.*
import com.intellij.ide.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.util.concurrency.annotations.*
import com.intellij.util.ui.components.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import java.awt.*
import java.awt.event.*
import javax.swing.*
import kotlin.coroutines.*
import kotlin.properties.*
import kotlin.time.Duration.Companion.milliseconds

//com.intellij.openapi.actionSystem.impl.FloatingToolbar
//forked as the original class is marked as @Internal

@OptIn(FlowPreview::class)
abstract class FloatingToolbar(
    val editor: Editor,
    /**
   * This scope will be canceled on dispose.
   */
  protected val coroutineScope: CoroutineScope
): Disposable {
  protected var hint: LightweightHint? = null
  private var buttonSize: Int by Delegates.notNull()
  private var lastSelection: String? = null

  /**
   * Prevents toolbar to be shown again if it was already recently closed.
   * At least mouse should be moved out of the selection first.
   */
  private var preventHintFromShowing = false

  private enum class HintRequest {
    Show,
    Hide
  }

  private val hintRequests = MutableSharedFlow<HintRequest>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  init {
    coroutineScope.launch {
      hintRequests.debounce(50.milliseconds).collectLatest { request ->
          withContext(Dispatchers.EDT) {
              when (request) {
                  HintRequest.Show -> showIfHidden()
                  HintRequest.Hide -> hide()
              }
          }
      }
    }
    //@Suppress("LeakingThis")
    editor.apply {
      addEditorMouseListener(MouseListener(), this@FloatingToolbar)
      addEditorMouseMotionListener(MouseMotionListener(), this@FloatingToolbar)
      contentComponent.addKeyListener(KeyboardListener(), this@FloatingToolbar)
      selectionModel.addSelectionListener(EditorSelectionListener(), this@FloatingToolbar)
      document.addDocumentListener(DocumentChangeListener(), this@FloatingToolbar)
    }
  }

  protected abstract fun createActionGroup(): ActionGroup?

  open fun hideByOtherHints(): Boolean = false

  @RequiresEdt
  fun isShown(): Boolean {
    return hint?.isVisible == true
  }

  @RequiresEdt
  private fun hide() {
    hint?.hide()
    hint = null
  }

  @RequiresEdt
  private suspend fun showIfHidden() {
    preventHintFromShowing = true
    if (isShown() || !isEnabled()) {
      return
    }
    val canBeShownAtCurrentSelection = readAction { canBeShownAtCurrentSelection() }
    if (!canBeShownAtCurrentSelection) {
      return
    }
    val hint = createHint()
    showHint(hint)
    hint.addHintListener {
      this.hint = null
    }
    this.hint = hint
  }

  fun show(callback: Runnable){
    coroutineScope.launch {
        withContext(Dispatchers.EDT) {
            showIfHidden()
            callback.run()
        }
    }
  }

  protected open suspend fun createHint(): LightweightHint {
    val toolbar = createUpdatedActionToolbar(editor.contentComponent)
    val component = BorderLayoutPanel().apply {
      addToCenter(toolbar.component)
    }
    val hint = LightweightHint(component).apply {
      setForceShowAsPopup(true)
    }
    return hint
  }

  fun scheduleShow() {
    if (isEnabled() && !preventHintFromShowing) {
      check(hintRequests.tryEmit(HintRequest.Show))
    }
  }

  fun scheduleHide() {
    check(hintRequests.tryEmit(HintRequest.Hide))
  }

  @RequiresEdt
  private fun updateLocationIfShown() {
    hint?.let(::showHint)
  }

  override fun dispose() {
    coroutineScope.cancel()
    hide()
  }

  private suspend fun createUpdatedActionToolbar(targetComponent: JComponent): ActionToolbar {
    return suspendCancellableCoroutine { continuation ->
        createActionToolbar(targetComponent) {
            if (!continuation.isCompleted) {
                continuation.resume(it)
            }
        }
    }
  }

  @Suppress("UnstableApiUsage")
  private fun createActionToolbar(targetComponent: JComponent, onUpdated: (ActionToolbar) -> Unit) {
    val group = createActionGroup() ?: return
    val place = ActionPlaces.EDITOR_FLOATING_TOOLBAR
    val toolbar = ToolbarUtils.createImmediatelyUpdatedToolbar(
        group,
        place,
        targetComponent,
        horizontal = true,
        onUpdated
    )
    buttonSize = toolbar.maxButtonHeight
  }

  open fun onHintShown() {}

  private fun showHint(hint: LightweightHint) {
    val hideByOtherHintsMask = when {
      hideByOtherHints() -> HintManager.HIDE_BY_OTHER_HINT
      else -> 0
    }
    HintManagerImpl.getInstanceImpl().showEditorHint(
      hint,
      editor,
      getHintPosition(hint),
      HintManager.HIDE_BY_ESCAPE or HintManager.UPDATE_BY_SCROLLING or hideByOtherHintsMask,
      0,
      true
    )
    onHintShown()
  }

  @RequiresReadLock
  abstract fun canBeShownAtCurrentSelection(): Boolean

  /**
   * Allow toolbar to be shown immediately even it was already closed before.
   * @see [preventHintFromShowing]
   */
  fun allowInstantShowing(){
    preventHintFromShowing = false
  }

  @RequiresReadLock
  protected open fun hasIgnoredParent(element: PsiElement): Boolean {
    return false
  }

  protected open fun disableForDoubleClickSelection(): Boolean {
    return false
  }

  protected open fun shouldSurviveDocumentChange(): Boolean = true

  open fun isEnabled(): Boolean {
    return true
  }

  protected open fun getHintPosition(hint: LightweightHint): Point {
    val hintPos = HintManagerImpl.getInstanceImpl().getHintPosition(hint, editor, HintManager.DEFAULT)
    // because of `hint.setForceShowAsPopup(true)`, HintManager.ABOVE does not place the hint above
    // the hint remains on the line, so we need to move it up ourselves
    val verticalGap = 2
    val dy = -(hint.component.preferredSize.height + verticalGap)
    val dx = buttonSize * -2
    hintPos.translate(dx, dy)
    return hintPos
  }

  private fun updateOnProbablyChangedSelection(onSelectionChanged: (String) -> Unit) {
    val newSelection = editor.selectionModel.selectedText
    when (newSelection) {
      null -> scheduleHide()
      lastSelection -> Unit
      else -> onSelectionChanged(newSelection)
    }
    lastSelection = newSelection
  }

  private inner class MouseListener : EditorMouseListener {
    override fun mouseReleased(event: EditorMouseEvent) {
      updateOnProbablyChangedSelection {
        if (isShown()) {
          updateLocationIfShown()
        } else {
          scheduleShow()
        }
      }
    }
  }

  private inner class KeyboardListener : KeyAdapter() {
    override fun keyReleased(event: KeyEvent) {
      super.keyReleased(event)
      if (event.source != editor.contentComponent) {
        return
      }
      updateOnProbablyChangedSelection {
        scheduleHide()
      }
    }
  }

  private inner class MouseMotionListener : EditorMouseMotionListener {
    override fun mouseMoved(event: EditorMouseEvent) {
      val visualPosition = event.visualPosition
      val hoverSelected = editor.caretModel.allCarets.any { visualPosition.isInsideSelection(it) }
      if (hoverSelected) {
        scheduleShow()
      } else if (!isShown()){
        preventHintFromShowing = false
      }
    }

    private fun VisualPosition.isInsideSelection(caret: Caret): Boolean {
      val beforeSelectionEnd = caret.selectionEndPosition.after(this)
      val afterSelectionStart = this.after(caret.selectionStartPosition)
      return beforeSelectionEnd && afterSelectionStart
    }
  }

  private inner class EditorSelectionListener : SelectionListener {
    override fun selectionChanged(event: SelectionEvent) {
      preventHintFromShowing = false
      if (isIgnoredEvent(IdeEventQueue.getInstance().trueCurrentEvent)) {
        preventHintFromShowing = true
      }
    }

    private fun isIgnoredEvent(event: AWTEvent): Boolean {
      return disableForDoubleClickSelection() && (event as? MouseEvent)?.clickCount == 2
    }
  }

  private inner class DocumentChangeListener : BulkAwareDocumentListener {
    override fun documentChanged(event: DocumentEvent) {
      preventHintFromShowing = false
      if (!shouldSurviveDocumentChange()) {
        scheduleHide()
      }
    }
  }

  companion object {
    private fun JComponent.addKeyListener(listener: KeyListener, parentDisposable: Disposable) {
      addKeyListener(listener)
        Disposer.register(parentDisposable) {
            removeKeyListener(listener)
        }
    }
  }
}
