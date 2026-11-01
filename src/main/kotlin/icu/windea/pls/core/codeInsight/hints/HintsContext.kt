@file:Suppress("UnstableApiUsage")

package icu.windea.pls.core.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiFile

/**
 * 内嵌提示的通用上下文。
 *
 * 可以通过 userData 附加额外的数据，例如图片渲染请求。
 *
 * @see InlayHintsProvider
 */
data class HintsContext<T: Any>(
    val file: PsiFile,
    val editor: Editor,
    val settings: T,
    val sink: InlayHintsSink,
    val factory: PresentationFactory,
) : UserDataHolderBase() {
    val project: Project get() = file.project
}
