package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.StatefulPresentation
import com.intellij.openapi.application.EDT
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
) : StatefulPresentation<Icon?>(null, stateMark) {
    companion object {
        @Suppress("RedundantQualifierName")
        private val stateMark = StateMark<Icon?>("chronicle.asyncIcon")
    }

    init {
        val session = ImageRenderContext.getOrCreateSession(context, ChronicleFacade.getCoroutineScope(context.project))
        val timeout = ChronicleImageSettings.getInstance().state.renderTimeoutSeconds.coerceAtLeast(1).seconds
        session.scope.launch {
            val icon = renderImageIcon(timeout) ?: return@launch
            if (context.project.isDisposed) return@launch
            withContext(Dispatchers.EDT) {
                if (context.project.isDisposed) return@withContext
                state = icon
            }
        }
    }

    private suspend fun renderImageIcon(timeout: Duration): Icon? {
        return withTimeoutOrNull(timeout) {
            withContext(Dispatchers.IO) {
                coroutineToIndicator {
                    ImageRenderService.getInstance().renderImageIcon(request, maxHeight)
                }
            }
        }
    }

    override fun toString(): String {
        return "AsyncIconPresentation(request=$request, loaded=${state != null})"
    }

    override fun getPresentation(): InlayPresentation {
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
}

