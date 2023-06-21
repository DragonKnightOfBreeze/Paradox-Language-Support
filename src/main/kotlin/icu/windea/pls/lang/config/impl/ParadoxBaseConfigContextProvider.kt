package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取直接的CWT规则上下文。
 */
class ParadoxBaseConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(contextElement: PsiElement, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        val fileInfo = vFile.fileInfo ?: return null
        val elementPath = ParadoxElementPathHandler.getFromFile(contextElement) ?: return null
        val definition = contextElement.findParentDefinition()
        val type = getContextType(contextElement)
        if(definition == null) {
            return ParadoxConfigContext(type, fileInfo, elementPath)
        } else {
            val definitionInfo = definition.definitionInfo ?: return null
            val elementPathFromDefinition = elementPath //TODO
            return ParadoxConfigContext(type, fileInfo, elementPath, definitionInfo, elementPathFromDefinition)
        }
    }
    
    private fun getContextType(contextElement: PsiElement): ParadoxConfigContext.Type {
        return when {
            contextElement is PsiFile -> ParadoxConfigContext.Type.InFile
            contextElement is ParadoxScriptRootBlock ->  ParadoxConfigContext.Type.InFile
            contextElement is ParadoxScriptProperty -> {
                when {
                    contextElement.parent is ParadoxScriptRootBlock -> ParadoxConfigContext.Type.InFile
                    else -> ParadoxConfigContext.Type.InClause
                }
            }
            else -> {
                val expressionElement = contextElement.parentOfType<ParadoxScriptExpressionElement>()
                when {
                    expressionElement is ParadoxScriptValue && expressionElement.isPropertyValue() -> ParadoxConfigContext.Type.PropertyValue
                    else -> ParadoxConfigContext.Type.InClause
                }
            }
        }
    }
}
