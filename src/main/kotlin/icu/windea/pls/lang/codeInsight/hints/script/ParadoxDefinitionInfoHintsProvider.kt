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
import icu.windea.pls.config.config.isBlock
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxDefinitionInfoHintsProvider.Settings
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import javax.swing.JComponent

/**
 * 通过内嵌提示显示定义信息，包括名称、类型和子类型。
 */
class ParadoxDefinitionInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var showSubtypes: Boolean = true
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxDefinitionInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.definitionInfo")
    override val description: String get() = PlsBundle.message("script.hints.definitionInfo.description")
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
        if (element is ParadoxScriptProperty) {
            val definitionInfo = element.definitionInfo
            if (definitionInfo != null) {
                //忽略类似event_namespace这样定义的值不是子句的定义
                if (definitionInfo.declarationConfig?.config?.isBlock == false) return true

                val presentation = doCollect(definitionInfo, settings)
                val finalPresentation = presentation.toFinalPresentation(this, file.project)
                val endOffset = element.propertyKey.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }

    private fun PresentationFactory.doCollect(definitionInfo: ParadoxDefinitionInfo, settings: Settings): InlayPresentation {
        val presentations: MutableList<InlayPresentation> = mutableListOf()
        val name = definitionInfo.name
        //如果definitionName和rootKey相同，则省略definitionName
        if (name.equals(definitionInfo.rootKey, true)) {
            presentations.add(smallText(": "))
        } else {
            presentations.add(smallText("$name: "))
        }
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
