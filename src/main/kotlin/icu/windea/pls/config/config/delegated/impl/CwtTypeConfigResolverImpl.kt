package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeImagesConfig
import icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig
import icu.windea.pls.config.config.getOptionValueOrValues
import icu.windea.pls.config.config.getOptionValues
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configGroup.modifiers
import icu.windea.pls.config.configGroup.type2ModifiersMap
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.core.util.takeWithOperator
import icu.windea.pls.model.CwtSeparatorType

internal class CwtTypeConfigResolverImpl : CwtTypeConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtTypeConfig? {
        val configGroup = config.configGroup

        // 从 key 中提取类型名：type[<name>]
        val name = config.key.removeSurroundingOrNull("type[", "]")?.orNull()?.intern() ?: return null
        var baseType: String? = null
        // 路径字段：统一移除 game/ 前缀并归一化为相对游戏根路径，使用有序集合以获得稳定顺序
        val paths = sortedSetOf<String>()
        var pathFile: String? = null
        var pathExtension: String? = null
        var pathStrict = false
        // 支持通配的路径规则
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
                    // 值可能是string也可能是stringArray
                    val list = prop.stringValue?.let { listOf(it) }
                        ?: prop.values?.mapNotNull { it.stringValue }
                        ?: continue
                    if (skipRootKey == null) skipRootKey = mutableListOf()
                    skipRootKey.add(list) // 出于一点点的性能考虑，这里保留大小写，后面匹配路径时会忽略掉
                }
                "localisation" -> {
                    // 解析类型本地化位置的子配置
                    localisation = CwtTypeLocalisationConfig.resolve(prop)
                }
                "images" -> {
                    // 解析类型图像位置的子配置
                    images = CwtTypeImagesConfig.resolve(prop)
                }
                "modifiers" -> {
                    // 从类型定义中收集修正规则：
                    // 1) 若写在 subtype[...] 内，则类型表达式为 <type>.<subtype>
                    // 2) 否则类型表达式为 <type>
                    // 将结果写入 configGroup.modifiers 与 configGroup.type2ModifiersMap，供后续索引/提示
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
                    // 值可能是string也可能是stringArray
                    val values = option.getOptionValueOrValues() ?: continue
                    val set = caseInsensitiveStringSet() // 忽略大小写
                    set.addAll(values)
                    // 使用 ReversibleValue 记录运算符（= 表示正向匹配，其他表示反向），以便后续取值时保留语义
                    val o = option.separatorType == CwtSeparatorType.EQUAL
                    typeKeyFilter = ReversibleValue(o, set.optimized())
                }
                "type_key_regex" -> {
                    typeKeyRegex = option.stringValue?.toRegex(RegexOption.IGNORE_CASE)
                }
                "starts_with" -> startsWith = option.stringValue ?: continue // 忽略大小写
                "graph_related_types" -> {
                    graphRelatedTypes = option.getOptionValues()
                }
            }
        }

        // 构造最终的类型配置实例（包含路径过滤、命名策略、子类型、本地化与图像位置、以及修正收集结果等）
        return CwtTypeConfigImpl(
            config, name, baseType,
            paths.optimized(), pathFile, pathExtension, pathStrict, pathPatterns.optimized(),
            nameField, typeKeyPrefix, nameFromFile, typePerFile, unique, severity,
            skipRootKey, typeKeyFilter, typeKeyRegex, startsWith,
            graphRelatedTypes?.optimized(), subtypes.optimized(), localisation, images
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
    // 可能的根键集合：合并类型与所有子类型的 type_key_filter（忽略运算符），用于加速根键匹配与提示
    override val possibleRootKeys: Set<String> by lazy {
        caseInsensitiveStringSet().apply {
            typeKeyFilter?.takeWithOperator()?.let { addAll(it) }
            subtypes.values.forEach { subtype -> subtype.typeKeyFilter?.takeWithOperator()?.let { addAll(it) } }
        }.optimized()
    }

    // 记录并标注 type_key_prefix 的值节点，供 UI 高亮与跳转（绑定到 PSI 指针以保持稳定）
    override val typeKeyPrefixConfig: CwtValueConfig? by lazy {
        config.properties?.find { it.key == "type_key_prefix" }?.valueConfig?.also {
            it.tagType = CwtTagType.TypeKeyPrefix
            it.pointer.element?.bindConfig(it)
        }
    }

    override fun toString() = "CwtTypeConfigImpl(name='$name')"
}
