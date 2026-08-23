package icu.windea.pls.lang.codeInsight.template.postfix

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.codeInsight.template.postfix.templates.editable.EditablePostfixTemplate
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.FromInternalConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.core.quote
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.text.ParadoxCsv
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.text.ParadoxScript

@FromInternalConfig("builtin/postfix_template_settings.cwt", CwtPostfixTemplateSettingsConfig::class)
abstract class ParadoxExpressionEditablePostfixTemplate(
    val setting: CwtPostfixTemplateSettingsConfig,
    provider: PostfixTemplateProvider
) : EditablePostfixTemplate(setting.id, setting.key, createTemplate(setting), setting.example.orEmpty(), provider) {
    abstract val groupName: String

    override fun isBuiltin(): Boolean {
        return true
    }

    override fun addTemplateVariables(element: PsiElement, template: Template) {
        val variables = setting.variables
        if (variables.isEmpty()) return
        val quotePattern = when(element.language) {
            ParadoxScriptLanguage -> QuotePatterns.ParadoxScript
            ParadoxCsvLanguage -> QuotePatterns.ParadoxCsv
            else -> QuotePatterns.Default
        }
        for (variable in variables) {
            template.addVariable(variable.key, "", variable.value.quote(quotePattern), true)
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxExpressionEditablePostfixTemplate && setting == other.setting)
    }

    override fun hashCode(): Int {
        return setting.hashCode()
    }
}

// com.intellij.codeInsight.template.postfix.templates.editable.EditablePostfixTemplateWithMultipleExpressions.createTemplate

private fun createTemplate(setting: CwtPostfixTemplateSettingsConfig): TemplateImpl {
    val template = TemplateImpl("fakeKey", setting.expression, "")
    template.isToReformat = true
    (template as Template).templateText // call template.parseSegments(), and pass compatibility verification
    return template
}
