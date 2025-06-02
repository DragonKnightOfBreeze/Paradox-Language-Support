package icu.windea.pls.lang.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.*

class ParadoxPostfixTemplateProvider : PostfixTemplateProvider {
    private val defaultTemplates by lazy {
        buildSet<PostfixTemplate> {
            val provider = this@ParadoxPostfixTemplateProvider
            val postfixTemplateSettings = PlsFacade.getConfigGroup(null).postfixTemplateSettings
            for ((groupName, settings) in postfixTemplateSettings) {
                when (groupName) {
                    ParadoxVariableOperationExpressionPostfixTemplate.Constants.GROUP_NAME -> {
                        for (setting in settings.values) {
                            add(ParadoxVariableOperationExpressionPostfixTemplate(setting, provider))
                        }
                    }
                }
            }
        }
    }

    override fun getId(): String {
        return "paradox.script"
    }

    //customizing postfix templates is not supported so far
    override fun getPresentableName(): String? {
        return null
    }

    override fun getTemplates(): Set<PostfixTemplate> {
        return defaultTemplates
    }


    override fun isTerminalSymbol(currentChar: Char): Boolean {
        return currentChar == '.'
    }

    override fun preExpand(file: PsiFile, editor: Editor) {

    }

    override fun afterExpand(file: PsiFile, editor: Editor) {

    }

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile {
        return copyFile
    }
}
