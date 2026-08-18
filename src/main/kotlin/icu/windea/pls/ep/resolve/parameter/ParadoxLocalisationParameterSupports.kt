package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.parentOfType
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxLocalisationParameterManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

class ParadoxBaseLocalisationParameterSupport : ParadoxLocalisationParameterSupport {
    override fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterLightElement? {
        val localisationName = localisationElement.name.orNull() ?: return null
        val localisationIcon = ChronicleIcons.Nodes.Localisation
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val readWriteAccess = ReadWriteAccess.Read
        val resolved = ParadoxLocalisationParameterLightElement(localisationElement, name, localisationName, localisationIcon, readWriteAccess, gameType, project)
        return resolved
    }

    override fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterLightElement? {
        val name = element.name.orNull() ?: return null
        val localisationElement = element.parentOfType<ParadoxLocalisationProperty>(withSelf = false) ?: return null
        val localisationName = localisationElement.name.orNull() ?: return null
        val localisationIcon = ChronicleIcons.Nodes.Localisation
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val readWriteAccess = ReadWriteAccess.Read
        val resolved = ParadoxLocalisationParameterLightElement(element, name, localisationName, localisationIcon, readWriteAccess, gameType, project)
        return resolved
    }

    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterLightElement? {
        if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.LocalisationParameter) return null
        val name = rangeInExpression?.substring(element.value) ?: element.name
        val localisationReferenceElement = ParadoxLocalisationParameterManager.getLocalisationReferenceElement(element, config) ?: return null
        val localisationName = localisationReferenceElement.name.orNull() ?: return null
        val localisationIcon = ChronicleIcons.Nodes.Localisation
        val readWriteAccess = ReadWriteAccess.Write
        val configGroup = config.configGroup
        val gameType = configGroup.gameType
        val project = configGroup.project
        val resolved = ParadoxLocalisationParameterLightElement(element, name, localisationName, localisationIcon, readWriteAccess, gameType, project)
        return resolved
    }

}
