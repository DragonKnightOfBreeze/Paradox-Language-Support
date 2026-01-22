package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.codeInsight.hints.text
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 通过内嵌提示显示定义引用信息，包括类型和子类型。
 *
 * @see ParadoxDefinitionReferenceInfoSettingsProvider
 */
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        val resolveConstraint = ParadoxResolveConstraint.Definition
        if (!resolveConstraint.canResolveReference(element)) return
        val reference = element.reference ?: return
        if (!resolveConstraint.canResolve(reference)) return
        val resolved = reference.resolve() ?: return
        if (resolved !is ParadoxScriptDefinitionElement) return
        val definitionInfo = resolved.definitionInfo ?: return

        // 省略定义名
        val settings = ParadoxDeclarativeHintsSettings.getInstance(definitionInfo.project)
        sink.addInlinePresentation(element.endOffset, priority = 1) {
            text(": ")
            val typeConfig = definitionInfo.typeConfig
            text(typeConfig.name, typeConfig.pointer)
            run {
                val subtypeConfigs = definitionInfo.subtypeConfigs
                if (subtypeConfigs.isEmpty()) return@run
                if (!settings.showSubtypesForDefinitionReference) return@run
                if (!settings.truncateSubtypesForDefinitionReference) {
                    for (subtypeConfig in subtypeConfigs) {
                        text(", ")
                        text(subtypeConfig.name, subtypeConfig.pointer)
                    }
                } else {
                    text("...")
                }
            }
        }
    }
}
