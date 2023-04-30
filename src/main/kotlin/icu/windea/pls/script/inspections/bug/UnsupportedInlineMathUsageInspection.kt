package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了内联数学表达式。
 */
class UnsupportedInlineMathUsageInspection: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val extension = file.name.substringAfterLast('.').lowercase()
        if(extension == "asset") {
            return object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    ProgressManager.checkCanceled()
                    if(element is ParadoxScriptInlineMath) {
                        holder.registerProblem(element, PlsBundle.message("inspection.script.bug.unsupportedInlineMathUsage.description.1"))
                    }
                }
            }
        }
        
        //每个scripted_trigger或者scripted_effect声明中仅能使用一次
        val fileInfo = file.fileInfo
        if(fileInfo != null) {
            val path = fileInfo.path
            val gameType = fileInfo.rootInfo.gameType
            val configGroup = getCwtConfig(file.project).get(gameType)
            
            fun doBuildVisitor(definitionType: String, message: String): PsiElementVisitor? {
                val definitionFilePath = configGroup.types.get(definitionType)?.path ?: return null
                if(!definitionFilePath.matchesPath(path.path)) return null
                
                return object : PsiElementVisitor() {
                    private var first: SmartPsiElementPointer<ParadoxScriptInlineMath>? = null
                    private var firstRegistered: Boolean = false
                    
                    override fun visitElement(element: PsiElement) {
                        ProgressManager.checkCanceled()
                        if(element is ParadoxScriptDefinitionElement && element.definitionInfo?.type == definitionType) {
                            first = null
                            firstRegistered = false
                            return
                        }
                        if(element is ParadoxScriptInlineMath) {
                            if(first == null) {
                                first = element.createPointer(file)
                            } else {
                                val firstElement = first?.element
                                if(firstElement != null) {
                                    holder.registerProblem(firstElement, message)
                                    firstRegistered = true
                                }
                                holder.registerProblem(element, message)
                            }
                        }
                    }
                }
            }
            
            doBuildVisitor("scripted_trigger", PlsBundle.message("inspection.script.bug.unsupportedInlineMathUsage.description.2"))
                ?.let { return it }
            doBuildVisitor("scripted_effect", PlsBundle.message("inspection.script.bug.unsupportedInlineMathUsage.description.2"))
                ?.let { return it }
        }
        
        return PsiElementVisitor.EMPTY_VISITOR
    }
}