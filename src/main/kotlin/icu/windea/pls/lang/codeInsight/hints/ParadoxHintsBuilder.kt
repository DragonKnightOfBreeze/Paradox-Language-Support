package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.MenuOnClickPresentation
import com.intellij.codeInsight.hints.presentation.ScaleAwarePresentationFactory
import com.intellij.codeInsight.hints.presentation.SequencePresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.core.optimized
import javax.swing.Icon

@Suppress("UnstableApiUsage")
class ParadoxHintsBuilder {
    private val presentations = mutableListOf<InlayPresentation>()

    fun clear() {
        presentations.clear()
    }

    fun add(presentation: InlayPresentation) {
        presentations.add(presentation)
    }

    fun merged(): InlayPresentation? {
        return when {
            presentations.isEmpty() -> null
            presentations.size == 1 -> presentations.first()
            else -> SequencePresentation(presentations.optimized())
        }
    }

    /**
     * 将内嵌提示处理为最终要显示的内嵌注释（加上背景、偏移、默认点击操作等）。
     */
    context(provider: ParadoxHintsProvider, context: ParadoxHintsContext)
    fun build(smallInset: Boolean = false): InlayPresentation? {
        // 最终显示的内嵌提示的格式是经过一定调整的
        // com.intellij.docker.dockerFile.inlay.DockerInlayHintsProvider.MyCollector.buildPresentation
        // com.intellij.microservices.jvm.dependencies.FrameworkDependenciesInlayHintsCollectorBase.buildPresentation

        val basePresentation = merged() ?: return null
        val factory = context.factory
        val scaledFactory = ScaleAwarePresentationFactory(context.editor, factory)
        val scaledPresentation = basePresentation
            .let { scaledFactory.inset(it, 0, 0, 4, 1) }
            .let { if (smallInset) factory.roundWithBackgroundAndSmallInset(it) else factory.roundWithBackground(it) }
            .let { scaledFactory.inset(it, 1, 1, -3, 0) }
        val finalPresentation = MenuOnClickPresentation(scaledPresentation, context.project) {
            InlayHintsUtils.getDefaultInlayHintsProviderPopupActions(provider.key) { provider.name }
        }
        return finalPresentation
    }

    context(context: ParadoxHintsContext)
    fun text(text: String) {
        val presentation = context.factory.smallText(text)
        presentations.add(presentation)
    }

    context(context: ParadoxHintsContext)
    fun text(text: String, pointer: SmartPsiElementPointer<out PsiElement>?) {
        val presentation = context.factory.smallText(text)
            .let { context.factory.psiSingleReference(it) { pointer?.element } }
        presentations.add(presentation)
    }

    context(context: ParadoxHintsContext)
    fun icon(icon: Icon) {
        val presentation = context.factory.smallScaledIcon(icon)
        presentations.add(presentation)
    }

    context(context: ParadoxHintsContext)
    fun icon(icon: Icon, pointer: SmartPsiElementPointer<out PsiElement>?) {
        val presentation = context.factory.smallScaledIcon(icon)
            .let { context.factory.psiSingleReference(it) { pointer?.element } }
        presentations.add(presentation)
    }
}
