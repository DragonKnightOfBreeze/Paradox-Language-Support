package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.codeInsight.hints.text
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.parentProperty

/**
 * 通过内嵌提示显示定义注入信息，包括目标的类型和子类型。
 */
class ParadoxDefinitionInjectionInfoHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptPropertyKey) return
        val property = element.parentProperty ?: return
        val definitionInjectionInfo = property.definitionInjectionInfo ?: return

        val typeConfig = definitionInjectionInfo.typeConfig ?: return
        val subtypeConfigs = definitionInjectionInfo.subtypeConfigs

        // 省略定义名
        val settings = ParadoxDeclarativeHintsSettings.getInstance(definitionInjectionInfo.project)
        sink.addInlinePresentation(element.endOffset, priority = 1) {
            text(": ")
            text(typeConfig.name, typeConfig.pointer)
            run {
                if (subtypeConfigs.isEmpty()) return@run
                if (!settings.showSubtypesForDefinitionInjection) return@run
                if (!settings.truncateSubtypesForDefinitionInjection) {
                    for (subtypeConfig in subtypeConfigs) {
                        text(", ")
                        text(subtypeConfig.name, subtypeConfig.pointer)
                    }
                } else {
                    text(", ...")
                }
            }
        }
    }
}
