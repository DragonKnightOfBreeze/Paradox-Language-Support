package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxBaseLocalisationParameterSupport : ParadoxLocalisationParameterSupport {
    companion object {
        @JvmField val localisationNameKey = Key.create<String>("paradox.parameterElement.localisationName")
    }
    
    override fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxParameterElement? {
        val localisationName = localisationElement.name
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val contextName = localisationName
        val contextIcon = PlsIcons.Localisation
        val key = "localisation#$localisationName#$name"
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val resolved = ParadoxParameterElement(localisationElement, name, contextName, contextIcon, key, null, readWriteAccess, gameType, project)
        resolved.putUserData(localisationNameKey, localisationName)
        return resolved
    }
    
    override fun resolveParameter(element: ParadoxLocalisationPropertyReference): ParadoxParameterElement? {
        val localisationElement = element.parentOfType<ParadoxLocalisationProperty>(withSelf = false) ?: return null
        val name = element.name
        val localisationName = localisationElement.name
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val parameterNames = ParadoxLocalisationParameterHandler.getParameterNames(localisationElement)
        if(name !in parameterNames) return null
        val contextName = localisationName
        val contextIcon = PlsIcons.Localisation
        val key = "localisation#$localisationName#$name"
        val rangeInParent = TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val resolved = ParadoxParameterElement(element, name, contextName, contextIcon, key, rangeInParent, readWriteAccess, gameType, project)
        resolved.putUserData(localisationNameKey, localisationName)
        return resolved
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.LocalisationParameter) return null
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config) ?: return null
        val name = rangeInElement?.substring(element.text) ?: element.name
        val localisationName = localisationReferenceElement.name
        val contextName = localisationName
        val contextIcon = PlsIcons.Localisation
        val key = "localisation#$localisationName#$name"
        val rangeInParent = rangeInElement ?: TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val configGroup = config.info.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val resolved = ParadoxParameterElement(element, name, contextName, contextIcon, key, rangeInParent, readWriteAccess, gameType, project)
        resolved.putUserData(localisationNameKey, localisationName)
        return resolved
    }
    
    override fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        if(!element.key.startsWith("localisation#")) return false
        
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        
        //加上所属本地化信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofLocalisation")).append(" ")
        appendLocalisationLink(gameType, element.getUserData(localisationNameKey).orUnknown(), element)
        return true
    }
}
