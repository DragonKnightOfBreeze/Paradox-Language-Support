@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxLocalisationReferenceHintsProvider.*
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import javax.swing.JComponent

/**
 * 通过内嵌提示显示渲染后的本地化文本，适用于引用的本地化。默认不启用。
 *
 * 如果本地化文本过长则会先被截断。
 */
class ParadoxLocalisationReferenceHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimitForInlay,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimitForInlay,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxLocalisationReferenceInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.localisationReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.localisationReferenceInfo.description")
    override val key: SettingsKey<Settings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun createSettings() = Settings()

    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                createTextLengthLimitRow(settings::textLengthLimit)
                createIconHeightLimitRow(settings::iconHeightLimit)
            }
        }
    }

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if (!ParadoxResolveConstraint.LocalisationReference.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.LocalisationReference.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved is ParadoxLocalisationProperty) {
            val localisationType = resolved.type
            if (localisationType == null) return true
            val presentation = doCollect(resolved, editor, settings)
            val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun PresentationFactory.doCollect(localisation: ParadoxLocalisationProperty, editor: Editor, settings: Settings): InlayPresentation? {
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(localisation)
    }
}
