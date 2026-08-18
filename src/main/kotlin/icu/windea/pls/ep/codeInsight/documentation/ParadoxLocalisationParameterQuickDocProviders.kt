package icu.windea.pls.ep.codeInsight.documentation

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unknown
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.text.appendPsiLinkOrUnresolved
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.ChronicleStrings

class ParadoxBaseLocalisationParameterQuickDocProvider: ParadoxLocalisationParameterQuickDocProvider {
    override fun buildDefinitionPart(element: ParadoxLocalisationParameterLightElement, builder: DocumentationBuilder): Boolean = with(builder) {
        // 不加上文件信息

        // 加上名字
        val name = element.name
        append(ChronicleStrings.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")

        // 加上所属本地化信息
        val gameType = element.gameType
        br().indent()
        append(ChronicleBundle.message("doc.text.ofLocalisation")).append(" ")
        val nameOrUnknown = element.localisationName.or.unknown()
        val link = ReferenceLinkType.Localisation.createLink(nameOrUnknown, gameType)
        appendPsiLinkOrUnresolved(link.escapeXml(), nameOrUnknown.escapeXml(), context = element)

        return true
    }
}
