package com.windea.plugin.idea.pls.cwt.formatter

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.cwt.*

class CwtLanguageCodeStyleSettingsProvider:LanguageCodeStyleSettingsProvider() {
	companion object{
		private val _spaceWithinBracesTitle = message("cwt.codeStyle.spaceWithinBraces")
		private val _spaceAroundSeparatorTitle = message("cwt.codeStyle.spaceAroundSeparator")
	}
	
	override fun getLanguage() = CwtLanguage
	
	override fun getCodeSample(settingsType: SettingsType) = cwtDemoText
	
	override fun createCustomSettings(settings: CodeStyleSettings) = CwtCodeStyleSettings(settings)
	
	//需要重载这个方法以显示indentOptions设置页面
	override fun getIndentOptionsEditor() = IndentOptionsEditor(this)
	
	override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
		indentOptions.INDENT_SIZE = 4
		indentOptions.CONTINUATION_INDENT_SIZE = 4
		indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = true
		indentOptions.USE_TAB_CHARACTER = true
	}
	
	override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
		when(settingsType) {
			SettingsType.INDENT_SETTINGS -> {
				consumer.showStandardOptions(
					CodeStyleSettingsCustomizable.IndentOption.INDENT_SIZE.name,
					CodeStyleSettingsCustomizable.IndentOption.CONTINUATION_INDENT_SIZE.name,
					CodeStyleSettingsCustomizable.IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name,
					CodeStyleSettingsCustomizable.IndentOption.USE_TAB_CHARACTER.name
				)
			}
			SettingsType.SPACING_SETTINGS -> {
				consumer.showCustomOption(
					CwtCodeStyleSettings::class.java,
					CwtCodeStyleSettings.Option.SPACE_WITHIN_BRACES.name,
					_spaceWithinBracesTitle,
					CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
				)
				consumer.showCustomOption(
					CwtCodeStyleSettings::class.java,
					CwtCodeStyleSettings.Option.SPACE_AROUND_SEPARATOR.name,
					_spaceAroundSeparatorTitle,
					CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
				)
			}
			else -> {}
		}
	}
	
	class IndentOptionsEditor(
		provider: LanguageCodeStyleSettingsProvider
	) : SmartIndentOptionsEditor(provider)
}