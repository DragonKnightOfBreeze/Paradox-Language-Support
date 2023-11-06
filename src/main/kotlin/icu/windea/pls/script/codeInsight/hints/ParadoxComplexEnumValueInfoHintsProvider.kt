package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 复杂枚举值的内嵌提示（枚举值的类型即枚举的名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxComplexEnumValueInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxComplexEnumValueInfoHintsSettingsKey")
    
    override val name: String get() = PlsBundle.message("script.hints.complexEnumValueInfo")
    override val description: String get() = PlsBundle.message("script.hints.complexEnumValueInfo.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey
    
    override fun createSettings() = NoSettings()
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        if(element !is ParadoxScriptStringExpressionElement) return true
        if(!element.isExpression()) return true
        val info = ParadoxComplexEnumValueHandler.getInfo(element)
        if(info != null) {
            val complexEnumConfig = info.getConfig(file.project) ?: return true
            val presentation = doCollect(complexEnumConfig)
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
            return true
        }
        
        val config = CwtConfigHandler.getConfigs(element).firstOrNull() ?: return true
        val configGroup = config.info.configGroup
        val type = config.expression.type
        if(type == CwtDataType.EnumValue) {
            val enumName = config.expression.value ?: return true
            val complexEnumConfig = configGroup.complexEnums[enumName] ?: return true
            val presentation = doCollect(complexEnumConfig)
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }
    
    private fun PresentationFactory.doCollect(complexEnumConfig: CwtComplexEnumConfig): InlayPresentation {
        val presentations = mutableListOf<InlayPresentation>()
        presentations.add(smallText(": "))
        presentations.add(psiSingleReference(smallText(complexEnumConfig.name)) { complexEnumConfig.pointer.element })
        return SequencePresentation(presentations)
    }
}