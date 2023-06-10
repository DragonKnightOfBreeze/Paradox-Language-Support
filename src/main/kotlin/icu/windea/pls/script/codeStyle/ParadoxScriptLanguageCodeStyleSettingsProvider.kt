package icu.windea.pls.script.codeStyle


import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*

class ParadoxScriptLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
	override fun getLanguage() = ParadoxScriptLanguage
	
	override fun getConfigurableDisplayName() = PlsBundle.message("options.script.displayName")
	
	override fun getCodeSample(settingsType: SettingsType) = PlsConstants.paradoxScriptCodeStyleSettingsDemoText
	
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxScriptCodeStyleSettings(settings)
	
	//需要重载这个方法以显示indentOptions设置页面
	override fun getIndentOptionsEditor() = IndentOptionsEditor(this)
	
	override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
		indentOptions.INDENT_SIZE = 4
		indentOptions.CONTINUATION_INDENT_SIZE = 4
		indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = false
		indentOptions.USE_TAB_CHARACTER = false
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
			IndentOption.INDENT_SIZE.name,
			IndentOption.CONTINUATION_INDENT_SIZE.name,
			IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name,
			IndentOption.USE_TAB_CHARACTER.name
		)
	}
	
	private fun customizeSpacingSettings(consumer: CodeStyleSettingsCustomizable) {
		val spacesAroundOperators = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
		consumer.showCustomOption(ParadoxScriptCodeStyleSettings::class.java, "SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR", PlsBundle.message("script.codeStyleSettings.spacing.around.scriptedVariableSeparator"), spacesAroundOperators)
		consumer.showCustomOption(ParadoxScriptCodeStyleSettings::class.java, "SPACE_AROUND_PROPERTY_SEPARATOR", PlsBundle.message("script.codeStyleSettings.spacing.around.propertySeparator"), spacesAroundOperators)
		consumer.showCustomOption(ParadoxScriptCodeStyleSettings::class.java, "SPACE_AROUND_INLINE_MATH_OPERATOR", PlsBundle.message("script.codeStyleSettings.spacing.around.inlineMathOperator"), spacesAroundOperators)
		
		val spacesWithin = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
		consumer.showCustomOption(ParadoxScriptCodeStyleSettings::class.java, "SPACE_WITHIN_BRACES", PlsBundle.message("script.codeStyleSettings.spacing.withIn.braces"), spacesWithin)
		consumer.showCustomOption(ParadoxScriptCodeStyleSettings::class.java, "SPACE_WITHIN_PARAMETER_CONDITION_BRACKETS", PlsBundle.message("script.codeStyleSettings.spacing.withIn.parameterConditionBrackets"), spacesWithin)
		consumer.showCustomOption(ParadoxScriptCodeStyleSettings::class.java, "SPACE_WITHIN_PARAMETER_CONDITION_EXPRESSION_BRACKETS", PlsBundle.message("script.codeStyleSettings.spacing.withIn.parameterConditionExpressionBrackets"), spacesWithin)
		consumer.showCustomOption(ParadoxScriptCodeStyleSettings::class.java, "SPACE_WITHIN_INLINE_MATH_BRACKETS", PlsBundle.message("script.codeStyleSettings.spacing.withIn.inlineMathBrackets"), spacesWithin)
	}
	
	private fun customizeCommenterSettings(consumer: CodeStyleSettingsCustomizable) {
		consumer.showStandardOptions(
			CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
			CommenterOption.LINE_COMMENT_ADD_SPACE.name
		)
	}
	
	class IndentOptionsEditor(
		provider: LanguageCodeStyleSettingsProvider
	) : SmartIndentOptionsEditor(provider)
}
