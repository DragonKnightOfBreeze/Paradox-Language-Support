package com.windea.plugin.idea.paradox.script.formatter


import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.message
import com.windea.plugin.idea.paradox.script.*
import com.windea.plugin.idea.paradox.script.formatter.ParadoxScriptCodeStyleSettings.*

//代码风格：
//INDENT_SETTINGS
//* INDENT_SIZE
//* CONTINUATION_INDENT_SIZE
//* KEEP_INDENTS_ON_EMPTY_LINES
//* USE_TAB_CHARACTER
//SPACING_SETTINGS
//* SPACE_WITHIN_BRACES
//* SPACE_AROUND_SEPARATOR

class ParadoxScriptLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	companion object{
		private val _spaceWithinBracesTitle = message("paradox.script.codeStyle.spaceWithinBraces")
		private val _spaceAroundSeparatorTitle = message("paradox.script.codeStyle.spaceAroundSeparator")
	}
	
	override fun getLanguage() = ParadoxScriptLanguage
	
	override fun getCodeSample(settingsType: SettingsType) = paradoxScriptSampleText
	
	override fun createCustomSettings(settings: CodeStyleSettings) =
		ParadoxScriptCodeStyleSettings(settings)

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
					ParadoxScriptCodeStyleSettings::class.java,
					Option.SPACE_WITHIN_BRACES.name,
					_spaceWithinBracesTitle,
					CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
				)
				consumer.showCustomOption(
					ParadoxScriptCodeStyleSettings::class.java,
					Option.SPACE_AROUND_SEPARATOR.name,
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
