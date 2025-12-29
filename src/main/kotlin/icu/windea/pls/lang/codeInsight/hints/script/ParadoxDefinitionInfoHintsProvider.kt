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
import icu.windea.pls.core.codeInsight.editorActions.hints.mergePresentations
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 通过内嵌提示显示定义信息，包括名称、类型和子类型。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionInfoHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.definitionInfo")

    override val name: String get() = PlsBundle.message("script.hints.definitionInfo")
    override val description: String get() = PlsBundle.message("script.hints.definitionInfo.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val showTypeInfo: Boolean get() = true

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (element is ParadoxScriptProperty) {
            val definitionInfo = element.definitionInfo
            if (definitionInfo == null) return true
            // 忽略类似 event_namespace 这样的定义的值不是子句的定义
            if (definitionInfo.declarationConfig?.config?.let { it.valueType == CwtType.Block } == false) return true

            val presentation = collect(definitionInfo, settings)
            val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
            val endOffset = element.propertyKey.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun PresentationFactory.collect(definitionInfo: ParadoxDefinitionInfo, settings: ParadoxHintsSettings): InlayPresentation? {
        val presentations: MutableList<InlayPresentation> = mutableListOf()
        val name = definitionInfo.name
        // 如果定义名等同于类型键，则省略定义名
        if (name.equals(definitionInfo.typeKey, true)) {
            presentations.add(smallText(": "))
        } else {
            presentations.add(smallText("$name: ".optimized()))
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
        return presentations.mergePresentations()
    }
}
