package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeImagesConfig
import icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.core.util.takeWithOperator
import icu.windea.pls.model.ParadoxTagType

internal class CwtTypeConfigResolverImpl : CwtTypeConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtTypeConfig? {
        val name = config.key.removeSurroundingOrNull("type[", "]")?.orNull()?.optimized() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid type config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propElements.groupBy { it.key }
        val paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.optimizedPathExtension()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val baseType = propGroup.getOne("base_type")?.stringValue
        val nameField = propGroup.getOne("name_field")?.stringValue
        val nameFromFile = propGroup.getOne("name_from_file")?.booleanValue ?: false
        val typePerFile = propGroup.getOne("type_per_file")?.booleanValue ?: false
        val skipRootKey = propGroup.getAll("skip_root_key").map { prop ->
            // 出于一点点的性能考虑，这里保留大小写，后面匹配路径时会忽略掉
            prop.stringValue?.let { listOf(it) } ?: prop.values?.mapNotNull { it.stringValue }?.optimized().orEmpty()
        }
        val typeKeyPrefix = propGroup.getOne("type_key_prefix")?.stringValue
        val typeKeyFilter = config.optionData.typeKeyFilter
        val typeKeyRegex = config.optionData.typeKeyRegex
        val startsWith = config.optionData.startsWith
        val unique = propGroup.getOne("unique")?.booleanValue ?: false
        val severity = propGroup.getOne("severity")?.stringValue
        val graphRelatedTypes = config.optionData.graphRelatedTypes
        val subtypes = propElements.mapNotNull { CwtSubtypeConfig.resolve(it) }.associateBy { it.name }.optimized()
        val localisation = propGroup.getOne("localisation")?.let { CwtTypeLocalisationConfig.resolve(it) }
        val images = propGroup.getOne("images")?.let { CwtTypeImagesConfig.resolve(it) }

        logger.debug { "Resolved type config (name: $name).".withLocationPrefix(config) }
        return CwtTypeConfigImpl(
            config, name, baseType,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
            nameField, nameFromFile, typePerFile,
            skipRootKey, typeKeyPrefix, typeKeyFilter, typeKeyRegex, startsWith,
            unique, severity, graphRelatedTypes,
            subtypes, localisation, images
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
    override val nameFromFile: Boolean,
    override val typePerFile: Boolean,
    override val skipRootKey: List<List<String>>,
    override val typeKeyPrefix: String?,
    override val typeKeyFilter: ReversibleValue<Set<String>>?,
    override val typeKeyRegex: Regex?,
    override val startsWith: String?,
    override val unique: Boolean,
    override val severity: String?,
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
            it.tagType = ParadoxTagType.TypeKeyPrefix
            it.pointer.element?.bindConfig(it)
        }
    }

    override fun toString() = "CwtTypeConfigImpl(name='$name')"
}
