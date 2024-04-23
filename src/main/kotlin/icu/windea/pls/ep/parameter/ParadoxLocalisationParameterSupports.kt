package icu.windea.pls.ep.parameter

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxBaseLocalisationParameterSupport : ParadoxLocalisationParameterSupport {
    override fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterElement? {
        val localisationName = localisationElement.name
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val resolved = ParadoxLocalisationParameterElement(localisationElement, name, localisationName, null, readWriteAccess, gameType, project)
        return resolved
    }
    
    override fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxLocalisationParameterElement? {
        val localisationElement = element.parentOfType<ParadoxLocalisationProperty>(withSelf = false) ?: return null
        val name = element.name
        val localisationName = localisationElement.name
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val parameterNames = ParadoxLocalisationParameterHandler.getParameterNames(localisationElement)
        if(name !in parameterNames) return null
        val rangeInParent = TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val resolved = ParadoxLocalisationParameterElement(element, name, localisationName, rangeInParent, readWriteAccess, gameType, project)
        return resolved
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterElement? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataTypes.LocalisationParameter) return null
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config) ?: return null
        val name = rangeInElement?.substring(element.text) ?: element.name
        val localisationName = localisationReferenceElement.name
        val rangeInParent = rangeInElement ?: TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val configGroup = config.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val resolved = ParadoxLocalisationParameterElement(element, name, localisationName, rangeInParent, readWriteAccess, gameType, project)
        return resolved
    }
    
    override fun buildDocumentationDefinition(element: ParadoxLocalisationParameterElement, builder: StringBuilder): Boolean = with(builder) {
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        
        //加上所属本地化信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofLocalisation")).append(" ")
        val localisationName = element.localisationName
        appendLocalisationLink(gameType, localisationName.orUnknown(), element)
        return true
    }
}