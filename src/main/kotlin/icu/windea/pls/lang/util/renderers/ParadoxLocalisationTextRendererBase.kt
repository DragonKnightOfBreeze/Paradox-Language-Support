package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationRichText
import icu.windea.pls.localisation.psi.ParadoxLocalisationString
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import java.awt.Color

abstract class ParadoxLocalisationTextRendererBase<C, R> : ParadoxLocalisationTextRenderer<C, R> {
    final override fun render(input: PsiElement, context: C): R {
        return when (input) {
            is ParadoxLocalisationProperty -> render(input, context)
            is ParadoxLocalisationRichText -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxLocalisationProperty, context: C = initContext()): R {
        with(context) { renderRootProperty(element) }
        return getOutput(context)
    }

    fun render(element: List<ParadoxLocalisationRichText>, context: C = initContext()): R {
        with(context) { renderRootRichTexts(element) }
        return getOutput(context)
    }

    fun render(element: ParadoxLocalisationRichText, context: C = initContext()): R {
        with(context) { renderRootRichText(element) }
        return getOutput(context)
    }

    protected abstract fun getOutput(context: C): R

    context(context: C)
    protected open fun renderRootProperty(element: ParadoxLocalisationProperty) {
        renderProperty(element)
    }

    context(context: C)
    protected open fun renderRootRichTexts(elements: List<ParadoxLocalisationRichText>) {
        for (element in elements) {
            ProgressManager.checkCanceled()
            renderRichText(element)
        }
    }

    context(context: C)
    protected open fun renderRootRichText(element: ParadoxLocalisationRichText) {
        renderRichText(element)
    }

    context(context: C)
    protected open fun renderRichText(element: ParadoxLocalisationRichText) {
        when (element) {
            is ParadoxLocalisationString -> renderString(element)
            is ParadoxLocalisationColorfulText -> renderColorfulText(element)
            is ParadoxLocalisationParameter -> renderParameter(element)
            is ParadoxLocalisationIcon -> renderIcon(element)
            is ParadoxLocalisationCommand -> renderCommand(element)
            is ParadoxLocalisationConceptCommand -> renderConceptCommand(element)
            is ParadoxLocalisationTextIcon -> renderTextIcon(element)
            is ParadoxLocalisationTextFormat -> renderTextFormat(element)
            else -> throw UnsupportedOperationException("Unsupported rich type element type: ${element.elementType}")
        }
    }

    context(context: C)
    protected abstract fun renderProperty(element: ParadoxLocalisationProperty)

    context(context: C)
    protected abstract fun renderString(element: ParadoxLocalisationString)

    context(context: C)
    protected abstract fun renderColorfulText(element: ParadoxLocalisationColorfulText)

    context(context: C)
    protected abstract fun renderParameter(element: ParadoxLocalisationParameter)

    context(context: C)
    protected abstract fun renderIcon(element: ParadoxLocalisationIcon)

    context(context: C)
    protected abstract fun renderCommand(element: ParadoxLocalisationCommand)

    context(context: C)
    protected abstract fun renderConceptCommand(element: ParadoxLocalisationConceptCommand)

    context(context: C)
    protected abstract fun renderTextIcon(element: ParadoxLocalisationTextIcon)

    context(context: C)
    protected abstract fun renderTextFormat(element: ParadoxLocalisationTextFormat)

    protected fun shouldRenderColurfulText(): Boolean {
        return PlsSettings.getInstance().state.others.renderLocalisationColorfulText
    }

    protected fun getGuardKey(element: PsiElement?): String? {
        return if (element is ParadoxLocalisationProperty) element.name else null
    }

    protected fun checkGuard(guardStack: ArrayDeque<String>, element: PsiElement?): Boolean {
        val key = getGuardKey(element)
        return key != null && key in guardStack
    }

    protected inline fun <R> withGuard(guardStack: ArrayDeque<String>, element: PsiElement?, action: () -> R): R {
        val key = getGuardKey(element)
        try {
            if (key != null) guardStack.addLast(key)
            return action()
        } finally {
            if (key != null) guardStack.removeLast()
        }
    }

    protected inline fun <R> withColor(colorStack: ArrayDeque<Color>, color: Color?, action: () -> R): R {
        try {
            if (color != null) colorStack.addLast(color)
            return action()
        } finally {
            if (color != null) colorStack.removeLast()
        }
    }
}
