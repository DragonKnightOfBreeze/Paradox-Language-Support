package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 通过内嵌提示显示定义引用信息，包括名称、类型和子类型。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.definitionReferenceInfo")

    override val name: String get() = PlsBundle.message("script.hints.definitionReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.definitionReferenceInfo.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val showTypeInfo: Boolean get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        if (!ParadoxResolveConstraint.Definition.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.Definition.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved is ParadoxScriptDefinitionElement) {
            val definitionInfo = resolved.definitionInfo ?: return true
            val presentation = collect(definitionInfo)
            val finalPresentation = presentation?.toFinalPresentation() ?: return true
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    context(context: ParadoxHintsContext)
    private fun collect(definitionInfo: ParadoxDefinitionInfo): InlayPresentation? {
        val presentations: MutableList<InlayPresentation> = mutableListOf()
        // 省略定义名
        presentations.add(context.factory.smallText(": "))
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
