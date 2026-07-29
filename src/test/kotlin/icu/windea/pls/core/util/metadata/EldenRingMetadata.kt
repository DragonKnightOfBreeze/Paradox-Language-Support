package icu.windea.pls.core.util.metadata

class EldenRingMetadata : MetadataMapBase() {
    // region Boss 属性
    var bossName: String // KeyWithProducer → non-null
        get() = this[EldenRingKeys.bossName]
        set(value) = run { this[EldenRingKeys.bossName] = value }
    var bossLevel: Int // KeyWithDefault → non-null
        get() = this[EldenRingKeys.bossLevel]
        set(value) = run { this[EldenRingKeys.bossLevel] = value }
    var health: Int // KeyWithDefault → non-null
        get() = this[EldenRingKeys.health]
        set(value) = run { this[EldenRingKeys.health] = value }
    var damageType: String? // KeyNormal → nullable
        get() = this[EldenRingKeys.damageType]
        set(value) = run { this[EldenRingKeys.damageType] = value }
    var weakness: String? // KeyNormal → nullable
        get() = this[EldenRingKeys.weakness]
        set(value) = run { this[EldenRingKeys.weakness] = value }
    var isRemembrance: Boolean // KeyWithDefault → non-null
        get() = this[EldenRingKeys.isRemembrance]
        set(value) = run { this[EldenRingKeys.isRemembrance] = value }
    var location: String? // KeyNormal → nullable
        get() = this[EldenRingKeys.location]
        set(value) = run { this[EldenRingKeys.location] = value }
    // endregion

    // region Weapon 属性
    var weaponName: String // KeyWithProducer → non-null
        get() = this[EldenRingKeys.weaponName]
        set(value) = run { this[EldenRingKeys.weaponName] = value }
    var weaponType: String? // KeyNormal → nullable
        get() = this[EldenRingKeys.weaponType]
        set(value) = run { this[EldenRingKeys.weaponType] = value }
    var attackPower: Int // KeyWithDefault → non-null
        get() = this[EldenRingKeys.attackPower]
        set(value) = run { this[EldenRingKeys.attackPower] = value }
    var weight: Double // KeyWithDefault → non-null
        get() = this[EldenRingKeys.weight]
        set(value) = run { this[EldenRingKeys.weight] = value }
    var isSomber: Boolean // KeyWithDefault → non-null
        get() = this[EldenRingKeys.isSomber]
        set(value) = run { this[EldenRingKeys.isSomber] = value }
    // endregion

    // region Spell 属性
    var spellName: String // KeyWithProducer → non-null
        get() = this[EldenRingKeys.spellName]
        set(value) = run { this[EldenRingKeys.spellName] = value }
    var fpCost: Int // KeyWithDefault → non-null
        get() = this[EldenRingKeys.fpCost]
        set(value) = run { this[EldenRingKeys.fpCost] = value }
    var slots: Int // KeyWithDefault → non-null
        get() = this[EldenRingKeys.slots]
        set(value) = run { this[EldenRingKeys.slots] = value }
    var intelligence: Int // KeyWithDefault → non-null
        get() = this[EldenRingKeys.intelligence]
        set(value) = run { this[EldenRingKeys.intelligence] = value }
    var faith: Int // KeyWithDefault → non-null
        get() = this[EldenRingKeys.faith]
        set(value) = run { this[EldenRingKeys.faith] = value }
    // endregion

    // region Producer 测试属性
    val runeMultiplier: Int // KeyWithProducer → non-null（延迟计算）
        get() = this[EldenRingKeys.runeMultiplier]
    // endregion
}
