package icu.windea.pls.script.inspections.bug

import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * （对于脚本文件）检查是否存在不支持的递归。例如，递归调用scripted_trigger/scripted_effect。
 */
class UnsupportedRecursionInspection : LocalInspectionTool() {
    //目前仅做检查即可，不需要显示递归的装订线图标
    //在定义声明级别进行此项检查
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if(!isScriptFileToInspect(holder.file)) return PsiElementVisitor.EMPTY_VISITOR
        
        //TODO 仍然需要优化性能 - 考虑缓存堆栈信息？
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo ?: return
                    if(definitionInfo.type != "scripted_trigger" && definitionInfo.type != "scripted_effect") return
                    
                    withMeasureMillis({ element.fileInfo?.path.toString() + "." + definitionInfo.name }) {
                        visitDefinition(element, definitionInfo)
                    }
                }
            }
            
            private fun visitDefinition(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
                //防止StackOverflow
                val guardStack = LinkedList<String>()
                guardStack.add(definitionInfo.name)
                try {
                    //如果原本会发生StackOverflow,这里会抛出StackOverflowPreventedException
                    doRecursiveVisit(element, definitionInfo, guardStack)
                } catch(e: RecursionException) {
                    if(e.definitionInfo.name == definitionInfo.name) {
                        registerProblem(element, definitionInfo, e.recursion)
                    }
                }
            }
            
            private fun doRecursiveVisit(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, guardStack: LinkedList<String>) {
                ProgressManager.checkCanceled()
                element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(e: PsiElement) {
                        if(e is ParadoxScriptStringExpressionElement) visitStringExpressionElement(e)
                        if(e.isExpressionOrMemberContext()) super.visitElement(e)
                    }
                    
                    private fun visitStringExpressionElement(e: ParadoxScriptStringExpressionElement) {
                        ProgressManager.checkCanceled()
                        //为了优化性能，这里不直接解析引用
                        //认为scripted_trigger/scripted_effect不能在各种复杂表达式中使用，并且名字必须是合法的标识符
                        if(!e.value.isExactIdentifier()) return
                        val isKey = e is ParadoxScriptPropertyKey
                        val configs = ParadoxConfigHandler.getConfigs(e, !isKey, isKey)
                        val config = configs.firstOrNull() ?: return
                        val configExpression = config.expression
                        if(configExpression.type != CwtDataType.Definition) return
                        val definitionType = configExpression.value
                        if(definitionType != "scripted_trigger" && definitionType != "scripted_effect") return
                        ProgressManager.checkCanceled()
                        val resolved = ParadoxConfigHandler.resolveScriptExpression(e, null, config, config.expression, config.info.configGroup, isKey)
                        if(resolved !is ParadoxScriptDefinitionElement) return
                        val resolvedInfo = resolved.definitionInfo ?: return
                        if(resolvedInfo.type != definitionInfo.type) return
                        if(guardStack.contains(resolvedInfo.name)) throw RecursionException(e, resolved, resolvedInfo)
                        guardStack.add(resolvedInfo.name)
                        doRecursiveVisit(resolved, definitionInfo, guardStack)
                        guardStack.removeLast()
                    }
                })
            }
            
            private fun registerProblem(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, recursion: PsiElement) {
                val message = when {
                    definitionInfo.type == "scripted_trigger" -> PlsBundle.message("inspection.script.bug.unsupportedRecursion.description.1")
                    definitionInfo.type == "scripted_effect" -> PlsBundle.message("inspection.script.bug.unsupportedRecursion.description.2")
                    else -> return
                }
                val location = if(element is ParadoxScriptProperty) element.propertyKey else element
                holder.registerProblem(location, message, NavigateToRecursionFix(recursion))
            }
        }
    }
    
    private fun isScriptFileToInspect(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.pathToEntry
        return "txt" == filePath.fileExtension && ("common/scripted_triggers".matchesPath(filePath.path) || "common/scripted_effects".matchesPath(filePath.path))
    }
    
    private class NavigateToRecursionFix(
        target: PsiElement,
    ) : LocalQuickFixAndIntentionActionOnPsiElement(target) {
        override fun getText() = PlsBundle.message("inspection.script.bug.unsupportedRecursion.quickFix.1")
        
        override fun getFamilyName() = text
        
        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            if(editor == null) return
            navigateTo(editor, startElement)
        }
        
        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY
        
        override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
        
        override fun startInWriteAction() = false
        
        override fun availableInBatchMode() = false
        
        private fun navigateTo(editor: Editor, toNavigate: PsiElement) {
            editor.caretModel.moveToOffset(toNavigate.textOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }
    
    private class RecursionException(
        val recursion: PsiElement,
        val definition: ParadoxScriptDefinitionElement,
        val definitionInfo: ParadoxDefinitionInfo
    ): RuntimeException()
}