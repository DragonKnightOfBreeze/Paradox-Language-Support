package icu.windea.pls.ep.parameter

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.documentation.DocumentationBuilder
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.core.util.unknown
import icu.windea.pls.lang.documentation.appendBr
import icu.windea.pls.lang.documentation.appendIndent
import icu.windea.pls.lang.documentation.appendPsiLinkOrUnresolved
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxLocalisationParameterManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

class ParadoxBaseLocalisationParameterSupport : ParadoxLocalisationParameterSupport {
    override fun resolveParameter(localisationElement: ParadoxLocalisationProperty, name: String): ParadoxLocalisationParameterElement? {
        val localisationName = localisationElement.name
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val resolved = ParadoxLocalisationParameterElement(localisationElement, name, localisationName, readWriteAccess, gameType, project)
        return resolved
    }

    override fun resolveParameter(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterElement? {
        val localisationElement = element.parentOfType<ParadoxLocalisationProperty>(withSelf = false) ?: return null
        val name = element.name
        val localisationName = localisationElement.name
        val file = localisationElement.containingFile
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val parameterNames = ParadoxLocalisationParameterManager.getParameterNames(localisationElement)
        if (name !in parameterNames) return null
        val rangeInParent = TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val resolved = ParadoxLocalisationParameterElement(element, name, localisationName, readWriteAccess, gameType, project)
        return resolved
    }

    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxLocalisationParameterElement? {
        if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.LocalisationParameter) return null
        val localisationReferenceElement = ParadoxLocalisationParameterManager.getLocalisationReferenceElement(element, config) ?: return null
        val name = rangeInElement?.substring(element.text) ?: element.name
        val localisationName = localisationReferenceElement.name
        val rangeInParent = rangeInElement ?: TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val configGroup = config.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val resolved = ParadoxLocalisationParameterElement(element, name, localisationName, readWriteAccess, gameType, project)
        return resolved
    }

    override fun buildDocumentationDefinition(element: ParadoxLocalisationParameterElement, builder: DocumentationBuilder): Boolean = with(builder) {
        //不加上文件信息

        //加上名字
        val name = element.name
        append(PlsStringConstants.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")

        //加上所属本地化信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofLocalisation")).append(" ")
        val nameOrUnknown = element.localisationName.or.unknown()
        val link = ReferenceLinkType.Localisation.createLink(nameOrUnknown, gameType)
        appendPsiLinkOrUnresolved(link.escapeXml(), nameOrUnknown.escapeXml(), context = element)
        return true
    }
}
