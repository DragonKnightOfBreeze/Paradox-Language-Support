package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.MenuOnClickPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import javax.swing.Icon

@Suppress("UnstableApiUsage")
class ParadoxHintsBuilder {
    val presentations = mutableListOf<InlayPresentation>()

    context(provider: ParadoxHintsProvider, context: ParadoxHintsContext)
    fun build(smaller: Boolean = false): InlayPresentation? {
        // 将内嵌提示处理为最终要显示的内嵌注释（加上背景、左偏移、默认点击操作等）
        val basePresentation = presentations.mergePresentations() ?: return null
        val roundedPresentation = when {
            smaller -> context.factory.roundWithBackgroundAndSmallInset(basePresentation)
            else -> context.factory.roundWithBackground(basePresentation)
        }
        return MenuOnClickPresentation(roundedPresentation, context.project) {
            InlayHintsUtils.getDefaultInlayHintsProviderPopupActions(provider.key) { provider.name }
        }
    }

    context(context: ParadoxHintsContext)
    fun ParadoxHintsBuilder.text(text: String) {
        val presentation = context.factory.smallText(text)
        presentations.add(presentation)
    }

    context(context: ParadoxHintsContext)
    fun ParadoxHintsBuilder.text(text: String, pointer: SmartPsiElementPointer<out PsiElement>?) {
        val presentation = context.factory.psiSingleReference(context.factory.smallText(text)) { pointer?.element }
        presentations.add(presentation)
    }

    context(context: ParadoxHintsContext)
    fun ParadoxHintsBuilder.icon(icon: Icon) {
        val presentation = context.factory.smallScaledIcon(icon)
        presentations.add(presentation)
    }

    context(context: ParadoxHintsContext)
    fun ParadoxHintsBuilder.icon(icon: Icon, pointer: SmartPsiElementPointer<out PsiElement>?) {
        val presentation = context.factory.psiSingleReference(context.factory.smallScaledIcon(icon)) { pointer?.element }
        presentations.add(presentation)
    }
}
