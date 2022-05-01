package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*

class CwtLanguageCodeStyleSettingsProvider: LanguageCodeStyleSettingsProvider() {
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
		commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
		commonSettings.LINE_COMMENT_ADD_SPACE = false
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
					PlsBundle.message("cwt.codeStyle.spaceWithinBraces"),
					CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
				)
				consumer.showCustomOption(
					CwtCodeStyleSettings::class.java,
					CwtCodeStyleSettings.Option.SPACE_AROUND_SEPARATOR.name,
					PlsBundle.message("cwt.codeStyle.spaceAroundSeparator"),
					CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
				)
			}
			SettingsType.COMMENTER_SETTINGS -> {
				consumer.showStandardOptions(
					CodeStyleSettingsCustomizable.CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
					CodeStyleSettingsCustomizable.CommenterOption.LINE_COMMENT_ADD_SPACE.name
				)
			}
			else -> pass()
		}
	}
	
	class IndentOptionsEditor(
		provider: LanguageCodeStyleSettingsProvider
	) : SmartIndentOptionsEditor(provider)
}