package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

/**
 * 通过内嵌提示显示渲染后的本地化文本，适用于引用的本地化。默认不启用。
 *
 * 如果本地化文本过长则会先被截断。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationReferenceHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.localisationReferenceInof")

    override val name: String get() = PlsBundle.message("script.hints.localisationReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.localisationReferenceInfo.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (!ParadoxResolveConstraint.LocalisationReference.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.LocalisationReference.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved is ParadoxLocalisationProperty) {
            val localisationType = resolved.type
            if (localisationType == null) return true
            val presentation = collect(resolved, editor, settings)
            val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun PresentationFactory.collect(localisation: ParadoxLocalisationProperty, editor: Editor, settings: ParadoxHintsSettings): InlayPresentation? {
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(localisation)
    }
}
