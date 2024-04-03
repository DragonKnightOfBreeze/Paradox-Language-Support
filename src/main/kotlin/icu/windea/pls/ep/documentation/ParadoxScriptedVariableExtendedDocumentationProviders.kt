package icu.windea.pls.ep.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class ParadoxBaseScriptedVariableExtendedDocumentationProvider : ParadoxScriptedVariableExtendedDocumentationProvider {
    override fun getDocumentation(element: ParadoxScriptScriptedVariable): String? {
        val name = element.name
        if(name.isNullOrEmpty()) return null
        if(name.isParameterized()) return null
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = getConfigGroup(project, gameType)
        val config = configGroup.extendedScriptedVariables.getByTemplate(name, element, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}