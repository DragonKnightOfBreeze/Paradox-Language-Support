package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.presentation.BasePresentation
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationListener
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.ui.UiService
import icu.windea.pls.images.ImageRenderContext
import icu.windea.pls.images.ImageRenderRequest
import icu.windea.pls.images.ImageRenderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseEvent
import javax.swing.Icon
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 异步物化图片的内嵌提示 presentation。
 *
 * 占位状态只显示文本，物化完成后在 EDT 更新为图标。
 */
@Suppress("UnstableApiUsage")
class AsyncIconPresentation(
    private val context: ParadoxHintsContext,
    private val request: ImageRenderRequest,
    private val maxHeight: Int?,
    private val pointer: SmartPsiElementPointer<out PsiElement>?,
) : BasePresentation() {
    // NOTE 3.0.5 [compatibility] 直接基于 `BasePresentation` 实现状态更新，不依赖平台内部的 `StatefulPresentation`（后者在 IDEA 2026.2 中已被标记为内部 API）

    private var state: Icon? = null
    private var currentPresentation: InlayPresentation? = null
    private var presentationListener: PresentationListener? = null

    init {
        val session = ImageRenderContext.getOrCreateSession(context, ChronicleFacade.getCoroutineScope(context.project))
        val timeout = ChronicleImageSettings.getInstance().state.renderTimeoutSeconds.coerceAtLeast(1).seconds
        session.scope.launch {
            val icon = renderImageIcon(timeout) ?: return@launch
            if (context.project.isDisposed) return@launch
            withContext(Dispatchers.EDT) {
                if (context.project.isDisposed) return@withContext
                setState(icon)
            }
        }
    }

    private suspend fun renderImageIcon(timeout: Duration): Icon? {
        return withTimeoutOrNull(timeout) {
            withContext(Dispatchers.IO) {
                coroutineToIndicator {
                    ImageRenderService.getInstance().createImageIcon(request, maxHeight)
                }
            }
        }
    }

    override val width: Int
        get() = getOrCreatePresentation().width

    override val height: Int
        get() = getOrCreatePresentation().height

    override fun paint(g: Graphics2D, attributes: TextAttributes) {
        getOrCreatePresentation().paint(g, attributes)
    }

    override fun mouseClicked(event: MouseEvent, translated: Point) {
        getOrCreatePresentation().mouseClicked(event, translated)
    }

    override fun mousePressed(event: MouseEvent, translated: Point) {
        getOrCreatePresentation().mousePressed(event, translated)
    }

    override fun mouseMoved(event: MouseEvent, translated: Point) {
        getOrCreatePresentation().mouseMoved(event, translated)
    }

    override fun mouseExited() {
        getOrCreatePresentation().mouseExited()
    }

    override fun updateState(previousPresentation: InlayPresentation): Boolean {
        if (previousPresentation !is AsyncIconPresentation) return true
        var changedState = false
        if (previousPresentation.state != state) {
            setState(previousPresentation.state)
            changedState = true
        }
        val underlyingChanged = getOrCreatePresentation().updateState(previousPresentation.getOrCreatePresentation())
        return changedState || underlyingChanged
    }

    override fun toString(): String {
        return "AsyncIconPresentation(request=$request, loaded=${state != null})"
    }

    private fun setState(icon: Icon?) {
        if (icon == state) return
        val previousDimension = Dimension(width, height)
        state = icon
        updatePresentation(createPresentation())
        val currentDimension = Dimension(width, height)
        if (previousDimension != currentDimension) {
            fireSizeChanged(previousDimension, currentDimension)
        }
        fireContentChanged(Rectangle(width, height))
    }

    private fun createPresentation(): InlayPresentation {
        val icon = state
        val base = if (icon == null) {
            context.factory.smallText("…")
        } else {
            context.factory.smallScaledIcon(icon)
        }
        if (pointer == null) return base
        return context.factory.psiSingleReference(base) { pointer.element }
            .let { context.factory.withCursorOnHoverWhenControlDown(it, UiService.getHandCursor()) }
    }

    private fun updatePresentation(presentation: InlayPresentation) {
        val previousListener = presentationListener
        if (previousListener != null) {
            currentPresentation?.removeListener(previousListener)
        }
        val listener = DelegateListener()
        presentation.addListener(listener)
        currentPresentation = presentation
        presentationListener = listener
    }

    private fun getOrCreatePresentation(): InlayPresentation {
        return currentPresentation ?: createPresentation().also { updatePresentation(it) }
    }

    private inner class DelegateListener : PresentationListener {
        override fun contentChanged(area: Rectangle) = fireContentChanged(area)

        override fun sizeChanged(previous: Dimension, current: Dimension) = fireSizeChanged(previous, current)
    }
}
