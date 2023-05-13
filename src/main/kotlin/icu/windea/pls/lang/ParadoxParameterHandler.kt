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
import java.util.*

object ParadoxParameterHandler {
    fun getContextInfo(context: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
        if(!ParadoxParameterSupport.isContext(context)) return null
        return CachedValuesManager.getCachedValue(context, PlsKeys.cachedParametersKey) {
            val value = doGetContextInfo(context)
            CachedValueProvider.Result(value, context)
        }
    }
    
    private fun doGetContextInfo(context: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo {
        val file = context.containingFile
        val parameters = sortedMapOf<String, MutableList<ParadoxParameterInfo>>() //按名字进行排序
        val fileConditionStack = LinkedList<ReversibleValue<String>>()
        context.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxParameter) visitParameter(element)
                if(element is ParadoxScriptParameterConditionExpression) visitParadoxConditionExpression(element)
                super.visitElement(element)
            }
            
            private fun visitParadoxConditionExpression(element: ParadoxScriptParameterConditionExpression) {
                ProgressManager.checkCanceled()
                var operator = true
                var value = ""
                element.processChild p@{
                    val elementType = it.elementType
                    when(elementType) {
                        ParadoxScriptElementTypes.NOT_SIGN -> operator = false
                        ParadoxScriptElementTypes.PARAMETER_CONDITION_PARAMETER -> value = it.text
                    }
                    true
                }
                //value may be empty (invalid condition expression)
                fileConditionStack.addLast(ReversibleValue(operator, value))
                super.visitElement(element)
            }
            
            private fun visitParameter(element: ParadoxParameter) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                val defaultValue = element.defaultValue
                val conditionalStack = if(fileConditionStack.isEmpty()) null else LinkedList(fileConditionStack)
                val info = ParadoxParameterInfo(element.createPointer(file), name, defaultValue, conditionalStack)
                parameters.getOrPut(name) { SmartList() }.add(info)
                //不需要继续向下遍历
            }
            
            override fun elementFinished(element: PsiElement?) {
                if(element is ParadoxScriptParameterCondition) finishParadoxCondition()
            }
            
            private fun finishParadoxCondition() {
                fileConditionStack.removeLast()
            }
        })
        return ParadoxParameterContextInfo(parameters)
    }
    
    fun completeParameters(element: PsiElement, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        //向上找到参数上下文
        val parameterContext = ParadoxParameterSupport.findContext(element) ?: return
        val parameterContextInfo = getContextInfo(parameterContext) ?: return
        if(parameterContextInfo.parameters.isEmpty()) return
        for((parameterName, parameterInfos) in parameterContextInfo.parameters) {
            ProgressManager.checkCanceled()
            val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
            //排除当前正在输入的那个
            if(parameterInfos.size == 1 && element isSamePosition parameter) continue
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
        val config = context.config ?: return
        val completionOffset = context.parameters?.offset ?: return
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, config, completionOffset) ?: return
        val argumentNames = contextReferenceInfo.argumentNames.toMutableSet()
        val namesToDistinct = mutableSetOf<String>().synced()
        //整合查找到的所有参数上下文
        val insertSeparator = context.isKey == true && context.contextElement !is ParadoxScriptPropertyKey
        ParadoxParameterSupport.processContext(element, contextReferenceInfo) p@{ parameterContext ->
            ProgressManager.checkCanceled()
            val parameterContextInfo = getContextInfo(parameterContext) ?: return@p true
            if(parameterContextInfo.parameters.isEmpty()) return@p true
            for((parameterName, parameterInfos) in parameterContextInfo.parameters) {
                //排除已输入的
                if(parameterName in argumentNames) continue
                if(!namesToDistinct.add(parameterName)) continue
                
                val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
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
    
    /**
     * 当参数值表示整个脚本表达式时，尝试推断得到这个脚本表达式对应的CWT规则。
     */
    fun inferEntireConfig(parameterElement: ParadoxParameterElement): CwtValueConfig? {
        var result: CwtValueConfig? = null
        ParadoxParameterSupport.processContext(parameterElement) p@{ context ->
            val contextInfo = getContextInfo(context) ?: return@p true
            val config = contextInfo.getEntireConfig(parameterElement.name) ?: return@p true
            if(result == null) {
                result = config
            } else {
                if(result?.expression != config.expression) {
                    result = null
                    return@p false
                }
            }
            true
        }
        return result
    }
}