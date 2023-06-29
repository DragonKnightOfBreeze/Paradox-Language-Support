package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
class GenerateLocalisationsInFileHandler : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val contextKey = PlsKeys.generateLocalisationsInFileContextKey
        val context = file.getUserData(contextKey)
            ?: getDefaultContext(project, editor, file)
            ?: return
        ParadoxPsiGenerator.generateLocalisationsInFile(context, project, file)
    }
    
    private fun getDefaultContext(project: Project, editor: Editor, file: PsiFile): GenerateLocalisationsInFileContext? {
        //直接基于所有的相关本地化，无论是否确实
        return getDefaultContext(file)
    }
    
    companion object {
        @JvmStatic
        fun getDefaultContext(file: PsiFile): GenerateLocalisationsInFileContext? {
            val context = GenerateLocalisationsInFileContext(file.name, mutableListOf())
            file.accept(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxScriptDefinitionElement) visitDefinition(element)
                    if(element.isExpressionOrMemberContext()) super.visitElement(element)
                }
                
                private fun visitDefinition(element: ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo ?: return
                    val context0 = GenerateLocalisationsHandler.getDefaultContext(definitionInfo) ?: return
                    context.contextList.add(context0)
                }
            })
            return context
        }
    }
}
 