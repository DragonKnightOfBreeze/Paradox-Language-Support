@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

/**
 * @property name string
 * @property baseType (property) path: string
 * @property paths (property) path: string
 * @property pathFile (property) path_file: string
 * @property pathExtension (property) path_extension: string
 * @property pathStrict (property) path_strict: boolean
 * @property pathPatterns (property*) path_pattern: string
 * @property nameField (property) name_field: string
 * @property typeKeyPrefix (property) type_key_prefix: string
 * @property nameFromFile (property) name_from_file: boolean
 * @property typePerFile (property) type_per_file: boolean
 * @property typeKeyFilter (option*) type_key_filter: string | string[]
 * @property typeKeyRegex (option) type_key_regex: string
 * @property startsWith (option) starts_with: string
 * @property unique (property) unique: boolean
 * @property severity (property) severity: string:severity
 * @property skipRootKey (property*) skip_root_key: string | string[]
 * @property graphRelatedTypes (option) graph_related_types: graphRelatedType[]
 * @property subtypes (property*) subtype[?]: subtypeInfo
 * @property localisation (property*) localisation: localisationInfo
 * @property images (property*) images: imagesInfo
 */
interface CwtTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtFilePathMatchableConfig {
    val name: String
    val baseType: String?
    override val paths: Set<String>
    override val pathFile: String?
    override val pathExtension: String?
    override val pathStrict: Boolean
    override val pathPatterns: Set<String>
    val nameField: String?
    val typeKeyPrefix: String?
    val nameFromFile: Boolean
    val typePerFile: Boolean
    val unique: Boolean
    val severity: String?
    val skipRootKey: List<List<@CaseInsensitive String>>?
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    val typeKeyRegex: Regex?
    val startsWith: @CaseInsensitive String?
    val graphRelatedTypes: Set<String>?
    val subtypes: Map<String, CwtSubtypeConfig>
    val localisation: CwtTypeLocalisationConfig?
    val images: CwtTypeImagesConfig?

    object Keys : KeyRegistry()

    companion object {
        fun resolve(config: CwtPropertyConfig): CwtTypeConfig? = doResolve(config)
    }
}

//Accessors

val CwtTypeConfig.possibleRootKeys: Set<String> by createKey(CwtTypeConfig.Keys) {
    caseInsensitiveStringSet().apply {
        typeKeyFilter?.takeIfTrue()?.let { addAll(it) }
        subtypes.values.forEach { subtype -> subtype.typeKeyFilter?.takeIfTrue()?.let { addAll(it) } }
    }.optimized()
}

val CwtTypeConfig.typeKeyPrefixConfig: CwtValueConfig? by createKey(CwtTypeConfig.Keys) {
    config.properties?.find { it.key == "type_key_prefix" }?.valueConfig?.also {
        it.tagType = CwtTagType.TypeKeyPrefix
        it.pointer.element?.bindConfig(it)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtTypeConfig? {
    val configGroup = config.configGroup

    val name = config.key.removeSurroundingOrNull("type[", "]")?.orNull()?.intern() ?: return null
    var baseType: String? = null
    val paths = sortedSetOf<String>()
    var pathFile: String? = null
    var pathExtension: String? = null
    var pathStrict = false
    val pathPatterns = sortedSetOf<String>()
    var nameField: String? = null
    var typeKeyPrefix: String? = null
    var nameFromFile = false
    var typePerFile = false
    var unique = false
    var severity: String? = null
    var skipRootKey: MutableList<List<String>>? = null
    var typeKeyFilter: ReversibleValue<Set<String>>? = null
    var typeKeyRegex: Regex? = null
    var startsWith: String? = null
    var graphRelatedTypes: Set<String>? = null
    val subtypes: MutableMap<String, CwtSubtypeConfig> = mutableMapOf()
    var localisation: CwtTypeLocalisationConfig? = null
    var images: CwtTypeImagesConfig? = null

    val props = config.properties.orEmpty()
    if (props.isEmpty()) return null
    for (prop in props) {
        when (prop.key) {
            "base_type" -> baseType = prop.stringValue
            "path" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { paths += it.intern() }
            "path_file" -> pathFile = prop.stringValue ?: continue
            "path_extension" -> pathExtension = prop.stringValue?.removePrefix(".")?.intern() ?: continue
            "path_strict" -> pathStrict = prop.booleanValue ?: continue
            "path_pattern" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { pathPatterns += it.intern() }
            "name_field" -> nameField = prop.stringValue ?: continue
            "type_key_prefix" -> typeKeyPrefix = prop.stringValue ?: continue
            "name_from_file" -> nameFromFile = prop.booleanValue ?: continue
            "type_per_file" -> typePerFile = prop.booleanValue ?: continue
            "unique" -> unique = prop.booleanValue ?: continue
            "severity" -> severity = prop.stringValue ?: continue
            "skip_root_key" -> {
                //值可能是string也可能是stringArray
                val list = prop.stringValue?.let { listOf(it) }
                    ?: prop.values?.mapNotNull { it.stringValue }
                    ?: continue
                if (skipRootKey == null) skipRootKey = mutableListOf()
                skipRootKey.add(list) //出于一点点的性能考虑，这里保留大小写，后面匹配路径时会忽略掉
            }
            "localisation" -> {
                localisation = CwtTypeLocalisationConfig.resolve(prop)
            }
            "images" -> {
                images = CwtTypeImagesConfig.resolve(prop)
            }
            "modifiers" -> {
                val propProps = prop.properties ?: continue
                for (p in propProps) {
                    val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
                    if (subtypeName != null) {
                        val pps = p.properties ?: continue
                        for (pp in pps) {
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
        }

        run {
            val subtypeConfig = CwtSubtypeConfig.resolve(prop) ?: return@run
            subtypes[subtypeConfig.name] = subtypeConfig
        }
    }

    val options = config.optionConfigs.orEmpty()
    for (option in options) {
        if (option !is CwtOptionConfig) continue
        when (option.key) {
            "type_key_filter" -> {
                //值可能是string也可能是stringArray
                val values = option.getOptionValueOrValues() ?: continue
                val set = caseInsensitiveStringSet() //忽略大小写
                set.addAll(values)
                val o = option.separatorType == CwtSeparatorType.EQUAL
                typeKeyFilter = set.optimized() reverseIf o
            }
            "type_key_regex" -> {
                typeKeyRegex = option.stringValue?.toRegex(RegexOption.IGNORE_CASE)
            }
            "starts_with" -> startsWith = option.stringValue ?: continue //忽略大小写
            "graph_related_types" -> {
                graphRelatedTypes = option.getOptionValues()
            }
        }
    }

    return CwtTypeConfigImpl(
        config, name, baseType,
        paths.optimized(), pathFile, pathExtension, pathStrict, pathPatterns.optimized(),
        nameField, typeKeyPrefix, nameFromFile, typePerFile, unique, severity,
        skipRootKey, typeKeyFilter, typeKeyRegex, startsWith,
        graphRelatedTypes?.optimized(), subtypes.optimized(), localisation, images
    )
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
    override fun toString(): String {
        return "CwtTypeConfigImpl(name='$name')"
    }
}
