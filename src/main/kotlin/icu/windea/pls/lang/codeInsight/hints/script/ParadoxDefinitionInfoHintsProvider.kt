package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
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

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        if (element is ParadoxScriptProperty) {
            val definitionInfo = element.definitionInfo
            if (definitionInfo == null) return true
            // 忽略类似 event_namespace 这样的定义的值不是子句的定义
            if (definitionInfo.declarationConfig?.config?.let { it.valueType == CwtType.Block } == false) return true

            val presentation = collect(definitionInfo)
            val finalPresentation = presentation?.toFinalPresentation() ?: return true
            val endOffset = element.propertyKey.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    context(context: ParadoxHintsContext)
    private fun collect(definitionInfo: ParadoxDefinitionInfo): InlayPresentation? {
        val presentations: MutableList<InlayPresentation> = mutableListOf()
        val name = definitionInfo.name
        // 如果定义名等同于类型键，则省略定义名
        if (name.equals(definitionInfo.typeKey, true)) {
            presentations.add(context.factory.smallText(": "))
        } else {
            presentations.add(context.factory.smallText("$name: ".optimized()))
        }
        val typeConfig = definitionInfo.typeConfig
        presentations.add(context.factory.psiSingleReference(context.factory.smallText(typeConfig.name)) { typeConfig.pointer.element })
        if (context.settings.showSubtypes) {
            val subtypeConfigs = definitionInfo.subtypeConfigs
            for (subtypeConfig in subtypeConfigs) {
                presentations.add(context.factory.smallText(", "))
                presentations.add(context.factory.psiSingleReference(context.factory.smallText(subtypeConfig.name)) { subtypeConfig.pointer.element })
            }
        }
        return presentations.mergePresentations()
    }
}
