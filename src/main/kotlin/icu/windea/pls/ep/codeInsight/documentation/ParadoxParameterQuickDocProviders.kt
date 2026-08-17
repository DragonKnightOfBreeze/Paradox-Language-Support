package icu.windea.pls.ep.codeInsight.documentation

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.ep.resolve.parameter.ParadoxDefinitionParameterSupport
import icu.windea.pls.ep.resolve.parameter.ParadoxInlineScriptParameterSupport
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.text.appendPsiLinkOrUnresolved
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.definitionName
import icu.windea.pls.model.definitionTypes
import icu.windea.pls.model.inlineScriptExpression

/**
 * @see ParadoxDefinitionParameterSupport
 */
class ParadoxDefinitionParameterQuickDocProvider: ParadoxParameterQuickDocProvider {
    override fun buildDefinitionPart(element: ParadoxParameterLightElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val definitionName = element.definitionName ?: return false
        val definitionType = element.definitionTypes ?: return false
        if (definitionType.isEmpty()) return false

        // 不加上文件信息

        // 加上名字
        val name = element.name
        append(ChronicleStrings.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
        // 加上推断得到的类型信息
        val inferredType = ParadoxParameterManager.getInferredType(element)
        if (inferredType != null) {
            append(": ").append(inferredType.escapeXml())
        }
        // 加上所属定义信息
        val gameType = element.gameType
        val categories = ReferenceLinkType.CwtConfig.Categories
        appendBr().appendIndent()
        append(ChronicleBundle.message("doc.text.ofDefinition")).append(" ")
        val link = ReferenceLinkType.Definition.createLink(definitionName, definitionType.first(), gameType)
        appendPsiLinkOrUnresolved(link.escapeXml(), definitionName.escapeXml(), context = element)
        append(": ")
        val type = definitionType.first()
        val typeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, type, gameType)
        appendPsiLinkOrUnresolved(typeLink.escapeXml(), type.escapeXml())
        for ((index, t) in definitionType.withIndex()) {
            if (index == 0) continue
            append(", ")
            val subtypeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, "$type/$t", gameType)
            appendPsiLinkOrUnresolved(subtypeLink.escapeXml(), t.escapeXml())
        }

        return true
    }
}

/**
 * @see ParadoxInlineScriptParameterSupport
 */
class ParadoxInlineScriptParameterQuickDocProvider: ParadoxParameterQuickDocProvider {
    override fun buildDefinitionPart(element: ParadoxParameterLightElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val inlineScriptExpression = element.inlineScriptExpression ?: return false
        if (inlineScriptExpression.isEmpty()) return false
        val filePath = ParadoxInlineScriptManager.getInlineScriptFilePath(inlineScriptExpression) ?: return false

        // 不加上文件信息

        // 加上名字
        val name = element.name
        append(ChronicleStrings.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
        // 加上推断得到的类型信息
        val inferredType = ParadoxParameterManager.getInferredType(element)
        if (inferredType != null) {
            append(": ").append(inferredType.escapeXml())
        }
        // 加上所属内联脚本信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(ChronicleBundle.message("doc.text.ofInlineScript")).append(" ")
        val link = ReferenceLinkType.FilePath.createLink(filePath, gameType)
        appendPsiLinkOrUnresolved(link.escapeXml(), inlineScriptExpression.escapeXml(), context = element)

        return true
    }
}
