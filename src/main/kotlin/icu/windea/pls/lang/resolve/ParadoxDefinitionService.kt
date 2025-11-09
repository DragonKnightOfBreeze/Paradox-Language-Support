package icu.windea.pls.lang.resolve

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.type2ModifiersMap
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionInheritSupport
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionModifierProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.stringValue

object ParadoxDefinitionService {
    private val ParadoxDefinitionInfo.subtypeConfigsCache by createKey(ParadoxDefinitionInfo.Keys) {
        CacheBuilder().build<Int, List<CwtSubtypeConfig>>().cancelable() // TODO may be trackable
    }
    private val ParadoxDefinitionInfo.declarationConfigsCache by createKey(ParadoxDefinitionInfo.Keys) {
        CacheBuilder().build<Int, Any>().cancelable()
    }

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

    fun resolveSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, matchOptions: Int = ParadoxMatchOptions.Default): List<CwtSubtypeConfig> {
        if (definitionInfo.typeConfig.subtypes.isEmpty()) return emptyList()
        return definitionInfo.subtypeConfigsCache.get(matchOptions) { doResolveSubtypeConfigs(definitionInfo, matchOptions) }
    }

    private fun doResolveSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, matchOptions: Int): List<CwtSubtypeConfig> {
        val subtypesConfig = definitionInfo.typeConfig.subtypes
        val result = buildList {
            for (subtypeConfig in subtypesConfig.values) {
                if (ParadoxConfigMatchService.matchesSubtype(definitionInfo.element, definitionInfo.typeKey, subtypeConfig, this, definitionInfo.configGroup, matchOptions)) {
                    this += subtypeConfig
                }
            }
        }
        return result.optimized() // optimized to optimize memory
    }

    fun resolveDeclaration(definitionInfo: ParadoxDefinitionInfo, matchOptions: Int = ParadoxMatchOptions.Default): CwtPropertyConfig? {
        return definitionInfo.declarationConfigsCache.get(matchOptions) { doResolveDeclaration(definitionInfo, matchOptions) ?: EMPTY_OBJECT }.castOrNull()
    }

    private fun doResolveDeclaration(definitionInfo: ParadoxDefinitionInfo, matchOptions: Int): CwtPropertyConfig? {
        val declarationConfig = definitionInfo.configGroup.declarations.get(definitionInfo.type) ?: return null
        val subtypes = resolveSubtypeConfigs(definitionInfo, matchOptions).map { it.name }
        val declarationConfigContext = CwtDeclarationConfigContextProvider.getContext(definitionInfo.element, definitionInfo.name, definitionInfo.type, subtypes, definitionInfo.configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    fun resolveRelatedLocalisations(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedLocalisationInfo> {
        val mergedConfigs = definitionInfo.typeConfig.localisation?.getConfigs(definitionInfo.subtypes) ?: return emptyList()
        val result = buildList(mergedConfigs.size) {
            for (config in mergedConfigs) {
                val locationExpression = CwtLocalisationLocationExpression.resolve(config.value)
                val info = ParadoxDefinitionInfo.RelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
                this += info
            }
        }
        return result
    }

    fun resolveRelatedImages(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedImageInfo> {
        val mergedConfigs = definitionInfo.typeConfig.images?.getConfigs(definitionInfo.subtypes) ?: return emptyList()
        val result = buildList(mergedConfigs.size) {
            for (config in mergedConfigs) {
                val locationExpression = CwtImageLocationExpression.resolve(config.value)
                val info = ParadoxDefinitionInfo.RelatedImageInfo(config.key, locationExpression, config.required, config.primary)
                this += info
            }
        }
        return result
    }

    fun resolveModifiers(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.ModifierInfo> {
        val result = buildList {
            definitionInfo.configGroup.type2ModifiersMap.get(definitionInfo.type)?.forEach { (_, v) ->
                this += ParadoxDefinitionInfo.ModifierInfo(CwtTemplateExpressionManager.extract(v.template, definitionInfo.name), v)
            }
            for (subtype in definitionInfo.subtypes) {
                definitionInfo.configGroup.type2ModifiersMap.get("${definitionInfo.type}.$subtype")?.forEach { (_, v) ->
                    this += ParadoxDefinitionInfo.ModifierInfo(CwtTemplateExpressionManager.extract(v.template, definitionInfo.name), v)
                }
            }
        }
        return result
    }

    /**
     * @see ParadoxDefinitionInheritSupport.getSuperDefinition
     */
    fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionInheritSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getSuperDefinition(definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionModifierProvider.getModifierCategories
     */
    fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionModifierProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getModifierCategories(definitionInfo)
        }
    }
}
