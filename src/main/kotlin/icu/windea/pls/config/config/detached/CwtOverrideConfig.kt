package icu.windea.pls.config.config.detached

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
)
