package icu.windea.pls.core.refactoring

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@State(name = "RefactoringSettings", storages = [Storage("baseRefactoring.xml")], category = SettingsCategory.CODE)
class ParadoxRefactoringSettings : PersistentStateComponent<ParadoxRefactoringSettings> {
    @JvmField var renameRelatedLocalisations = true
    @JvmField var renameRelatedImages = true
    @JvmField var renameGeneratedModifier = true
    @JvmField var renameGeneratedModifierNameDesc = true
    @JvmField var renameGeneratedModifierIcon = true
    
    override fun getState() = this
    
    override fun loadState(state: ParadoxRefactoringSettings) = XmlSerializerUtil.copyBean(state, this)
    
    companion object {
        @JvmStatic
        val instance: ParadoxRefactoringSettings get() = service()
    }
}