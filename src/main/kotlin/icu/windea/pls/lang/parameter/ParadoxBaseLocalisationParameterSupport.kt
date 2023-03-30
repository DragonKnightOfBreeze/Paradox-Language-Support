package icu.windea.pls.lang.parameter

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
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
        
        val contextName = "localisation#$localisationName#$parameterName"
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        return ParadoxParameterElement(element, parameterName, contextName, localisationName, readWriteAccess, gameType, project)
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxParameterElement? {
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config)
        if(localisationReferenceElement == null) return null
        val parameterName = element.name ?: return null
        val localisationName = localisationReferenceElement.name
        val contextName = "localisation#$localisationName#$parameterName"
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val configGroup = config.info.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        return ParadoxParameterElement(element, parameterName, contextName, localisationName, readWriteAccess, gameType, project)
    }
}
