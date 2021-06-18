package icu.windea.pls.localisation.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	override fun getLanguage() = ParadoxLocalisationLanguage
	
	override fun getCodeSample(settingsType: SettingsType) = paradoxLocalisationDemoText
	
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxLocalisationCodeStyleSettings(settings)
	
	//需要重载这个方法以显示indentOptions设置页面
	override fun getIndentOptionsEditor() = IndentOptionsEditor(this)

	override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
		indentOptions.INDENT_SIZE = 1
		indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = true
		commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
		commonSettings.LINE_COMMENT_ADD_SPACE = false
	}

	override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
		when(settingsType) {
			SettingsType.INDENT_SETTINGS -> {
				consumer.showStandardOptions(
					CodeStyleSettingsCustomizable.IndentOption.INDENT_SIZE.name,
					CodeStyleSettingsCustomizable.IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name
				)
			}
			SettingsType.COMMENTER_SETTINGS -> {
				consumer.showStandardOptions(
					CodeStyleSettingsCustomizable.CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
					CodeStyleSettingsCustomizable.CommenterOption.LINE_COMMENT_ADD_SPACE.name
				)
			}
		}
	}
	
	class IndentOptionsEditor(
		provider: LanguageCodeStyleSettingsProvider
	) : SmartIndentOptionsEditor(provider)
}