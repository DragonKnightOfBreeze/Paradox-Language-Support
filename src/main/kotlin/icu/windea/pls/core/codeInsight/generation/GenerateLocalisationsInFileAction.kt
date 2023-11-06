package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.codeInsight.generation.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 生成当前脚本文件中所有定义的所有（缺失的）本地化。
 */
class GenerateLocalisationsInFileAction : BaseCodeInsightAction(), GenerateActionPopupTemplateInjector {
    private val handler = ParadoxGenerateLocalisationsHandler(forFile = true)
    
    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }
    
    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        return file is ParadoxScriptFile && file.fileInfo != null
    }
    
    override fun createEditTemplateAction(dataContext: DataContext?): AnAction? {
        return null
    }
}