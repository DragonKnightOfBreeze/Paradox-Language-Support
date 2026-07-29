package icu.windea.pls.core.util.metadata

import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey

/**
 * @property bossName Boss 名称（含 Producer，默认 `""`）
 * @property bossLevel Boss 等级（含 Default，默认 `0`）
 * @property health 生命值（含 Default，默认 `0`）
 * @property damageType 伤害类型（可空）
 * @property weakness 弱点属性（可空）
 * @property isRemembrance 是否追忆 Boss（含 Default，默认 `false`）
 * @property location 所在地（可空）
 * @property weaponName 武器名称（含 Producer，默认 `""`）
 * @property weaponType 武器类型（可空）
 * @property attackPower 攻击力（含 Default，默认 `0`）
 * @property weight 重量（含 Default，默认 `0.0`）
 * @property isSomber 是否失色锻造石强化（含 Default，默认 `false`）
 * @property spellName 法术名称（含 Producer，默认 `""`）
 * @property fpCost FP 消耗（含 Default，默认 `0`）
 * @property slots 占用记忆格（含 Default，默认 `1`）
 * @property intelligence 智力需求（含 Default，默认 `0`）
 * @property faith 信仰需求（含 Default，默认 `0`）
 * @property runeMultiplier 卢恩倍率（含 Producer，默认 `{ 1 }`，用于测试延迟计算）
 */
object EldenRingKeys : KeyRegistry() {
    // Boss keys
    val bossName by registerKey(this) { "" }
    val bossLevel by registerKey(this, 0)
    val health by registerKey(this, 0)
    val damageType by registerKey<String>(this)
    val weakness by registerKey<String>(this)
    val isRemembrance by registerKey(this, false)
    val location by registerKey<String>(this)

    // Weapon keys
    val weaponName by registerKey(this) { "" }
    val weaponType by registerKey<String>(this)
    val attackPower by registerKey(this, 0)
    val weight by registerKey(this, 0.0)
    val isSomber by registerKey(this, false)

    // Spell keys
    val spellName by registerKey(this) { "" }
    val fpCost by registerKey(this, 0)
    val slots by registerKey(this, 1)
    val intelligence by registerKey(this, 0)
    val faith by registerKey(this, 0)

    // 用于测试 Producer 延迟计算的 key
    val runeMultiplier by registerKey(this) { 1 }
}
