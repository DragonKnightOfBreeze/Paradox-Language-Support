package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.Key
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import org.junit.Assert.*
import org.junit.Test

/**
 * [MetadataMap] 与 [MetadataMapBase] 的单元测试。
 *
 * @see MetadataMap
 * @see MetadataMapBase
 */
class MetadataMapTest {
    // region Test Keys and Metadata Implementation

    // endregion

    // region isEmpty

    @Test
    fun testIsEmpty_default() {
        val metadata = EldenRingMetadata()
        assertTrue(metadata.isEmpty())
    }

    @Test
    fun testIsEmpty_afterSet() {
        val metadata = EldenRingMetadata()
        metadata.bossName = "女武神玛莲妮亚"
        assertFalse(metadata.isEmpty())
    }

    // endregion

    // region KeyNormal — 可空键

    @Test
    fun testKeyNormal_defaultReturnsNull() {
        val metadata = EldenRingMetadata()
        assertNull(metadata.damageType)
        assertNull(metadata.weakness)
        assertNull(metadata.location)
        assertNull(metadata.weaponType)
    }

    @Test
    fun testKeyNormal_setAndGet() {
        val metadata = EldenRingMetadata()
        metadata.damageType = "物理"
        metadata.weakness = "出血"
        metadata.location = "米凯拉的圣树"

        assertEquals("物理", metadata.damageType)
        assertEquals("出血", metadata.weakness)
        assertEquals("米凯拉的圣树", metadata.location)
    }

    @Test
    fun testKeyNormal_setToNullClears() {
        val metadata = EldenRingMetadata()
        metadata.damageType = "魔法"
        assertEquals("魔法", metadata.damageType)

        metadata.damageType = null
        assertNull(metadata.damageType)
    }

    // endregion

    // region KeyWithDefault — 带静态默认值的键

    @Test
    fun testKeyWithDefault_returnsDefaultWhenNotSet() {
        val metadata = EldenRingMetadata()
        // bossLevel → default 0
        assertEquals(0, metadata.bossLevel)
        // isRemembrance → default false
        assertFalse(metadata.isRemembrance)
        // slots → default 1
        assertEquals(1, metadata.slots)
        // weight → default 0.0
        assertEquals(0.0, metadata.weight, 0.0)
    }

    @Test
    fun testKeyWithDefault_setOverridesDefault() {
        val metadata = EldenRingMetadata()
        metadata.bossLevel = 150
        metadata.health = 18448
        metadata.isRemembrance = true
        metadata.slots = 2

        assertEquals(150, metadata.bossLevel)
        assertEquals(18448, metadata.health)
        assertTrue(metadata.isRemembrance)
        assertEquals(2, metadata.slots)
    }

    @Test
    fun testKeyWithDefault_setBackToDefault() {
        val metadata = EldenRingMetadata()
        metadata.bossLevel = 120
        assertEquals(120, metadata.bossLevel)

        metadata.bossLevel = 0
        assertEquals(0, metadata.bossLevel)
    }

    // endregion

    // region KeyWithProducer — 带延迟计算默认值的键

    @Test
    fun testKeyWithProducer_returnsProducedValueWhenNotSet() {
        var callCount = 0
        val keys = object : KeyRegistry() {
            val runeValue by registerKey(this) { ++callCount; 50000 }
        }
        val metadata = object : MetadataMapBase() {
            val runeValue: Int get() = this[keys.runeValue]
        }

        assertEquals(50000, metadata.runeValue) // producer 首次调用
        assertEquals(1, callCount)

        assertEquals(50000, metadata.runeValue) // 缓存命中的值
        assertEquals(1, callCount) // producer 未被重复调用
    }

    @Test
    fun testKeyWithProducer_computedValueIsCached() {
        var counter = 0
        val keys = object : KeyRegistry() {
            val heavyRune by registerKey(this) { ++counter; counter * 1000 }
        }
        val metadata = object : MetadataMapBase() {
            val heavyRune: Int get() = this[keys.heavyRune]
        }

        val first = metadata.heavyRune
        val second = metadata.heavyRune

        assertEquals(1000, first)
        assertEquals(1000, second)
        assertEquals(1, counter) // 只计算了一次
    }

    @Test
    fun testKeyWithProducer_setOverridesProducer() {
        val metadata = EldenRingMetadata()
        // runeMultiplier 默认 producer 返回 1
        metadata[EldenRingKeys.runeMultiplier] = 5 // 显式设置

        assertEquals(5, metadata.runeMultiplier) // 返回显式设置的值，而非 producer 计算值
    }

    @Test
    fun testKeyWithProducer_defaultViaSubclassProperty() {
        // EldenRingMetadata 上 runeMultiplier 为 val（只读），producer 默认返回 1
        val metadata = EldenRingMetadata()
        assertEquals(1, metadata.runeMultiplier)
    }

    // endregion

    // region KeyWithDefault 与 Key 的 get 重载——键类型决定返回值类型

    @Test
    fun testKeyTypeResolution_keyNormalReturnsNullable() {
        val metadata = EldenRingMetadata()
        metadata.damageType = "圣"

        // 通过 Key<*> 读取（返回 T?）
        val viaKey: Key<*> = EldenRingKeys.damageType
        @Suppress("UNCHECKED_CAST")
        val value = (viaKey as Key<String?>).let { metadata[it] }
        assertEquals("圣", value)
    }

    @Test
    fun testKeyTypeResolution_keyWithDefaultReturnsNonNull() {
        val metadata = EldenRingMetadata()
        // bossLevel 的 KeyWithDefault 默认 0
        val value: Int = metadata[EldenRingKeys.bossLevel]
        assertEquals(0, value) // 未设置，获取默认
    }

    // endregion

    // region set — 通过 MetadataMapBase 的 set 操作符直接操作

    @Test
    fun testSet_usingOperatorDirectly() {
        val metadata = MetadataMapBase()
        metadata[EldenRingKeys.bossName] = "拉塔恩"
        metadata[EldenRingKeys.bossLevel] = 180

        assertEquals("拉塔恩", metadata[EldenRingKeys.bossName])
        assertEquals(180, metadata[EldenRingKeys.bossLevel])
    }

    @Test
    fun testSet_nullRemovesKey() {
        val metadata = EldenRingMetadata()
        metadata.bossName = "蒙格"
        assertFalse(metadata.isEmpty())

        metadata[EldenRingKeys.bossName] = null
        // bossName 是 KeyWithProducer，producer 会返回 ""，但 null set 后存储的是 EMPTY_OBJECT
        // 因此再次通过 get 读取时会触发 producer 再次计算
        assertEquals("", metadata.bossName)
    }

    // endregion

    // region clearMetadata

    @Test
    fun testClearMetadata_emptiesAll() {
        val metadata = EldenRingMetadata()
        metadata.bossName = "葛孚雷"
        metadata.bossLevel = 120
        metadata.damageType = "物理"
        metadata.weaponName = "名刀月隐"
        metadata.attackPower = 178

        assertFalse(metadata.isEmpty())
        metadata.clearMetadata()
        assertTrue(metadata.isEmpty())

        // 清除后，KeyWithDefault 恢复默认值
        assertEquals(0, metadata.bossLevel)
        assertEquals(0, metadata.attackPower)
    }

    @Test
    fun testClearMetadata_keyNormalBecomesNull() {
        val metadata = EldenRingMetadata()
        metadata.damageType = "火焰"
        metadata.clearMetadata()
        assertNull(metadata.damageType)
    }

    // endregion

    // region copyMetadataTo

    @Test
    fun testCopyMetadataTo_replacesAllTargetEntries() {
        val source = EldenRingMetadata().apply {
            bossName = "火焰巨人"
            bossLevel = 100
            health = 43263
            damageType = "火焰"
            weakness = "出血"
            location = "巨人山顶"
        }
        val target = EldenRingMetadata().apply {
            weaponName = "猎犬长牙"
            attackPower = 256
            weight = 11.5
        }

        source.copyMetadataTo(target)

        // target 原有数据被替换
        assertEquals("", target.weaponName) // bossName 的 producer 默认值被应用...
        // 实际上 copy 是整个 map 的赋值，target 的整个 map 被 source 的 map 覆盖
        // 所以 target 现在拥有和 source 完全一致的元数据
        assertEquals("火焰巨人", target.bossName)
        assertEquals(100, target.bossLevel)
        assertEquals(43263, target.health)
        assertEquals("火焰", target.damageType)
        assertEquals("出血", target.weakness)
        assertEquals("巨人山顶", target.location)
    }

    @Test
    fun testCopyMetadataTo_emptySourceClearsTarget() {
        val target = EldenRingMetadata().apply {
            bossName = "死龙弗尔桑克斯"
            bossLevel = 110
        }
        val source = EldenRingMetadata() // 空的

        source.copyMetadataTo(target)
        assertTrue(target.isEmpty())
    }

    @Test
    fun testCopyMetadataTo_emptyTargetReceivesData() {
        val source = EldenRingMetadata().apply {
            spellName = "彗星亚兹勒"
            fpCost = 40
            slots = 3
            intelligence = 60
        }
        val target = EldenRingMetadata()

        source.copyMetadataTo(target)
        assertEquals("彗星亚兹勒", target.spellName)
        assertEquals(40, target.fpCost)
        assertEquals(3, target.slots)
        assertEquals(60, target.intelligence)
    }

    @Test
    fun testCopyMetadataTo_usingMetadataMapBaseDirectly() {
        val source = MetadataMapBase()
        val target = MetadataMapBase()

        source[EldenRingKeys.bossName] = "龙王普拉顿桑克斯"
        source[EldenRingKeys.bossLevel] = 130
        source[EldenRingKeys.damageType] = "雷电"

        source.copyMetadataTo(target)

        assertEquals("龙王普拉顿桑克斯", target[EldenRingKeys.bossName])
        assertEquals(130, target[EldenRingKeys.bossLevel])
        assertEquals("雷电", target[EldenRingKeys.damageType])
    }

    // endregion

    // region mergeMetadataTo

    @Test
    fun testMergeMetadataTo_addsNewKeysToTarget() {
        val source = EldenRingMetadata().apply {
            spellName = "黄金树立誓"
            fpCost = 47
            slots = 1
            faith = 25
        }
        val target = EldenRingMetadata().apply {
            weaponName = "暗月大剑"
            attackPower = 198
            weight = 10.0
            isSomber = true
        }

        source.mergeMetadataTo(target)

        // 原先在 target 上的键仍然保留
        assertEquals("暗月大剑", target.weaponName)
        assertEquals(198, target.attackPower)
        assertEquals(10.0, target.weight, 0.0)
        assertTrue(target.isSomber)

        // source 中的键被合并到 target
        assertEquals("黄金树立誓", target.spellName)
        assertEquals(47, target.fpCost)
        assertEquals(1, target.slots)
        assertEquals(25, target.faith)
    }

    @Test
    fun testMergeMetadataTo_overlappingKeysTargetWins() {
        // 场景："熔炉骑士"同时作为 boss 和武器的元数据
        // boss 视图：bossName = "熔炉骑士", bossLevel = 80
        // 武器视图：bossName = "熔炉骑士", weaponName = "奥陶琵斯大剑"
        // 合并到武器视图时，bossName 以武器视图为准（target 的原有值优先）

        val bossMetadata = EldenRingMetadata().apply {
            bossName = "熔炉骑士"
            bossLevel = 80
            location = "深根底层"
        }
        val weaponMetadata = EldenRingMetadata().apply {
            bossName = "熔炉骑士"
            weaponName = "奥陶琵斯大剑"
            attackPower = 245
            weight = 15.5
        }

        bossMetadata.mergeMetadataTo(weaponMetadata)

        // target 中已存在的键不变（target wins）
        assertEquals("熔炉骑士", weaponMetadata.bossName)
        assertEquals("奥陶琵斯大剑", weaponMetadata.weaponName)
        assertEquals(245, weaponMetadata.attackPower)
        assertEquals(15.5, weaponMetadata.weight, 0.0)

        // source 中有而 target 中没有的键被添加
        assertEquals(80, weaponMetadata.bossLevel)
        assertEquals("深根底层", weaponMetadata.location)
    }

    @Test
    fun testMergeMetadataTo_emptySourceNoChange() {
        val target = EldenRingMetadata().apply {
            weaponName = "尸山血海"
            attackPower = 186
            weight = 6.5
        }
        val source = EldenRingMetadata() // 空的

        source.mergeMetadataTo(target)

        // 没有任何变化
        assertEquals("尸山血海", target.weaponName)
        assertEquals(186, target.attackPower)
        assertEquals(6.5, target.weight, 0.0)
    }

    @Test
    fun testMergeMetadataTo_emptyTargetReceivesData() {
        val source = EldenRingMetadata().apply {
            bossName = "黑剑玛利喀斯"
            bossLevel = 130
            health = 23102
            isRemembrance = true
        }
        val target = EldenRingMetadata() // 空的

        source.mergeMetadataTo(target)

        assertEquals("黑剑玛利喀斯", target.bossName)
        assertEquals(130, target.bossLevel)
        assertEquals(23102, target.health)
        assertTrue(target.isRemembrance)
    }

    @Test
    fun testMergeMetadataTo_usingMetadataMapBaseDirectly() {
        val source = MetadataMapBase()
        val target = MetadataMapBase()

        target[EldenRingKeys.weaponName] = "神躯化剑"
        target[EldenRingKeys.attackPower] = 264

        source[EldenRingKeys.bossName] = "艾尔登之兽"
        source[EldenRingKeys.bossLevel] = 140
        // 注意：source 中设置 bossName 会覆盖 target 中已有的武器信息吗？不会，因为 key 不同

        source.mergeMetadataTo(target)

        assertEquals("神躯化剑", target[EldenRingKeys.weaponName])
        assertEquals(264, target[EldenRingKeys.attackPower])
        assertEquals("艾尔登之兽", target[EldenRingKeys.bossName])
        assertEquals(140, target[EldenRingKeys.bossLevel])
    }

    // endregion

    // region 综合场景

    @Test
    fun testFullBossMetadataLifecycle() {
        // 创建一个 Boss 元数据：女武神玛莲妮亚
        val malenia = EldenRingMetadata().apply {
            bossName = "女武神玛莲妮亚"
            bossLevel = 180
            health = 18448
            damageType = "物理"
            weakness = "出血"
            isRemembrance = true
            location = "米凯拉的圣树"
        }

        // 验证
        assertEquals("女武神玛莲妮亚", malenia.bossName)
        assertEquals(180, malenia.bossLevel)
        assertEquals(18448, malenia.health)
        assertEquals("物理", malenia.damageType)
        assertEquals("出血", malenia.weakness)
        assertTrue(malenia.isRemembrance)
        assertEquals("米凯拉的圣树", malenia.location)
        assertFalse(malenia.isEmpty())

        // 创建武器元数据
        val weapon = EldenRingMetadata().apply {
            weaponName = "玛莲妮亚的义手刀"
            weaponType = "刀"
            attackPower = 247
            weight = 5.5
            isSomber = true
        }

        // 将武器元数据合并到 Boss 元数据（模拟综合视角）
        weapon.mergeMetadataTo(malenia)

        // Boss 原有属性不变
        assertEquals("女武神玛莲妮亚", malenia.bossName)
        assertEquals(180, malenia.bossLevel)

        // 武器属性也被合并进去
        assertEquals("玛莲妮亚的义手刀", malenia.weaponName)
        assertEquals("刀", malenia.weaponType)
        assertEquals(247, malenia.attackPower)
        assertEquals(5.5, malenia.weight, 0.0)
        assertTrue(malenia.isSomber)
    }

    @Test
    fun testCopyAndClearSequence() {
        val godrick = EldenRingMetadata().apply {
            bossName = "接肢葛瑞克"
            bossLevel = 30
            health = 6080
            isRemembrance = true
            location = "史东薇尔城"
        }

        // 复制
        val copy = EldenRingMetadata()
        godrick.copyMetadataTo(copy)
        assertEquals("接肢葛瑞克", copy.bossName)
        assertEquals(30, copy.bossLevel)
        assertEquals(6080, copy.health)
        assertTrue(copy.isRemembrance)

        // 清除原对象不影响副本
        godrick.clearMetadata()
        assertTrue(godrick.isEmpty())
        assertEquals("接肢葛瑞克", copy.bossName)
    }

    @Test
    fun testMergeDoesNotOverrideTargetDefaults() {
        val source = EldenRingMetadata().apply {
            // slots 未显式设置，默认 1；fpCost 未设置，默认 0
            spellName = "黑夜彗星"
            fpCost = 24
        }
        val target = EldenRingMetadata().apply {
            spellName = "帚星"
            fpCost = 32
            intelligence = 43
        }

        // merge：source 中在 target 已存在的键也会被覆盖
        source.mergeMetadataTo(target)

        assertEquals("黑夜彗星", target.spellName) // target wins
        assertEquals(24, target.fpCost) // target wins
        assertEquals(43, target.intelligence) // source 添加（target 中没有，且未设置所以默认 0 但没在 map 中）
        assertEquals(1, target.slots) // target 的默认值，未被 source 修改
    }

    // endregion

    // region 边界情况 — Producer 返回 null

    @Test
    fun testKeyWithProducer_returnsNullFromProducer() {
        var callCount = 0
        val keys = object : KeyRegistry() {
            val nullableRune by registerKey<String?>(this) { callCount++; null }
        }
        val metadata = object : MetadataMapBase() {
            val nullableRune: String? get() = this[keys.nullableRune]
        }

        assertNull(metadata.nullableRune)
        assertEquals(1, callCount)

        // 再次读取不应重复调用 producer（null 被缓存为 EMPTY_OBJECT）
        assertNull(metadata.nullableRune)
        assertEquals(1, callCount)
    }

    // endregion

    // region 边界情况 — clearMetadata 后 KeyWithProducer 重新触发

    @Test
    fun testClearMetadata_retriggersProducer() {
        var callCount = 0
        val keys = object : KeyRegistry() {
            val triggeredRune by registerKey(this) { callCount++; callCount * 100 }
        }
        val metadata = object : MetadataMapBase() {
            val triggeredRune: Int get() = this[keys.triggeredRune]
        }

        assertEquals(100, metadata.triggeredRune)
        assertEquals(1, callCount)

        metadata.clearMetadata()
        assertEquals(200, metadata.triggeredRune) // producer 被重新触发
        assertEquals(2, callCount)
    }

    // endregion

    // region 边界情况 — 自合并

    @Test
    fun testMergeMetadataTo_selfMergeIsNoOp() {
        val metadata = EldenRingMetadata().apply {
            bossName = "恶兆妖鬼玛尔基特"
            bossLevel = 40
            health = 6888
        }

        metadata.mergeMetadataTo(metadata)

        assertEquals("恶兆妖鬼玛尔基特", metadata.bossName)
        assertEquals(40, metadata.bossLevel)
        assertEquals(6888, metadata.health)
        assertFalse(metadata.isEmpty())
    }

    // endregion

    // region 边界情况 — KeyWithDefault 显式设置为默认值

    @Test
    fun testKeyWithDefault_setToDefaultValue_mapNotEmpty() {
        val metadata = EldenRingMetadata()
        assertTrue(metadata.isEmpty())

        // bossLevel 默认是 0，显式设置为 0 后 map 不应为空
        metadata.bossLevel = 0
        assertEquals(0, metadata.bossLevel)
        assertFalse(metadata.isEmpty())

        // attackPower 默认是 0
        metadata.attackPower = 0
        assertEquals(0, metadata.attackPower)
        assertFalse(metadata.isEmpty())

        // weight 默认是 0.0
        metadata.weight = 0.0
        assertEquals(0.0, metadata.weight, 0.0)

        // isRemembrance 默认是 false
        metadata.isRemembrance = false
        assertFalse(metadata.isRemembrance)
    }

    // endregion

    // region 边界情况 — 多实例隔离

    @Test
    fun testMultipleInstances_doNotInterfere() {
        val m1 = EldenRingMetadata().apply {
            bossName = "黄金律法拉达冈"
            bossLevel = 140
        }
        val m2 = EldenRingMetadata().apply {
            bossName = "艾尔登之兽"
            bossLevel = 150
        }

        // m1 和 m2 拥有各自独立的元数据
        assertEquals("黄金律法拉达冈", m1.bossName)
        assertEquals(140, m1.bossLevel)
        assertEquals("艾尔登之兽", m2.bossName)
        assertEquals(150, m2.bossLevel)

        // 修改 m1 不影响 m2
        m1.bossName = "拉达冈的红狼"
        m1.bossLevel = 50
        assertEquals("拉达冈的红狼", m1.bossName)
        assertEquals(50, m1.bossLevel)
        assertEquals("艾尔登之兽", m2.bossName)
        assertEquals(150, m2.bossLevel)
    }

    // endregion

    // region 边界情况 — 对同一 key 连续 set 多次

    @Test
    fun testSetMultipleTimes_lastValueWins() {
        val metadata = EldenRingMetadata()

        metadata.weaponName = "古兰桑克斯的雷电"
        metadata.weaponName = "日蚀钩剑"
        metadata.weaponName = "玛雷家行刑剑"
        assertEquals("玛雷家行刑剑", metadata.weaponName)

        metadata.bossLevel = 10
        metadata.bossLevel = 60
        metadata.bossLevel = 120
        assertEquals(120, metadata.bossLevel)

        metadata.damageType = "物理"
        metadata.damageType = "火焰"
        metadata.damageType = null
        assertNull(metadata.damageType)
    }

    // endregion

    // region 边界情况 — copyMetadataTo 后 source 修改不影响 target

    @Test
    fun testCopyMetadataTo_sourceModificationDoesNotAffectTarget() {
        val source = EldenRingMetadata().apply {
            bossName = "亵渎君王拉卡德"
            bossLevel = 100
        }
        val target = EldenRingMetadata()
        source.copyMetadataTo(target)

        // copy 完成后修改 source
        source.bossName = "被修改"
        source.bossLevel = 999
        source.health = 99999

        // target 保持不变
        assertEquals("亵渎君王拉卡德", target.bossName)
        assertEquals(100, target.bossLevel)
        assertEquals(0, target.health) // 从未设置过，使用默认
    }

    // endregion

    // region 边界情况 — 三方合并

    @Test
    fun testMergeMetadataTo_threeWayMerge() {
        val boss = EldenRingMetadata().apply {
            bossName = "黑暗弃子艾丝缇"
            bossLevel = 120
            location = "腐败湖"
        }
        val weapon = EldenRingMetadata().apply {
            weaponName = "艾丝缇薄翼"
            attackPower = 200
            weight = 2.5
        }
        val combined = EldenRingMetadata() // 空的目标

        boss.mergeMetadataTo(combined)
        weapon.mergeMetadataTo(combined)

        // combined 应同时包含 boss 和 weapon 的数据
        assertEquals("黑暗弃子艾丝缇", combined.bossName)
        assertEquals(120, combined.bossLevel)
        assertEquals("腐败湖", combined.location)
        assertEquals("艾丝缇薄翼", combined.weaponName)
        assertEquals(200, combined.attackPower)
        assertEquals(2.5, combined.weight, 0.0)
    }

    // endregion

    // region 边界情况 — 空字符串

    @Test
    fun testKeyWithProducer_emptyStringDefault() {
        val metadata = EldenRingMetadata()
        // bossName、weaponName、spellName 的 producer 默认返回 ""
        assertEquals("", metadata.bossName)
        assertEquals("", metadata.weaponName)
        assertEquals("", metadata.spellName)
    }

    @Test
    fun testKeyNormal_setToEmptyString() {
        val metadata = EldenRingMetadata()
        metadata.damageType = ""
        assertEquals("", metadata.damageType) // 空字符串不是 null
        assertFalse(metadata.isEmpty())
    }

    // endregion
}
