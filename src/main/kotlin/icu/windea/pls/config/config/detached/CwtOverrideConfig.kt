package icu.windea.pls.config.config.detached

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.orNull
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy

/**
 * 覆盖规则。
 *
 * 用于指定某个目标目录（相对于入口目录）的覆盖策略，影响目标的生效顺序与查询结果排序（流式查询除外）。
 * 对应规则文件中的形如 `"common/on_actions" = ordered` 的属性。
 *
 * @property filePath 目标目录的路径，相对于入口目录（通常是游戏目录或模组目录，或者游戏目录下的 `game` 子目录）。
 * @property strategy 使用的覆盖策略。
 *
 * @see ParadoxOverrideStrategy
 */
data class CwtOverrideConfig(
    val filePath: String,
    val strategy: ParadoxOverrideStrategy,
) : CwtDetachedConfig {
    companion object {
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtOverrideConfig? {
            return CwtOverrideConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtOverrideConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtOverrideConfig? {
        val filePath = config.key.optimizedPath().orNull() ?: return null
        val strategyString = config.stringValue?.orNull() ?: return null
        val strategy = ParadoxOverrideStrategy.get(strategyString.uppercase()) ?: return null
        logger.debugWithPrefix(config) { "Resolved override config (filePath: $filePath, strategy: $strategy)" }
        return CwtOverrideConfig(filePath, strategy)
    }
}

// endregion
