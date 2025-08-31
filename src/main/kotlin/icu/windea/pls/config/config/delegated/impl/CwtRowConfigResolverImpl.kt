package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtRowConfigResolverImpl : CwtRowConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtRowConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtRowConfig? {
        val name = config.key.removeSurroundingOrNull("row[", "]")?.orNull() ?: return null
        val paths = sortedSetOf<String>()
        var pathFile: String? = null
        var pathExtension: String? = null
        var pathStrict = false
        val pathPatterns = sortedSetOf<String>()
        val columnConfigs = mutableMapOf<String, CwtPropertyConfig>()
        var endColumn: String? = null

        val props = config.properties.orEmpty()
        if (props.isEmpty()) return null
        for (prop in props) {
            when (prop.key) {
                "path" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { paths += it.intern() }
                "path_file" -> pathFile = prop.stringValue ?: continue
                "path_extension" -> pathExtension = prop.stringValue?.removePrefix(".")?.intern() ?: continue
                "path_strict" -> pathStrict = prop.booleanValue ?: continue
                "path_pattern" -> prop.stringValue?.removePrefix("game/")?.normalizePath()?.let { pathPatterns += it.intern() }
                "columns" -> prop.properties.orEmpty().forEach { c -> columnConfigs[c.key] = c }
                "end_column" -> endColumn = prop.stringValue ?: continue
            }
        }

        return CwtRowConfigImpl(
            config, name,
            paths.optimized(), pathFile, pathExtension, pathStrict, pathPatterns.optimized(),
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
