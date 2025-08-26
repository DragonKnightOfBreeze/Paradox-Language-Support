@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.hints.csv.ParadoxCsvDefinitionReferenceInfoHintsProvider.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * C通过内嵌提示显示定义引用信息，包括名称、类型和子类型。
 */
class ParadoxCsvDefinitionReferenceInfoHintsProvider : ParadoxCsvHintsProvider<Settings>() {
    data class Settings(
        var showSubtypes: Boolean = true
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxCsvDefinitionReferenceInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("csv.hints.definitionReferenceInfo")
    override val description: String get() = PlsBundle.message("csv.hints.definitionReferenceInfo.description")
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
        if (element !is ParadoxCsvColumn) return true
        if (!ParadoxResolveConstraint.Definition.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.Definition.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved !is ParadoxScriptDefinitionElement) return true
        val presentation = doCollect(resolved, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxScriptDefinitionElement, settings: Settings): InlayPresentation? {
        val definitionInfo = element.definitionInfo ?: return null
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
