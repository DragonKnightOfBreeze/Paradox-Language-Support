package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*

class CwtLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	override fun getLanguage() = CwtLanguage
	
	override fun getCodeSample(settingsType: SettingsType) = cwtCodeStyleSettingsDemoText
	
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
			SettingsType.INDENT_SETTINGS -> customizeIndentSettings(consumer)
			SettingsType.SPACING_SETTINGS -> customizeSpacingSettings(consumer)
			SettingsType.COMMENTER_SETTINGS -> customizeCommenterSettings(consumer)
			else -> pass()
		}
	}
	
	private fun customizeIndentSettings(consumer: CodeStyleSettingsCustomizable) {
		consumer.showStandardOptions(
			CodeStyleSettingsCustomizable.IndentOption.INDENT_SIZE.name,
			CodeStyleSettingsCustomizable.IndentOption.CONTINUATION_INDENT_SIZE.name,
			CodeStyleSettingsCustomizable.IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name,
			CodeStyleSettingsCustomizable.IndentOption.USE_TAB_CHARACTER.name
		)
	}
	
	private fun customizeSpacingSettings(consumer: CodeStyleSettingsCustomizable) {
		val spacesAroundOperators = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
		consumer.showCustomOption(CwtCodeStyleSettings::class.java, "SPACE_AROUND_OPTION_SEPARATOR", PlsBundle.message("cwt.codeStyleSettings.spacing.around.optionSeparator"), spacesAroundOperators)
		consumer.showCustomOption(CwtCodeStyleSettings::class.java, "SPACE_AROUND_PROPERTY_SEPARATOR", PlsBundle.message("cwt.codeStyleSettings.spacing.around.propertySeparator"), spacesAroundOperators)
		
		val spacesWithin = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
		consumer.showCustomOption(CwtCodeStyleSettings::class.java, "SPACE_WITHIN_BRACES", PlsBundle.message("cwt.codeStyleSettings.spacing.withIn.braces"), spacesWithin)
	}
	
	private fun customizeCommenterSettings(consumer: CodeStyleSettingsCustomizable) {
		consumer.showStandardOptions(
			CodeStyleSettingsCustomizable.CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
			CodeStyleSettingsCustomizable.CommenterOption.LINE_COMMENT_ADD_SPACE.name
		)
	}
	
	class IndentOptionsEditor(
		provider: LanguageCodeStyleSettingsProvider
	) : SmartIndentOptionsEditor(provider)
}