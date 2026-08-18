package icu.windea.pls.ep.codeInsight.documentation

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orNull
import icu.windea.pls.core.pass
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.ep.resolve.modifier.ParadoxEconomicCategoryModifierSupport
import icu.windea.pls.ep.resolve.modifier.ParadoxTemplateModifierSupport
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.text.appendPsiLink
import icu.windea.pls.lang.text.appendPsiLinkOrUnresolved
import icu.windea.pls.lang.util.ParadoxEconomicCategoryManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.economicCategoryInfo
import icu.windea.pls.model.economicCategoryModifierInfo
import icu.windea.pls.model.modifierConfig
import icu.windea.pls.model.templateExpression
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/***
 * 适用于从模板表达式生成的修正。
 *
 * @see ParadoxTemplateModifierSupport
 */
class ParadoxTemplateModifierQuickDocProvider : ParadoxModifierQuickDocProvider {
    override fun buildDefinitionPart(element: ParadoxModifierLightElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val modifierConfig = element.modifierConfig ?: return false
        val templateExpression = element.templateExpression ?: return false

        val configGroup = modifierConfig.configGroup
        val gameType = configGroup.gameType
        // 加上名字
        val name = element.name
        append(ChronicleStrings.modifierPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
        // 加上模板信息
        val templateConfigExpression = modifierConfig.template
        if (templateConfigExpression.expressionString.isNotEmpty()) {
            val templateString = templateConfigExpression.expressionString

            br().indent()
            append(ChronicleBundle.message("doc.text.fromTemplate")).append(" ")
            val templateLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.modifiers, templateString, gameType)
            appendPsiLinkOrUnresolved(templateLink.escapeXml(), templateString.escapeXml())

            // 加上生成源信息
            val snippetNodes = templateExpression.nodes.filterIsInstance<ParadoxTemplateSnippetNode>()
            if (snippetNodes.isNotEmpty()) {
                for (snippetNode in snippetNodes) {
                    ProgressManager.checkCanceled()
                    br().indent()
                    val configExpression = snippetNode.configExpression
                    when (configExpression.type) {
                        CwtDataTypes.Definition -> {
                            val definitionName = snippetNode.text
                            val definitionType = configExpression.metadata.value!!
                            val definitionTypes = definitionType.split('.')
                            append(ChronicleBundle.message("doc.text.generatedFromDefinition"))
                            append(" ")
                            val link = ReferenceLinkType.Definition.createLink(definitionName, definitionType, gameType)
                            appendPsiLinkOrUnresolved(link.escapeXml(), definitionName.escapeXml(), context = element)
                            append(": ")

                            val type = definitionTypes.first()
                            val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.types, type, gameType)
                            appendPsiLinkOrUnresolved(typeLink.escapeXml(), type.escapeXml())
                            for ((index, t) in definitionTypes.withIndex()) {
                                if (index == 0) continue
                                append(", ")
                                val subtypeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.types, "$type/$t", gameType)
                                appendPsiLinkOrUnresolved(subtypeLink.escapeXml(), t.escapeXml())
                            }
                        }
                        CwtDataTypes.EnumValue -> {
                            val enumValueName = snippetNode.text
                            val enumName = configExpression.metadata.value!!
                            append(ChronicleBundle.message("doc.text.generatedFromEnumValue"))
                            append(" ")
                            if (configGroup.enums.containsKey(enumName)) {
                                val link = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.enums, "$enumName/$enumValueName", gameType)
                                appendPsiLinkOrUnresolved(link.escapeXml(), enumName.escapeXml(), context = element)
                                append(": ")
                                val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.enums, enumName, gameType)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml(), context = element)
                            } else if (configGroup.complexEnums.containsKey(enumName)) {
                                append(enumValueName.escapeXml())
                                append(": ")
                                val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.complexEnums, enumName, gameType)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml(), context = element)
                            } else {
                                // unexpected
                                append(enumValueName.escapeXml())
                                append(": ")
                                append(enumName.escapeXml())
                            }
                        }
                        CwtDataTypes.Value -> {
                            val dynamicValueType = snippetNode.text
                            val valueName = configExpression.metadata.value!!
                            append(ChronicleBundle.message("doc.text.generatedFromDynamicValue"))
                            if (configGroup.dynamicValueTypes.containsKey(valueName)) {
                                val link = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.values, "$dynamicValueType/$valueName", gameType)
                                appendPsiLinkOrUnresolved(link.escapeXml(), valueName.escapeXml(), context = element)
                                append(": ")
                                val typeLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.values, dynamicValueType, gameType)
                                appendPsiLinkOrUnresolved(typeLink.escapeXml(), valueName.escapeXml(), context = element)
                            } else {
                                append(valueName.escapeXml())
                                append(": ")
                                append(dynamicValueType.escapeXml())
                            }
                        }
                        else -> pass()
                    }
                }
            }
        }

        return true
    }

    override fun buildDefinitionPartForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = with(builder) {
        val modifiers = definitionInfo.modifiers
        if (modifiers.isEmpty()) return false

        val gameType = definitionInfo.gameType
        for (modifier in modifiers) {
            ProgressManager.checkCanceled()
            br()
            append(ChronicleStrings.generatedModifierPrefix).append(" ")
            val link = ReferenceLinkType.Modifier.createLink(modifier.name, gameType)
            appendPsiLink(link.escapeXml(), modifier.name.escapeXml())
            // 2.1.8 文本可能过长，因此这里目前改为不显示
            // append(" ")
            // grayed {
            //     append(ChronicleBundle.message("fromTemplate"))
            //     append(" ")
            //     val key = modifier.config.name
            //     val templateLink = ReferenceLinkType.CwtConfig.createLink(ReferenceLinkType.CwtConfig.Categories.modifiers, key, gameType)
            //     appendPsiLinkOrUnresolved(templateLink.escapeXml(), key.escapeXml())
            // }
        }

        return true
    }
}

/**
 * （仅限 Stellaris）适用于从经济分类（`economic_category`）生成的修正.
 *
 * @see ParadoxEconomicCategoryModifierSupport
 */
@ForGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierQuickDocProvider : ParadoxModifierQuickDocProvider {
    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun buildDefinitionPart(element: ParadoxModifierLightElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val economicCategoryInfo = element.economicCategoryInfo ?: return false
        val modifierInfo = element.economicCategoryModifierInfo ?: return false

        val gameType = element.gameType
        // 加上名字
        val name = element.name.orNull()
        append(ChronicleStrings.modifierPrefix).append(" <b>").append(name?.escapeXml().or.anonymous()).append("</b>")
        // 加上经济分类信息
        br().indent()
        append(ChronicleBundle.message("doc.text.generatedFromEconomicCategory"))
        append(" ")
        val ecLink = ReferenceLinkType.Definition.createLink(economicCategoryInfo.name, ParadoxDefinitionTypes.economicCategory, gameType)
        appendPsiLinkOrUnresolved(ecLink.escapeXml(), economicCategoryInfo.name.escapeXml(), context = element)
        if (modifierInfo.resource != null) {
            br().indent()
            append(ChronicleBundle.message("doc.text.generatedFromResource"))
            append(" ")
            val resourceLink = ReferenceLinkType.Definition.createLink(modifierInfo.resource, ParadoxDefinitionTypes.resource, gameType)
            appendPsiLinkOrUnresolved(resourceLink.escapeXml(), modifierInfo.resource.escapeXml(), context = element)
        }

        return true
    }

    override fun buildDefinitionPartForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = with(builder) {
        val configGroup = definitionInfo.configGroup
        val project = configGroup.project
        val selector = ParadoxDefinitionSearch.selector(project, definition).contextSensitive()
            .withConstraint(ParadoxDefinitionIndexConstraint.EconomicCategory)
        val economicCategory = ParadoxDefinitionSearch.searchProperty(definitionInfo.name, ParadoxDefinitionTypes.economicCategory, selector).find() ?: return false
        val economicCategoryInfo = ParadoxEconomicCategoryManager.getInfo(economicCategory) ?: return false

        val gameType = definitionInfo.gameType
        for (modifierInfo in economicCategoryInfo.modifiers) {
            ProgressManager.checkCanceled()
            br()
            append(ChronicleStrings.generatedModifierPrefix).append(" ")
            val modifierLink = ReferenceLinkType.Modifier.createLink(modifierInfo.name, gameType)
            appendPsiLink(modifierLink.escapeXml(), modifierInfo.name.escapeXml())
            if (modifierInfo.resource != null) {
                append(" ")
                grayed {
                    append(ChronicleBundle.message("doc.text.fromResource"))
                    append(" ")
                    val resourceLink = ReferenceLinkType.Definition.createLink(modifierInfo.resource, ParadoxDefinitionTypes.resource, gameType)
                    appendPsiLinkOrUnresolved(resourceLink.escapeXml(), modifierInfo.resource.escapeXml(), context = definition)
                }
            }
        }

        return true
    }
}
