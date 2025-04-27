package icu.windea.pls.script.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.script.codeStyle.ParadoxScriptCodeStyleSettings as Settings

class ParadoxScriptLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = ParadoxScriptLanguage.INSTANCE

    override fun getCodeSample(settingsType: SettingsType) = PlsConstants.Samples.paradoxScriptCodeStyleSettings

    override fun createCustomSettings(settings: CodeStyleSettings) = Settings(settings)

    //需要重载这个方法以显示indentOptions设置页面
    override fun getIndentOptionsEditor() = IndentOptionsEditor(this)

    override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
        indentOptions.INDENT_SIZE = 4
        indentOptions.CONTINUATION_INDENT_SIZE = 4
        indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = false
        indentOptions.USE_TAB_CHARACTER = false
        commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
        commonSettings.LINE_COMMENT_ADD_SPACE = true
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.INDENT_SETTINGS -> customizeIndentSettings(consumer)
            SettingsType.SPACING_SETTINGS -> customizeSpacingSettings(consumer)
            SettingsType.BLANK_LINES_SETTINGS -> customizeBlankLinesSettings(consumer)
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
        val spacesAroundOperatorsGroup = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR.name, PlsBundle.message("script.codeStyleSettings.spacing.around.scriptedVariableSeparator"), spacesAroundOperatorsGroup)
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_AROUND_PROPERTY_SEPARATOR.name, PlsBundle.message("script.codeStyleSettings.spacing.around.propertySeparator"), spacesAroundOperatorsGroup)
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_AROUND_INLINE_MATH_OPERATOR.name, PlsBundle.message("script.codeStyleSettings.spacing.around.inlineMathOperator"), spacesAroundOperatorsGroup)

        val spacesWithinGroup = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_WITHIN_BRACES.name, PlsBundle.message("script.codeStyleSettings.spacing.withIn.braces"), spacesWithinGroup)
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_WITHIN_PARAMETER_CONDITION_BRACKETS.name, PlsBundle.message("script.codeStyleSettings.spacing.withIn.parameterConditionBrackets"), spacesWithinGroup)
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_WITHIN_PARAMETER_CONDITION_EXPRESSION_BRACKETS.name, PlsBundle.message("script.codeStyleSettings.spacing.withIn.parameterConditionExpressionBrackets"), spacesWithinGroup)
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_WITHIN_INLINE_MATH_BRACKETS.name, PlsBundle.message("script.codeStyleSettings.spacing.withIn.inlineMathBrackets"), spacesWithinGroup)
    }

    private fun customizeBlankLinesSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            BlankLinesOption.KEEP_BLANK_LINES_IN_CODE.name
        )
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
