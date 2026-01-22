package icu.windea.pls.inject.injectors

import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import icu.windea.pls.core.memberProperty
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import java.awt.Graphics2D

/**
 * @see com.intellij.codeInsight.hints.presentation.WithAttributesPresentation
 * @see com.intellij.codeInsight.hints.presentation.WithAttributesPresentation.paint
 */
@Suppress("UnstableApiUsage")
@InjectionTarget("com.intellij.codeInsight.hints.presentation.WithAttributesPresentation")
class WithAttributesPresentationCodeInjector : CodeInjectorBase() {
    // 如果 `other` 是 `INLAY_DEFAULT`（或者 `INLAY_TEXT_WITHOUT_BACKGROUND`），应当直优先使用 `attributes` 进行渲染
    // 否则，`PresentationFactory.withReferenceAttributes` 等会无法正常生效

    private val Any.presentation: InlayPresentation by memberProperty("presentation", null)
    private val Any.textAttributesKey: TextAttributesKey by memberProperty("textAttributesKey", null)
    private val Any.colorsScheme: EditorColorsScheme by memberProperty("colorsScheme", null)

    @Suppress("unused")
    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE)
    fun Any.paint(g: Graphics2D, attributes: TextAttributes) {
        run {
            if (!attributes.checkAttribtues()) return@run
            if (!textAttributesKey.checkAttributesKey()) return@run
            val other = colorsScheme.getAttributes(textAttributesKey) ?: TextAttributes()
            val result = attributes.clone()
            if (result.foregroundColor == null) {
                result.foregroundColor = other.foregroundColor
            }
            if (result.backgroundColor == null) {
                result.backgroundColor = other.backgroundColor
            }
            if (result.effectType == null) {
                result.effectType = other.effectType
            }
            if (result.effectColor == null) {
                result.effectColor = other.effectColor
            }
            presentation.paint(g, result) // = `super.paint(g, result)`
            return
        }
        continueInvocation()
    }

    private fun TextAttributes.checkAttribtues(): Boolean {
        return foregroundColor != null || backgroundColor != null || effectType != null || effectColor != null
    }

    private fun TextAttributesKey.checkAttributesKey(): Boolean {
        return this == INLAY_DEFAULT || this == INLAY_TEXT_WITHOUT_BACKGROUND
    }
}
