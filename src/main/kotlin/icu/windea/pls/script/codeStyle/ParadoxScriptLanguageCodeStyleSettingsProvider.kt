package icu.windea.pls.script.codeStyle


import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.codeStyle.ParadoxScriptCodeStyleSettings.*

class ParadoxScriptLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	companion object{
		private val _spaceWithinBracesTitle = message("paradox.script.codeStyle.spaceWithinBraces")
		private val _spaceAroundSeparatorTitle = message("paradox.script.codeStyle.spaceAroundSeparator")
	}
	
	override fun getLanguage() = ParadoxScriptLanguage
	
	override fun getCodeSample(settingsType: SettingsType) = paradoxScriptDemoText
	
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxScriptCodeStyleSettings(settings)

	//需要重载这个方法以显示indentOptions设置页面
	override fun getIndentOptionsEditor() = IndentOptionsEditor(this)

	override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
		indentOptions.INDENT_SIZE = 4
		indentOptions.CONTINUATION_INDENT_SIZE = 4
		indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = true
		indentOptions.USE_TAB_CHARACTER = true
		commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
		commonSettings.LINE_COMMENT_ADD_SPACE = false
	}

	override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
		when(settingsType) {
			SettingsType.INDENT_SETTINGS -> {
				consumer.showStandardOptions(
					IndentOption.INDENT_SIZE.name,
					IndentOption.CONTINUATION_INDENT_SIZE.name,
					IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name,
					IndentOption.USE_TAB_CHARACTER.name
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
			SettingsType.COMMENTER_SETTINGS -> {
				consumer.showStandardOptions(
					CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
					CommenterOption.LINE_COMMENT_ADD_SPACE.name
				)
			}
			else -> pass()
		}
	}
	
	class IndentOptionsEditor(
		provider: LanguageCodeStyleSettingsProvider
	) : SmartIndentOptionsEditor(provider)
}
