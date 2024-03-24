package icu.windea.pls.config.config

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

/**
 * @property name string
 * @property baseType (property) path: string
 * @property path (property) path: string/path
 * @property pathStrict (property) path_strict: boolean
 * @property pathFile (property) path_file: string/fileName
 * @property pathExtension (property) path_extension: string/fileExtension
 * @property nameField (property) name_field: string/propertyKey
 * @property nameFromFile (property) name_from_file: boolean
 * @property typePerFile (property) type_per_file: boolean
 * @property typeKeyFilter (property) type_key_filter: boolean
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
interface CwtTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val baseType: String?
    val path: String?
    val pathStrict: Boolean
    val pathFile: String?
    val pathExtension: String?
    val nameField: String?
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
    
    val possibleRootKeys: Set<String>
    val possibleSwappedTypeRootKeys: Set<String>
    val possibleNestedTypeRootKeys: Set<String>
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtTypeConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtTypeConfig? {
    val configGroup = config.info.configGroup
    val name = config.key.removeSurroundingOrNull("type[", "]")?.orNull()?.intern() ?: return null
    var baseType: String? = null
    var path: String? = null
    var pathStrict = false
    var pathFile: String? = null
    var pathExtension: String? = null
    var nameField: String? = null
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
    
    val props = config.properties
    if(!props.isNullOrEmpty()) {
        for(prop in props) {
            val key = prop.key
            when(key) {
                "base_type" -> baseType = prop.stringValue
                //这里的path一般以"game/"开始，需要去除
                "path" -> path = prop.stringValue?.removePrefix("game")?.trim('/') ?: continue
                "path_strict" -> pathStrict = prop.booleanValue ?: continue
                "path_file" -> pathFile = prop.stringValue ?: continue
                //这里的path_extension一般以"."开始，需要去除
                "path_extension" -> pathExtension = prop.stringValue?.removePrefix(".") ?: continue
                "name_field" -> nameField = prop.stringValue ?: continue
                "name_from_file" -> nameFromFile = prop.booleanValue ?: continue
                "type_per_file" -> typePerFile = prop.booleanValue ?: continue
                "unique" -> unique = prop.booleanValue ?: continue
                "severity" -> severity = prop.stringValue ?: continue
                "skip_root_key" -> {
                    //值可能是string也可能是stringArray
                    val list = prop.stringValue?.let { listOf(it) }
                        ?: prop.values?.mapNotNull { it.stringValue }
                        ?: continue
                    if(skipRootKey == null) skipRootKey = mutableListOf()
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
                    for(p in propProps) {
                        val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
                        if(subtypeName != null) {
                            val pps = p.properties ?: continue
                            for(pp in pps) {
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
    }
    
    val options = config.options
    if(!options.isNullOrEmpty()) {
        for(option in options) {
            if(option !is CwtOptionConfig) continue
            val key = option.key
            when(key) {
                "type_key_filter" -> {
                    //值可能是string也可能是stringArray
                    val values = option.getOptionValueOrValues() ?: continue
                    val set = caseInsensitiveStringSet() //忽略大小写
                    set.addAll(values)
                    val o = option.separatorType == CwtSeparatorType.EQUAL
                    typeKeyFilter = set reverseIf o
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
    }
    
    return CwtTypeConfigImpl(
        config, name, baseType, path, pathStrict, pathFile, pathExtension, nameField, nameFromFile,
        typePerFile, unique, severity, skipRootKey, typeKeyFilter, typeKeyRegex, startsWith, graphRelatedTypes,
        subtypes, localisation, images
    )
}

private class CwtTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val baseType: String? = null,
    override val path: String? = null,
    override val pathStrict: Boolean = false,
    override val pathFile: String? = null,
    override val pathExtension: String? = null,
    override val nameField: String? = null,
    override val nameFromFile: Boolean = false,
    override val typePerFile: Boolean = false,
    override val unique: Boolean = false,
    override val severity: String? = null,
    override val skipRootKey: List<List<String>>? = null,
    override val typeKeyFilter: ReversibleValue<Set<String>>? = null,
    override val typeKeyRegex: Regex? = null,
    override val startsWith: String? = null,
    override val graphRelatedTypes: Set<String>? = null,
    override val subtypes: Map<String, CwtSubtypeConfig> = emptyMap(),
    override val localisation: CwtTypeLocalisationConfig? = null,
    override val images: CwtTypeImagesConfig? = null
) : CwtTypeConfig {
    override val possibleRootKeys by lazy {
        caseInsensitiveStringSet().apply {
            typeKeyFilter?.takeIfTrue()?.let { addAll(it) }
            subtypes.values.forEach { subtype -> subtype.typeKeyFilter?.takeIfTrue()?.let { addAll(it) } }
        }
    }
    
    override val possibleSwappedTypeRootKeys by lazy {
        caseInsensitiveStringSet().apply {
            info.configGroup.swappedTypes.values.forEach f@{ swappedTypeConfig ->
                val baseType = swappedTypeConfig.baseType ?: return@f
                val baseTypeName = baseType.substringBefore('.')
                if(baseTypeName != name) return@f
                val rootKey = swappedTypeConfig.typeKeyFilter?.takeIfTrue()?.singleOrNull() ?: return@f
                add(rootKey)
            }
        }
    }
    
    override val possibleNestedTypeRootKeys by lazy {
        caseInsensitiveStringSet().apply {
            addAll(possibleSwappedTypeRootKeys)
        }
    }
}
