package icu.windea.pls.lang

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

object ParadoxParameterHandler {
    /**
     * 从上下文获取所有参数信息。
     */
    fun getParameters(context: ParadoxScriptDefinitionElement): Map<String, ParadoxParameterInfo> {
        if(!ParadoxParameterSupport.isContext(context)) return emptyMap()
        return CachedValuesManager.getCachedValue(context, PlsKeys.cachedParametersKey) {
            val value = doGetParameters(context)
            CachedValueProvider.Result(value, context)
        }
    }
    
    private fun doGetParameters(context: ParadoxScriptDefinitionElement): Map<String, ParadoxParameterInfo> {
        val file = context.containingFile
        val conditionalParameterNames = mutableSetOf<String>()
        val result = sortedMapOf<String, ParadoxParameterInfo>() //按名字进行排序
        context.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxParameter) visitParadoxParameter(element)
                if(element is ParadoxConditionParameter) visitParadoxConditionParameter(element)
                super.visitElement(element)
            }
            
            private fun visitParadoxConditionParameter(element: ParadoxConditionParameter) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                conditionalParameterNames.add(name)
                //不需要继续向下遍历
            }
            
            private fun visitParadoxParameter(element: ParadoxParameter) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                val info = result.getOrPut(name) { ParadoxParameterInfo(name) }
                info.pointers.add(element.createPointer(file))
                if(element.defaultValueToken != null) conditionalParameterNames.add(name)
                if(!info.optional && conditionalParameterNames.contains(name)) info.optional = true
                //不需要继续向下遍历
            }
        })
        return result
    }
    
    fun completeParameters(element: PsiElement, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        //向上找到参数上下文
        val parameterContext = ParadoxParameterSupport.findContext(element) ?: return
        val parameters = getParameters(parameterContext)
        if(parameters.isEmpty()) return
        for((parameterName, parameterInfo) in parameters) {
            ProgressManager.checkCanceled()
            val parameter = parameterInfo.pointers.firstNotNullOfOrNull { it.element } ?: continue
            //排除当前正在输入的那个
            if(parameterInfo.pointers.size == 1 && element isSamePosition parameter) continue
            val parameterElement = ParadoxParameterSupport.resolveParameter(parameter)
                ?: continue
            val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                .withIcon(PlsIcons.Parameter)
                .withTypeText(parameterElement.contextName, parameterContext.icon, true)
            result.addElement(lookupElement)
        }
    }
    
    fun completeArguments(element: PsiElement, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        if(quoted) return //输入参数不允许用引号括起
        val from = ParadoxParameterContextReferenceInfo.From.Argument
        val config = context.config as? CwtDataConfig<*> ?: return
        val completionOffset = context.parameters?.offset ?: return
        val contextReferenceInfo = ParadoxParameterSupport.findContextReferenceInfo(element, from, config, completionOffset) ?: return
        val argumentNames = contextReferenceInfo.argumentNames.toMutableSet()
        val namesToDistinct = mutableSetOf<String>().synced()
        //整合查找到的所有参数上下文
        val insertSeparator = context.isKey == true
        ParadoxParameterSupport.processContext(element, contextReferenceInfo) p@{ parameterContext ->
            ProgressManager.checkCanceled()
            val parameterMap = getParameters(parameterContext)
            if(parameterMap.isEmpty()) return@p true
            for((parameterName, parameterInfo) in parameterMap) {
                //排除已输入的
                if(parameterName in argumentNames) continue
                if(!namesToDistinct.add(parameterName)) continue
                
                val parameter = parameterInfo.pointers.firstNotNullOfOrNull { it.element } ?: return@p true
                val parameterElement = ParadoxParameterSupport.resolveParameter(parameter)
                    ?: continue
                val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                    .withIcon(PlsIcons.Parameter)
                    .withTypeText(parameterElement.contextName, parameterContext.icon, true)
                    .letIf(insertSeparator) {
                        it.withInsertHandler { c, _ ->
                            val editor = c.editor
                            val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
                            val text = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
                            EditorModificationUtil.insertStringAtCaret(editor, text, false, true)
                        }
                    }
                result.addElement(lookupElement)
            }
            true
        }
    }
}