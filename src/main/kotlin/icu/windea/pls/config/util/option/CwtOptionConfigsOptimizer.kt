package icu.windea.pls.config.util.option

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.optimizer.Optimizer
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMaps
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 用于优化附加到成员规则的选项成员规则列表的内存占用。
 *
 * @see CwtMemberConfig
 * @see CwtOptionMemberConfig
 */
@Optimized
object CwtOptionConfigsOptimizer : Optimizer<List<CwtOptionMemberConfig<*>>, Int> {
    // NOTE 2.0.7 目前没有必要压缩到 Byte 类型 - 这不会额外节省多少浅内存

    private val key2IdMap = Object2IntMaps.synchronize(Object2IntOpenHashMap<String>())
    private val id2CacheMap = Int2ObjectMaps.synchronize(Int2ObjectOpenHashMap<List<CwtOptionMemberConfig<*>>>())
    private val counter = AtomicInteger()

    override fun optimize(input: List<CwtOptionMemberConfig<*>>): Int {
        return computeId(input)
    }

    override fun deoptimize(input: Int): List<CwtOptionMemberConfig<*>> {
        if (input <= 0) return emptyList()
        if (input == Int.MAX_VALUE) return emptyList()
        return id2CacheMap.getOrElse(input) { emptyList() }
    }

    private fun computeId(optionConfigs: List<CwtOptionMemberConfig<*>>): Int {
        val keys = optionConfigs.mapTo(FastList(optionConfigs.size)) { CwtConfigManipulator.getIdentifierKey(it) }
        val key = keys.sorted().joinToString("\u0000")
        return key2IdMap.computeIfAbsent(key) {
            val id = counter.incrementAndGet()
            id2CacheMap.put(id, optionConfigs)
            id
        }
    }
}
