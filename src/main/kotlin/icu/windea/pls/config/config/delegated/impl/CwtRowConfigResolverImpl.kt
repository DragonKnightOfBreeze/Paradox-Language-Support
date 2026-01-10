package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtRowConfigResolverImpl : CwtRowConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtRowConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtRowConfig? {
        val name = config.key.removeSurroundingOrNull("row[", "]")?.orNull() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid row config (name: $name): Empty properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propElements.groupBy { it.key }
        val paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.optimizedPathExtension()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val columnConfigs = propGroup.getOne("columns")?.properties?.associateBy { it.key }.orEmpty()
        val endColumn = propGroup.getOne("end_column")?.stringValue

        logger.debug { "Resolved row config (name: $name).".withLocationPrefix(config) }
        return CwtRowConfigImpl(
            config, name,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
            columnConfigs, endColumn
        )
    }
}

private class CwtRowConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val pathPatterns: Set<String>,
    override val columns: Map<String, CwtPropertyConfig>,
    override val endColumn: String?
) : UserDataHolderBase(), CwtRowConfig {
    override fun toString() = "CwtRowConfigImpl(name='$name')"
}
