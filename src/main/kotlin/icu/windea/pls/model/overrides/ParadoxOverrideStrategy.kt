package icu.windea.pls.model.overrides

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.detached.CwtOverrideConfig
import icu.windea.pls.core.optimized
import icu.windea.pls.ep.overrides.ParadoxOverrideStrategyProvider
import icu.windea.pls.lang.overrides.ParadoxOverrideService

/**
 * 覆盖策略。
 *
 * 用于描述目标（全局封装变量、定义、定值变量、本地化等）在加载与覆盖时的行为。
 * 覆盖策略会驱动查询（非流式）结果的排序，并提供相关的提示与代码检查。
 *
 * 覆盖策略有两个来源，由 [ParadoxOverrideService] 按扩展注册顺序询问各提供者，返回首个非空结果：
 *
 * 1. **强制策略**：由内置提供者直接指定，与规则文件无关，优先级更高。
 *    - 文件或目录：始终使用 [FIOS]（`ParadoxForcedFileOverrideStrategyProvider`）。
 *    - 定义注入（definition injection）：始终使用 [LIOS]（`ParadoxForcedDefinitionInjectionOverrideStrategyProvider`）。
 *    - 特定定义类型：始终使用 [ORDERED]（`ParadoxForcedDefinitionOverrideStrategyProvider`），
 *      包括事件命名空间、动作触发（on action）、交换类型（swapped type）与匿名定义。
 * 2. **规则策略**：由 [icu.windea.pls.config.config.detached.CwtOverrideConfig]（规则文件中的 `overrides` 块）
 *    按目标所在目录指定，经 `ParadoxBaseOverrideStrategyProvider` 匹配得到；未命中任何目录映射时，默认使用 [LIOS]。
 *
 * 说明：
 * - [ORDERED] 表示不发生覆盖（按序新增或合并），因此通常视为“不存在重载”。
 * - 本地化的覆盖策略默认为 [LIOS]；`replace` 目录拥有更高优先级（尚不兼容这种情况）。
 *
 * 参见：[覆盖规则](https://windea.icu/Paradox-Language-Support/ref-config-format.html#config-override)
 *
 * @see CwtOverrideConfig
 * @see ParadoxOverrideService
 * @see ParadoxOverrideStrategyProvider
 */
enum class ParadoxOverrideStrategy(val id: String, val text: String) {
    /** 只读一次（First In, Only Served）。先加载者生效，后加载者会被直接忽略。 */
    FIOS("FIOS", ChronicleBundle.message("overrideStrategy.fios")),
    /** 后读覆盖（Last In, Only Served）。后加载者覆盖先加载者。 */
    LIOS("LIOS", ChronicleBundle.message("overrideStrategy.lios")),
    /** 整文件覆盖（Duplicates）。必须用同路径文件进行整体覆盖。 */
    DUPL("DUPL", ChronicleBundle.message("overrideStrategy.dupl")),
    /** 顺序读取（Ordered）。不能覆盖既有条目，后加载者会被按序新增或合并。 */
    ORDERED("ORDERED", ChronicleBundle.message("overrideStrategy.ordered")),
    ;

    override fun toString() = id

    companion object {
        private val map = entries.associateBy { it.id }.optimized()

        @JvmStatic
        fun get(id: String): ParadoxOverrideStrategy? = map[id]
    }
}
