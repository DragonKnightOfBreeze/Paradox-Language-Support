package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeImagesConfig
import icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configGroup.modifiers
import icu.windea.pls.config.configGroup.type2ModifiersMap
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.core.util.takeWithOperator

internal class CwtTypeConfigResolverImpl : CwtTypeConfig.Resolver {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtTypeConfig? {
        val configGroup = config.configGroup
        val name = config.key.removeSurroundingOrNull("type[", "]")?.orNull()?.intern() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid type config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propElements.groupBy { it.key }
        var paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) {it.stringValue?.removePrefix("game/")?.normalizePath()?.intern() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.removePrefix(".")?.intern()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.removePrefix("game/")?.normalizePath()?.intern() }.optimized()
        val baseType = propGroup.getOne("base_type")?.stringValue
        val nameField = propGroup.getOne("name_field")?.stringValue
        val typeKeyPrefix = propGroup.getOne("type_key_prefix")?.stringValue
        val nameFromFile = propGroup.getOne("name_from_file")?.booleanValue ?: false
        val typePerFile = propGroup.getOne("type_per_file")?.booleanValue ?: false
        val unique = propGroup.getOne("unique")?.booleanValue ?: false
        val severity = propGroup.getOne("severity")?.stringValue
        val skipRootKey = propGroup.getAll("skip_root_key").map { prop ->
            // 出于一点点的性能考虑，这里保留大小写，后面匹配路径时会忽略掉
            prop.stringValue?.let { listOf(it) } ?: prop.values?.mapNotNull { it.stringValue }?.optimized().orEmpty()
        }
        val typeKeyFilter = config.optionData { typeKeyFilter }
        val typeKeyRegex = config.optionData { typeKeyRegex }
        val startsWith = config.optionData { startsWith }
        val graphRelatedTypes = config.optionData { graphRelatedTypes }
        val subtypes = propElements.mapNotNull { CwtSubtypeConfig.resolve(it) }.associateBy { it.name }.optimized()
        val localisation = propGroup.getOne("localisation")?.let { CwtTypeLocalisationConfig.resolve(it) }
        val images = propGroup.getOne("images")?.let { CwtTypeImagesConfig.resolve(it) }

        if (configGroup.gameType.subDirectoryEntries.isNotNullOrEmpty() && paths.isNotNullOrEmpty()) {
            val temp = sortedSetOf<String>()
            for (path in paths) {
                temp += path
                for (subDirectory in configGroup.gameType.subDirectoryEntries) {
                    temp += "$subDirectory/$path"
                }
            }
            paths = temp
        }

        // merge all properties named 'modifiers'
        propGroup.getAll("modifiers").forEach { prop ->
            for (p in prop.properties.orEmpty()) {
                val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
                if (subtypeName != null) {
                    for (pp in p.properties.orEmpty()) {
                        val typeExpression = "$name.$subtypeName"
                        val modifierConfig = CwtModifierConfig.resolveFromDefinitionModifier(pp, pp.key, typeExpression) ?: continue
                        configGroup.modifiers[modifierConfig.name] = modifierConfig
                        configGroup.type2ModifiersMap.getOrPut(typeExpression) { mutableMapOf() }[pp.key] = modifierConfig
                    }
                } else {
                    val typeExpression = name
                    val modifierConfig = CwtModifierConfig.resolveFromDefinitionModifier(p, p.key, typeExpression) ?: continue
                    configGroup.modifiers[modifierConfig.name] = modifierConfig
                    configGroup.type2ModifiersMap.getOrPut(typeExpression) { mutableMapOf() }[p.key] = modifierConfig
                }
            }
        }

        logger.debug { "Resolved type config (name: $name).".withLocationPrefix(config) }
        return CwtTypeConfigImpl(
            config, name, baseType,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
            nameField, typeKeyPrefix, nameFromFile, typePerFile, unique, severity,
            skipRootKey, typeKeyFilter, typeKeyRegex, startsWith,
            graphRelatedTypes, subtypes, localisation, images
        )
    }
}

private class CwtTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val baseType: String?,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val pathPatterns: Set<String>,
    override val nameField: String?,
    override val typeKeyPrefix: String?,
    override val nameFromFile: Boolean,
    override val typePerFile: Boolean,
    override val unique: Boolean,
    override val severity: String?,
    override val skipRootKey: List<List<String>>?,
    override val typeKeyFilter: ReversibleValue<Set<String>>?,
    override val typeKeyRegex: Regex?,
    override val startsWith: String?,
    override val graphRelatedTypes: Set<String>?,
    override val subtypes: Map<String, CwtSubtypeConfig>,
    override val localisation: CwtTypeLocalisationConfig?,
    override val images: CwtTypeImagesConfig?,
) : UserDataHolderBase(), CwtTypeConfig {
    override val possibleTypeKeys: Set<String> by lazy {
        caseInsensitiveStringSet().apply {
            typeKeyFilter?.takeWithOperator()?.let { addAll(it) }
            subtypes.values.forEach { subtype -> subtype.typeKeyFilter?.takeWithOperator()?.let { addAll(it) } }
        }.optimized()
    }

    override val typeKeyPrefixConfig: CwtValueConfig? by lazy {
        config.properties?.find { it.key == "type_key_prefix" }?.valueConfig?.also {
            it.tagType = CwtTagType.TypeKeyPrefix
            it.pointer.element?.bindConfig(it)
        }
    }

    override fun toString() = "CwtTypeConfigImpl(name='$name')"
}
