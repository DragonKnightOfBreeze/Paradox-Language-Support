package icu.windea.pls.lang.parameter

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxInlineScriptParameterSupport : ParadoxParameterSupport {
    companion object {
        @JvmField val inlineScriptExpressionKey = Key.create<String>("paradox.parameterElement.inlineScriptExpression")
    }
    
    override fun supports(context: ParadoxScriptDefinitionElement): Boolean {
        return context is ParadoxScriptFile && ParadoxInlineScriptHandler.getInlineScriptExpression(context) != null
    }
    
    override fun findContext(element: PsiElement, file: PsiFile?): ParadoxScriptDefinitionElement? {
        if(element !is ParadoxParameter && element !is ParadoxArgument) return null
        val finalFile = file ?: element.containingFile
        if(finalFile is ParadoxScriptFile && ParadoxInlineScriptHandler.getInlineScriptExpression(finalFile) != null) return finalFile
        return null
    }
    
    override fun resolveParameter(name: String, element: PsiElement, context: ParadoxScriptDefinitionElement): ParadoxParameterElement? {
        val file = context as ParadoxScriptFile
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpression(file) ?: return null
        val readWriteAccess = getReadWriteAccess(element)
        val contextKey = "inline_script@$expression"
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val result = ParadoxParameterElement(element, name, expression, contextKey, readWriteAccess, gameType, project)
        result.putUserData(inlineScriptExpressionKey, expression)
        return result
    }
    
    override fun resolveParameterFromInvocationExpression(name: String, element: ParadoxScriptProperty, config: CwtPropertyConfig): ParadoxParameterElement? {
        val inlineConfig = config.inlineableConfig as? CwtInlineConfig ?: return null
        if(inlineConfig.name != "inline_script") return null
        val propertyValue = element.propertyValue ?: return null
        val expression = ParadoxInlineScriptHandler.getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val contextKey = "inline_script@$expression"
        val gameType = config.info.configGroup.gameType ?: return null
        val project = config.info.configGroup.project
        val result = ParadoxParameterElement(element, name, expression, contextKey, readWriteAccess, gameType, project)
        result.putUserData(inlineScriptExpressionKey, expression)
        return result
    }
    
    override fun processContextFromInvocationExpression(element: ParadoxScriptProperty, config: CwtPropertyConfig, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val inlineConfig = config.inlineableConfig as? CwtInlineConfig ?: return false
        if(inlineConfig.name != "inline_script") return false
        val propertyValue = element.propertyValue ?: return false
        val expression = ParadoxInlineScriptHandler.getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return false
        ParadoxInlineScriptHandler.processInlineScript(expression, element, config.info.configGroup.project, processor)
        return true
    }
    
    private fun getReadWriteAccess(element: PsiElement) = when {
        element is ParadoxParameter -> ReadWriteAccessDetector.Access.Read
        element is ParadoxArgument -> ReadWriteAccessDetector.Access.Write
        else -> ReadWriteAccessDetector.Access.ReadWrite
    }
    
    override fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        val inlineScriptExpression = element.getUserData(inlineScriptExpressionKey) ?: return false
        if(inlineScriptExpression.isEmpty()) return false
        val filePath = ParadoxInlineScriptHandler.getInlineScriptFilePath(inlineScriptExpression) ?: return false
        
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsDocBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        
        //加上所属定义信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsDocBundle.message("ofInlineScript")).append(" ")
        appendFilePathLink(inlineScriptExpression, gameType, filePath, element, true)
        return true
    }
}