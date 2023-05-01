package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxBaseLocalisationParameterSupport : ParadoxLocalisationParameterSupport {
    override fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxParameterElement? {
        val localisationElement = element.parentOfType<ParadoxLocalisationProperty>(withSelf = false)
        if(localisationElement == null) return null
        
        val parameterName = element.name
        val localisationName = localisationElement.name
        
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        
        val parameterNames = ParadoxLocalisationParameterHandler.getParameterNames(localisationElement)
        if(parameterName !in parameterNames) return null
        
        val contextKey = "localisation#$localisationName#$parameterName"
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        return ParadoxParameterElement(element, parameterName, localisationName, contextKey, readWriteAccess, gameType, project)
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxParameterElement? {
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config)
        if(localisationReferenceElement == null) return null
        val parameterName = element.name ?: return null
        val localisationName = localisationReferenceElement.name
        val contextKey = "localisation#$localisationName#$parameterName"
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val configGroup = config.info.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        return ParadoxParameterElement(element, parameterName, localisationName, contextKey, readWriteAccess, gameType, project)
    }
    
    override fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        if(!element.contextKey.startsWith("localisation#")) return false
        
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        
        //加上所属本地化信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofLocalisation")).append(" ")
        appendLocalisationLink(gameType, element.contextName, element)
        return true
    }
}
