package icu.windea.pls.config.util.option

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.optimizer.Optimizer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 用于优化附加到成员规则的选项成员规则列表的内存占用。
 *
 * @see CwtMemberConfig
 * @see CwtOptionMemberConfig
 */
@Optimized
object CwtOptionConfigsOptimizer : Optimizer<List<CwtOptionMemberConfig<*>>, Int> {
    // NOTE 2.1.1 这里必须要保证线程安全，因为这里的优化方法可能在初始化规则分组或者解析规则时被并发调用
    // NOTE 2.1.1 目前保持压缩到 Int 类型

    private val key2IdMap = ConcurrentHashMap<String, Int>()
    private val id2CacheMap = ConcurrentHashMap<Int, List<CwtOptionMemberConfig<*>>>()
    private val counter = AtomicInteger()

    override fun optimize(input: List<CwtOptionMemberConfig<*>>): Int {
        if (input.isEmpty()) return 0
        return computeId(input)
    }

    override fun deoptimize(input: Int): List<CwtOptionMemberConfig<*>> {
        if (input <= 0) return emptyList()
        if (input == Int.MAX_VALUE) return emptyList()
        return id2CacheMap[input].orEmpty()
    }

    private fun computeId(optionConfigs: List<CwtOptionMemberConfig<*>>): Int {
        val key = CwtConfigManipulator.getIdentifierKey(optionConfigs)
        return key2IdMap.computeIfAbsent(key) {
            val id = counter.incrementAndGet()
            id2CacheMap[id] = optionConfigs
            id
        }
    }
}
