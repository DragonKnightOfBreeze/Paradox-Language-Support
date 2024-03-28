@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxDefinitionReferenceInfoHintsSettingsKey")
    private val expressionTypes = mutableSetOf(
        CwtDataTypes.Definition,
        CwtDataTypes.AliasName, //需要兼容alias
        CwtDataTypes.AliasKeysField, //需要兼容alias
        CwtDataTypes.AliasMatchLeft, //需要兼容alias
        CwtDataTypes.SingleAliasRight, //需要兼容single_alias
    )
    
    override val name: String get() = PlsBundle.message("script.hints.definitionReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.definitionReferenceInfo.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey
    
    override fun createSettings() = NoSettings()
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        if(element !is ParadoxScriptExpressionElement) return true
        if(!ParadoxResolveConstraint.Definition.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if(!ParadoxResolveConstraint.Definition.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
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

