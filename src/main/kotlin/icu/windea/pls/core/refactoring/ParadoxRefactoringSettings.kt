package icu.windea.pls.core.refactoring

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@State(name = "ParadoxRefactoringSettings", storages = [Storage("baseRefactoring.xml")], category = SettingsCategory.CODE)
class ParadoxRefactoringSettings : PersistentStateComponent<ParadoxRefactoringSettings> {
    @JvmField var renameRelatedLocalisations = true
    @JvmField var renameRelatedImages = true
    @JvmField var renameGeneratedModifier = true
    @JvmField var renameGeneratedModifierNameDesc = true
    @JvmField var renameGeneratedModifierIcon = true
    
    @JvmField var inlineScriptedVariableThis = false
    @JvmField var inlineScriptedVariableKeep = false
    @JvmField var inlineScriptedTriggerThis = false
    @JvmField var inlineScriptedTriggerKeep = false
    @JvmField var inlineScriptedEffectThis = false
    @JvmField var inlineScriptedEffectKeep = false
    @JvmField var inlineLocalisationThis = false
    @JvmField var inlineLocalisationKeep = false
    @JvmField var inlineInlineScriptThis = false
    @JvmField var inlineInlineScriptKeep = false
    
    override fun getState() = this
    
    override fun loadState(state: ParadoxRefactoringSettings) = XmlSerializerUtil.copyBean(state, this)
    
    companion object {
        @JvmStatic
        fun getInstance(): ParadoxRefactoringSettings = service()
    }
}