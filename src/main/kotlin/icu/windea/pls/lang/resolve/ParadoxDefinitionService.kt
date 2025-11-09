package icu.windea.pls.lang.resolve

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionInheritSupport
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionModifierProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.stringValue

object ParadoxDefinitionService {
    fun resolveName(element: ParadoxScriptDefinitionElement, typeKey: String, typeConfig: CwtTypeConfig): String {
        // NOTE 2.0.6 inline logic is not applied here
        return when {
            // use type key (aka file name without extension), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameFromFile -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // use type key (aka property name), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameField == null -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // force empty (aka anonymous)
            typeConfig.nameField == "" -> ""
            // from property value (which should be a string)
            typeConfig.nameField == "-" -> element.castOrNull<ParadoxScriptProperty>()?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
            // from specific property value in definition declaration (while the property name is declared by config property "name_field")
            else -> element.findProperty(typeConfig.nameField)?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
        }
    }

    fun resolveName(node: LighterASTNode, tree: LighterAST, typeKey: String, typeConfig: CwtTypeConfig): String? {
        // NOTE 2.0.6 inline logic is not applied here
        return when {
            // use type key (aka file name without extension), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameFromFile -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // use type key (aka property name), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameField == null -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // force empty (aka anonymous)
            typeConfig.nameField == "" -> ""
            // from property value (which should be a string)
            typeConfig.nameField == "-" -> ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(node, tree)
            // from specific property value in definition declaration (while the property name is declared by config property "name_field")
            else -> ParadoxScriptLightTreeUtil.findPropertyFromPropertyNode(node, tree, typeConfig.nameField!!)
                ?.let { ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(it, tree) }
        }
    }

    /**
     * @see ParadoxDefinitionInheritSupport.getSuperDefinition
     */
    fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionInheritSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getSuperDefinition(definition, definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionModifierProvider.getModifierCategories
     */
    fun getModifierCategories(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionModifierProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getModifierCategories(definition, definitionInfo)
        }
    }
}
