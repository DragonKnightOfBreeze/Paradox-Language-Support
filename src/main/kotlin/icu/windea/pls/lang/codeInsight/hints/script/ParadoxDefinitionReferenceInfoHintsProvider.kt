@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.SequencePresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxDefinitionReferenceInfoHintsProvider.Settings
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import javax.swing.JComponent

/**
 * 通过内嵌提示显示定义引用信息，包括名称、类型和子类型。
 */
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var showSubtypes: Boolean = true
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxDefinitionReferenceInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.definitionReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.definitionReferenceInfo.description")
    override val key: SettingsKey<Settings> get() = settingsKey

    override fun createSettings() = Settings()

    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                createTypeInfoRow(settings::showSubtypes)
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
            val definitionInfo = resolved.definitionInfo
            if (definitionInfo != null) {
                val presentation = doCollect(definitionInfo, settings)
                val finalPresentation = presentation.toFinalPresentation(this, file.project)
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }

    private fun PresentationFactory.doCollect(definitionInfo: ParadoxDefinitionInfo, settings: Settings): InlayPresentation {
        val presentations: MutableList<InlayPresentation> = mutableListOf()
        //省略definitionName
        presentations.add(smallText(": "))
        val typeConfig = definitionInfo.typeConfig
        presentations.add(psiSingleReference(smallText(typeConfig.name)) { typeConfig.pointer.element })
        if (settings.showSubtypes) {
            val subtypeConfigs = definitionInfo.subtypeConfigs
            for (subtypeConfig in subtypeConfigs) {
                presentations.add(smallText(", "))
                presentations.add(psiSingleReference(smallText(subtypeConfig.name)) { subtypeConfig.pointer.element })
            }
        }
        return SequencePresentation(presentations)
    }
}
