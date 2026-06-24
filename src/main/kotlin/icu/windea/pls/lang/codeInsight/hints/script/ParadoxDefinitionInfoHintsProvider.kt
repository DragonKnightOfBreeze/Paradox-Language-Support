package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.codeInsight.hints.text
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

/**
 * 通过内嵌提示显示定义信息，包括名称、类型和子类型。
 *
 * @see ParadoxDefinitionInfoSettingsProvider
 */
class ParadoxDefinitionInfoHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptPropertyKey) return
        val definition = element.parent as? ParadoxScriptProperty ?: return
        val definitionInfo = definition.definitionInfo ?: return

        // 忽略内联或注入的定义
        if (definitionInfo.source == ParadoxDefinitionSource.Inline || definitionInfo.source == ParadoxDefinitionSource.Injection) return

        // 忽略类似 `event_namespace` 这样的定义的值不是子句的定义
        if (definitionInfo.declarationConfig?.config?.let { it.valueType == CwtExpressionType.Block } == false) return

        val typeConfig = definitionInfo.typeConfig
        val subtypeConfigs = definitionInfo.subtypeConfigs

        // 如果定义名等同于类型键，则省略定义名
        val settings = ParadoxDeclarativeHintsSettings.getInstance(definitionInfo.project)
        sink.addInlinePresentation(element.endOffset, priority = 1) {
            if (settings.showDefinitionName && !definitionInfo.name.equals(definitionInfo.typeKey, true)) {
                text("${definitionInfo.name}: ".optimized())
            } else {
                text(": ")
            }
            text(typeConfig.name, typeConfig.pointer)
            run {
                if (subtypeConfigs.isEmpty()) return@run
                if (!settings.showDefinitionSubtypes) return@run
                val limit = settings.truncateDefinitionSubtypes
                for (i in 0 until subtypeConfigs.size) {
                    if (limit >= 0 && limit <= i) {
                        text(", ...")
                        break
                    }
                    val subtypeConfig = subtypeConfigs[i]
                    text(", ")
                    text(subtypeConfig.name, subtypeConfig.pointer)
                }
            }
        }
    }
}
