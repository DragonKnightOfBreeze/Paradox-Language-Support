package icu.windea.pls.config.config.detached

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.scopes.CwtConfigResolutionScope
import icu.windea.pls.core.orNull
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy

/**
 * 覆盖规则（规则文件中的覆盖策略指定）。
 *
 * 用于按目标目录（相对于入口目录）指定覆盖策略。
 *
 * 覆盖规则只是覆盖策略的的来源之一。
 * 最终策略由 `ParadoxOverrideService` 按扩展注册顺序询问各提供者得到，其中内置的强制提供者会优先给出与
 * 规则文件无关的策略（文件/目录固定使用 `FIOS`、定义注入固定使用 `LIOS`、特定定义类型固定使用 `ORDERED`）；
 * 仅当强制提供者均不适用时，才回退到这里按目录匹配的策略（经 `ParadoxBaseOverrideStrategyProvider`），
 * 未命中任何目录映射时默认使用 `LIOS`。
 *
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

private object CwtOverrideConfigResolver : CwtConfigResolutionScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtOverrideConfig? {
        val filePath = config.key.resolvePath().orNull() ?: return null
        val strategyString = config.stringValue?.orNull() ?: return null
        val strategy = ParadoxOverrideStrategy.get(strategyString.uppercase()) ?: return null
        logger.debugWithPrefix(config) { "Resolved override config (filePath: $filePath, strategy: $strategy)" }
        return CwtOverrideConfig(filePath, strategy)
    }
}

// endregion
