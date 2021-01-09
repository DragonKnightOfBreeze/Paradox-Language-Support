package com.windea.plugin.idea.paradox.localisation.formatter

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.*

//代码风格：
//INDENT_SETTINGS
//* INDENT_SIZE
//* KEEP_INDENTS_ON_EMPTY_LINES
//WRAPPING_AND_BRACES_SETTINGS
//* ALIGN_PROPERTY_VALUES

class ParadoxLocalisationLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	override fun getLanguage() = ParadoxLocalisationLanguage
	
	override fun getCodeSample(settingsType: SettingsType) = paradoxLocalisationSampleText
	
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxLocalisationCodeStyleSettings(settings)
	
	//需要重载这个方法以显示indentOptions设置页面
	override fun getIndentOptionsEditor() = IndentOptionsEditor(this)

	override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
		indentOptions.INDENT_SIZE = 1
		indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = true
	}

	override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
		when(settingsType) {
			SettingsType.INDENT_SETTINGS -> {
				consumer.showStandardOptions(
					IndentOption.INDENT_SIZE.name,
					IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name
				)
			}
			else -> {}
		}
	}
	
	class IndentOptionsEditor(
		provider: LanguageCodeStyleSettingsProvider
	) : SmartIndentOptionsEditor(provider)
}
