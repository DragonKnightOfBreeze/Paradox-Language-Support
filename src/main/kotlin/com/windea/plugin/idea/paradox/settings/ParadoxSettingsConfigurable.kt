package com.windea.plugin.idea.paradox.settings

import com.intellij.openapi.options.*
import com.windea.plugin.idea.paradox.message
import javax.swing.*

class ParadoxSettingsConfigurable: SearchableConfigurable {
	companion object{
		 private val _name =  message("paradox.settings")
	}
	
	private var component: ParadoxSettingsComponent? = null
	
	override fun getId() = "settings.language.paradox"
	
	override fun getDisplayName() = _name
	
	override fun createComponent(): JComponent {
		val component = ParadoxSettingsComponent()
		this.component = component
		return component.panel
	}

	override fun disposeUIResources() {
		component = null
	}
	
	override fun isModified(): Boolean {
		val settings = ParadoxSettingsState.getInstance()
		val settingsComponent = component!!
		return settingsComponent.resolveStringReferencesCheckBox.isSelected != settings.resolveStringReferences
		       || settingsComponent.validateScriptCheckBox.isSelected != settings.validateScript
		       || settingsComponent.renderLocalisationTextCheckBox.isSelected != settings.renderLocalisationText
	}
	
	override fun apply() {
		val settings = ParadoxSettingsState.getInstance()
		val settingsComponent = component ?: return
		settings.resolveStringReferences = settingsComponent.resolveStringReferencesCheckBox.isSelected
		settings.validateScript = settingsComponent.validateScriptCheckBox.isSelected
		settings.renderLocalisationText = settingsComponent.renderLocalisationTextCheckBox.isSelected
	}
	
	override fun reset() {
		val settings = ParadoxSettingsState.getInstance()
		val settingsComponent = component ?: return
		settingsComponent.resolveStringReferencesCheckBox.isSelected = settings.resolveStringReferences
		settingsComponent.validateScriptCheckBox.isSelected = settings.validateScript
		settingsComponent.renderLocalisationTextCheckBox.isSelected = settings.renderLocalisationText
	}
}
