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
import icu.windea.pls.localisation.psi.ParadoxLocalisationText
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import java.awt.Color

/**
 * 本地化文本的渲染器。
 */
abstract class ParadoxLocalisationTextRenderer<T, C : ParadoxLocalisationTextRenderContext<T>, S : ParadoxLocalisationTextRenderSettings> : ParadoxRenderer<T, C, S> {
    final override fun render(input: PsiElement, context: C): T {
        return when (input) {
            is ParadoxLocalisationProperty -> render(input, context)
            is ParadoxLocalisationRichText -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxLocalisationProperty, context: C = createContext()): T {
        context.renderRootProperty(element)
        return context.build()
    }

    fun render(element: List<ParadoxLocalisationRichText>, context: C = createContext()): T {
        context.renderRootRichTexts(element)
        return context.build()
    }

    fun render(element: ParadoxLocalisationRichText, context: C = createContext()): T {
        context.renderRootRichText(element)
        return context.build()
    }
}

abstract class ParadoxLocalisationTextRenderContext<R> : ParadoxRenderContext<R> {
    abstract fun renderRootProperty(element: ParadoxLocalisationProperty)

    open fun renderRootRichTexts(elements: List<ParadoxLocalisationRichText>) {
        renderRichTexts(elements)
    }

    open fun renderRootRichText(element: ParadoxLocalisationRichText) {
        renderRichText(element)
    }

    open fun renderRichTexts(elements: List<ParadoxLocalisationRichText>) {
        if (elements.isEmpty()) return
        for (element in elements) {
            ProgressManager.checkCanceled()
            renderRichText(element)
        }
    }

    open fun renderRichText(element: ParadoxLocalisationRichText) {
        when (element) {
            is ParadoxLocalisationText -> renderText(element)
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

    abstract fun renderText(element: ParadoxLocalisationText)

    abstract fun renderColorfulText(element: ParadoxLocalisationColorfulText)

    abstract fun renderParameter(element: ParadoxLocalisationParameter)

    abstract fun renderIcon(element: ParadoxLocalisationIcon)

    abstract fun renderCommand(element: ParadoxLocalisationCommand)

    abstract fun renderConceptCommand(element: ParadoxLocalisationConceptCommand)

    abstract fun renderTextIcon(element: ParadoxLocalisationTextIcon)

    abstract fun renderTextFormat(element: ParadoxLocalisationTextFormat)

    fun shouldRenderColorfulText(): Boolean {
        return PlsSettings.getInstance().state.others.renderLocalisationColorfulText
    }

    fun getGuardKey(element: PsiElement?): String? {
        return if (element is ParadoxLocalisationProperty) element.name else null
    }

    fun checkGuard(guardStack: ArrayDeque<String>, element: PsiElement?): Boolean {
        val key = getGuardKey(element)
        return key != null && key in guardStack
    }

    inline fun <R> withGuard(guardStack: ArrayDeque<String>, element: PsiElement?, action: () -> R): R {
        val key = getGuardKey(element)
        try {
            if (key != null) guardStack.addLast(key)
            return action()
        } finally {
            if (key != null) guardStack.removeLast()
        }
    }

    inline fun <R> withColor(colorStack: ArrayDeque<Color>, color: Color?, action: () -> R): R {
        try {
            if (color != null) colorStack.addLast(color)
            return action()
        } finally {
            if (color != null) colorStack.removeLast()
        }
    }
}

abstract class ParadoxLocalisationTextRenderSettings : ParadoxRenderSettings
