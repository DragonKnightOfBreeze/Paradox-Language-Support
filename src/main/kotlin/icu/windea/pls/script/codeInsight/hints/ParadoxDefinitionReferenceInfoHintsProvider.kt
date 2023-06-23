package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
    companion object {
        private val settingsKey: SettingsKey<NoSettings> = SettingsKey("ParadoxDefinitionReferenceInfoHintsSettingsKey")
        private val expressionTypes: EnumSet<CwtDataType> = enumSetOf(
            CwtDataType.Definition,
            CwtDataType.AliasName, //需要兼容alias
            CwtDataType.AliasKeysField, //需要兼容alias
            CwtDataType.AliasMatchLeft, //需要兼容alias
            CwtDataType.SingleAliasRight, //需要兼容single_alias
        )
    }
    
    override val name: String get() = PlsBundle.message("script.hints.definitionReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.definitionReferenceInfo.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey
    
    override fun createSettings() = NoSettings()
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        //这里需要兼容 ParadoxScriptInt
        if(element !is ParadoxScriptExpressionElement) return true
        if(element !is ParadoxScriptStringExpressionElement && element !is ParadoxScriptInt) return true
        if(!element.isExpression()) return true
        val config = ParadoxConfigHandler.getConfigs(element).firstOrNull()
            ?.takeIf { it.expression.type in expressionTypes }
            ?: return true
        val configGroup = config.info.configGroup
        val isKey = element is ParadoxScriptPropertyKey
        val resolved = ParadoxConfigHandler.resolveScriptExpression(element, null, config, config.expression, configGroup, isKey)
        if(resolved is ParadoxScriptDefinitionElement) {
            val definitionInfo = resolved.definitionInfo
            if(definitionInfo != null) {
                val presentation = doCollect(definitionInfo)
                val finalPresentation = presentation.toFinalPresentation(this, file.project)
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }
    
    private fun PresentationFactory.doCollect(definitionInfo: ParadoxDefinitionInfo): InlayPresentation {
        val presentations: MutableList<InlayPresentation> = mutableListOf()
        //省略definitionName
        presentations.add(smallText(": "))
        val typeConfig = definitionInfo.typeConfig
        presentations.add(psiSingleReference(smallText(typeConfig.name)) { typeConfig.pointer.element })
        val subtypeConfigs = definitionInfo.subtypeConfigs
        for(subtypeConfig in subtypeConfigs) {
            presentations.add(smallText(", "))
            presentations.add(psiSingleReference(smallText(subtypeConfig.name)) { subtypeConfig.pointer.element })
        }
        return SequencePresentation(presentations)
    }
}

