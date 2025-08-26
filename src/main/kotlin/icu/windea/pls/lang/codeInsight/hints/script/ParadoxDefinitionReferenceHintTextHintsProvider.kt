@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.ep.codeInsight.hints.*
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxDefinitionReferenceHintTextHintsProvider.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 通过内嵌提示显示定义引用的提示文本。
 * 来自本地化后的名字（即最相关的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.Definition
 */
class ParadoxDefinitionReferenceHintTextHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimit,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxDefinitionReferenceLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.definitionReferenceHintText")
    override val description: String get() = PlsBundle.message("script.hints.definitionReferenceHintText.description")
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
        if (element !is ParadoxScriptExpressionElement) return true
        if (!ParadoxResolveConstraint.Definition.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.Definition.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved is ParadoxScriptDefinitionElement) {
            val presentation = doCollect(resolved, editor, settings) ?: return true
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxScriptDefinitionElement, editor: Editor, settings: Settings): InlayPresentation? {
        val primaryLocalisation = ParadoxDefinitionManager.getPrimaryLocalisation(element) ?: return null
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(primaryLocalisation)
    }
}
