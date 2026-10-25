package icu.windea.pls.core.util.stream

/**
 * 测试数据。
 *
 * 树状数据以地区（[SoulsRegion]）为根，子节点为地点（[SoulsLocation]），
 * 地点下再混合人物（[SoulsCharacter]）、武器（[SoulsWeapon]）和魔法（[SoulsSpell]），
 * 用于覆盖子节点类型不一致的复杂场景。
 */
internal sealed interface SoulsNode {
    val name: String
}

internal enum class SoulsGame(val title: String) {
    DarkSouls1("Dark Souls"), // 黑暗之魂
    DarkSouls2("Dark Souls II"), // 黑暗之魂2
    DarkSouls3("Dark Souls III"), // 黑暗之魂3
}

internal enum class SoulsCharacterKind { Boss, Npc }

internal enum class SoulsWeaponCategory { StraightSword, Greatsword, UltraGreatsword, Katana, CurvedSword, Staff }

internal enum class SoulsSpellCategory { Sorcery, Miracle, Pyromancy, Hex }

internal data class SoulsCharacter(
    override val name: String,
    val kind: SoulsCharacterKind,
    val game: SoulsGame,
) : SoulsNode

internal data class SoulsWeapon(
    override val name: String,
    val category: SoulsWeaponCategory,
    val game: SoulsGame,
) : SoulsNode

internal data class SoulsSpell(
    override val name: String,
    val category: SoulsSpellCategory,
    val game: SoulsGame,
) : SoulsNode

internal data class SoulsLocation(
    override val name: String,
    val region: String,
    val characters: List<SoulsCharacter> = emptyList(),
    val items: List<SoulsNode> = emptyList(),
) : SoulsNode

internal data class SoulsRegion(
    override val name: String,
    val locations: List<SoulsLocation>,
) : SoulsNode

internal fun SoulsNode.children(): List<SoulsNode> = when (this) {
    is SoulsRegion -> locations
    is SoulsLocation -> characters + items
    else -> emptyList()
}

internal object SoulsTestData {
    // region 人物

    val asylumDemon = SoulsCharacter("Asylum Demon", SoulsCharacterKind.Boss, SoulsGame.DarkSouls1) // 恶魔遗孤
    val ornsteinAndSmough = SoulsCharacter("Ornstein & Smough", SoulsCharacterKind.Boss, SoulsGame.DarkSouls1) // 猎龙者翁斯坦与刽子手斯摩
    val artorias = SoulsCharacter("Artorias the Abysswalker", SoulsCharacterKind.Boss, SoulsGame.DarkSouls1) // 深渊漫步者阿尔特留斯
    val gwyn = SoulsCharacter("Gwyn, Lord of Cinder", SoulsCharacterKind.Boss, SoulsGame.DarkSouls1) // 薪王葛温
    val thePursuer = SoulsCharacter("The Pursuer", SoulsCharacterKind.Boss, SoulsGame.DarkSouls2) // 咒缚者
    val fumeKnight = SoulsCharacter("Fume Knight", SoulsCharacterKind.Boss, SoulsGame.DarkSouls2) // 烟之骑士
    val nashandra = SoulsCharacter("Nashandra", SoulsCharacterKind.Boss, SoulsGame.DarkSouls2) // 娜珊德拉
    val iudexGundyr = SoulsCharacter("Iudex Gundyr", SoulsCharacterKind.Boss, SoulsGame.DarkSouls3) // 灰烬审判者古达
    val abyssWatchers = SoulsCharacter("Abyss Watchers", SoulsCharacterKind.Boss, SoulsGame.DarkSouls3) // 深渊监视者
    val sisterFriede = SoulsCharacter("Sister Friede", SoulsCharacterKind.Boss, SoulsGame.DarkSouls3) // 修女芙莉德
    val slaveKnightGael = SoulsCharacter("Slave Knight Gael", SoulsCharacterKind.Boss, SoulsGame.DarkSouls3) // 奴隶骑士盖尔

    val solaire = SoulsCharacter("Solaire of Astora", SoulsCharacterKind.Npc, SoulsGame.DarkSouls1) // 亚斯特拉的索拉尔
    val siegmeyer = SoulsCharacter("Siegmeyer of Catarina", SoulsCharacterKind.Npc, SoulsGame.DarkSouls1) // 卡塔利纳的希格迈耶
    val lucatiel = SoulsCharacter("Lucatiel of Mirrah", SoulsCharacterKind.Npc, SoulsGame.DarkSouls2) // 米勒的鲁卡提耶
    val fireKeeper = SoulsCharacter("Fire Keeper", SoulsCharacterKind.Npc, SoulsGame.DarkSouls3) // 防火女
    val anri = SoulsCharacter("Anri of Astora", SoulsCharacterKind.Npc, SoulsGame.DarkSouls3) // 亚斯特拉的安里

    // endregion

    // region 武器

    val longsword = SoulsWeapon("Longsword", SoulsWeaponCategory.StraightSword, SoulsGame.DarkSouls1) // 长剑
    val zweihander = SoulsWeapon("Zweihander", SoulsWeaponCategory.UltraGreatsword, SoulsGame.DarkSouls1) // 双手巨剑
    val uchigatana = SoulsWeapon("Uchigatana", SoulsWeaponCategory.Katana, SoulsGame.DarkSouls1) // 打刀
    val moonlightGreatsword = SoulsWeapon("Moonlight Greatsword", SoulsWeaponCategory.Greatsword, SoulsGame.DarkSouls1) // 月光大剑
    val chaosBlade = SoulsWeapon("Chaos Blade", SoulsWeaponCategory.Katana, SoulsGame.DarkSouls1) // 混沌之刃
    val fumeUltraGreatsword = SoulsWeapon("Fume Ultra Greatsword", SoulsWeaponCategory.UltraGreatsword, SoulsGame.DarkSouls2) // 烟之特大剑
    val blacksteelKatana = SoulsWeapon("Blacksteel Katana", SoulsWeaponCategory.Katana, SoulsGame.DarkSouls2) // 黑铁刀
    val carthusCurvedSword = SoulsWeapon("Carthus Curved Sword", SoulsWeaponCategory.CurvedSword, SoulsGame.DarkSouls3) // 卡萨斯曲剑
    val lothricKnightSword = SoulsWeapon("Lothric Knight Sword", SoulsWeaponCategory.StraightSword, SoulsGame.DarkSouls3) // 洛斯里克骑士剑
    val courtSorcerersStaff = SoulsWeapon("Court Sorcerer's Staff", SoulsWeaponCategory.Staff, SoulsGame.DarkSouls3) // 宫廷魔法师杖

    // endregion

    // region 魔法

    val soulArrow = SoulsSpell("Soul Arrow", SoulsSpellCategory.Sorcery, SoulsGame.DarkSouls1) // 灵魂箭
    val heal = SoulsSpell("Heal", SoulsSpellCategory.Miracle, SoulsGame.DarkSouls1) // 治愈
    val fireball = SoulsSpell("Fireball", SoulsSpellCategory.Pyromancy, SoulsGame.DarkSouls1) // 火球
    val darkOrb = SoulsSpell("Dark Orb", SoulsSpellCategory.Hex, SoulsGame.DarkSouls2) // 暗珠
    val greatLightningSpear = SoulsSpell("Great Lightning Spear", SoulsSpellCategory.Miracle, SoulsGame.DarkSouls2) // 巨雷枪
    val soulStream = SoulsSpell("Soul Stream", SoulsSpellCategory.Sorcery, SoulsGame.DarkSouls3) // 灵魂激流
    val tearsOfDenial = SoulsSpell("Tears of Denial", SoulsSpellCategory.Miracle, SoulsGame.DarkSouls3) // 拒绝的眼泪
    val chaosBedVestiges = SoulsSpell("Chaos Bed Vestiges", SoulsSpellCategory.Pyromancy, SoulsGame.DarkSouls3) // 混沌火球弹

    // endregion

    // region 地点与地区

    val firelinkShrine = SoulsLocation(
        name = "Firelink Shrine", // 传火祭祀场
        region = "Lordran", // 罗德兰
        characters = listOf(solaire, siegmeyer),
        items = listOf(longsword),
    )
    val undeadBurg = SoulsLocation(
        name = "Undead Burg", // 不死镇
        region = "Lordran", // 罗德兰
        items = listOf(uchigatana),
    )
    val anorLondo = SoulsLocation(
        name = "Anor Londo", // 亚诺尔隆德
        region = "Lordran", // 罗德兰
        characters = listOf(ornsteinAndSmough),
        items = listOf(moonlightGreatsword),
    )
    val oolacileTownship = SoulsLocation(
        name = "Oolacile Township", // 乌拉席露市镇
        region = "Lordran", // 罗德兰
        characters = listOf(artorias),
        items = listOf(soulArrow),
    )
    val kilnOfTheFirstFlame = SoulsLocation(
        name = "Kiln of the First Flame", // 初始之火的火炉
        region = "Lordran", // 罗德兰
        characters = listOf(gwyn),
        items = listOf(zweihander),
    )
    val lordran = SoulsRegion(
        name = "Lordran", // 罗德兰
        locations = listOf(firelinkShrine, undeadBurg, anorLondo, oolacileTownship, kilnOfTheFirstFlame),
    )

    val majula = SoulsLocation(
        name = "Majula", // 如蜜
        region = "Drangleic", // 多兰古雷格
        characters = listOf(lucatiel),
        items = listOf(heal),
    )
    val forestOfFallenGiants = SoulsLocation(
        name = "Forest of Fallen Giants", // 巨人陨落之森
        region = "Drangleic", // 多兰古雷格
        characters = listOf(thePursuer),
        items = listOf(blacksteelKatana),
    )
    val brumeTower = SoulsLocation(
        name = "Brume Tower", // 烟雾塔
        region = "Drangleic", // 多兰古雷格
        characters = listOf(fumeKnight),
        items = listOf(fumeUltraGreatsword),
    )
    val shrineOfAmana = SoulsLocation(
        name = "Shrine of Amana", // 阿玛那祭坛
        region = "Drangleic", // 多兰古雷格
        characters = listOf(nashandra),
        items = listOf(greatLightningSpear, darkOrb),
    )
    val drangleic = SoulsRegion(
        name = "Drangleic", // 多兰古雷格
        locations = listOf(majula, forestOfFallenGiants, brumeTower, shrineOfAmana),
    )

    val cemeteryOfAsh = SoulsLocation(
        name = "Cemetery of Ash", // 灰烬墓地
        region = "Lothric", // 洛斯里克
        characters = listOf(iudexGundyr, fireKeeper),
        items = listOf(soulStream),
    )
    val highWallOfLothric = SoulsLocation(
        name = "High Wall of Lothric", // 洛斯里克的高墙
        region = "Lothric", // 洛斯里克
        characters = listOf(anri),
        items = listOf(lothricKnightSword),
    )
    val farronKeep = SoulsLocation(
        name = "Farron Keep", // 法兰要塞
        region = "Lothric", // 洛斯里克
        characters = listOf(abyssWatchers),
        items = listOf(carthusCurvedSword),
    )
    val irithyll = SoulsLocation(
        name = "Irithyll of the Boreal Valley", // 冷冽谷的伊鲁席尔
        region = "Lothric", // 洛斯里克
    )
    val smoulderingLake = SoulsLocation(
        name = "Smouldering Lake", // 熏烟湖
        region = "Lothric", // 洛斯里克
        items = listOf(courtSorcerersStaff, chaosBedVestiges),
    )
    val lothric = SoulsRegion(
        name = "Lothric", // 洛斯里克
        locations = listOf(cemeteryOfAsh, highWallOfLothric, farronKeep, irithyll, smoulderingLake),
    )

    val paintedWorldOfAriandel = SoulsLocation(
        name = "Painted World of Ariandel", // 艾雷德尔的绘画世界
        region = "The Painted World", // 绘画世界
        characters = listOf(sisterFriede),
        items = listOf(tearsOfDenial),
    )
    val paintedWorld = SoulsRegion(
        name = "The Painted World", // 绘画世界
        locations = listOf(paintedWorldOfAriandel),
    )

    val ringedCity = SoulsLocation(
        name = "Ringed City", // 环印城
        region = "The Ringed City", // 环印城
        characters = listOf(slaveKnightGael),
    )
    val theDregHeap = SoulsLocation(
        name = "The Dreg Heap", // 聚集地
        region = "The Ringed City", // 环印城
    )
    val theRingedCity = SoulsRegion(
        name = "The Ringed City", // 环印城
        locations = listOf(ringedCity, theDregHeap),
    )

    // endregion

    // region 扁平数据

    val soulsBosses = listOf(
        asylumDemon, ornsteinAndSmough, artorias, gwyn,
        thePursuer, fumeKnight, nashandra,
        iudexGundyr, abyssWatchers, sisterFriede, slaveKnightGael,
    )
    val soulsNpcs = listOf(solaire, siegmeyer, lucatiel, fireKeeper, anri)
    val soulsCharacters = soulsBosses + soulsNpcs
    val soulsWeapons = listOf(
        longsword, zweihander, uchigatana, moonlightGreatsword, chaosBlade,
        fumeUltraGreatsword, blacksteelKatana,
        carthusCurvedSword, lothricKnightSword, courtSorcerersStaff,
    )
    val soulsSpells = listOf(
        soulArrow, heal, fireball,
        darkOrb, greatLightningSpear,
        soulStream, tearsOfDenial, chaosBedVestiges,
    )
    val soulsRegions = listOf(lordran, drangleic, lothric, paintedWorld, theRingedCity)

    // endregion
}

/**
 * 以先序遍历方式遍历当前节点及其所有后代节点。
 */
internal fun SoulsNode.soulsDescendants(): PushStream<SoulsNode> = PushStream.from {
    fun visit(node: SoulsNode): Boolean {
        if (!invoke(node)) return false
        for (child in node.children()) if (!visit(child)) return false
        return true
    }
    visit(this@soulsDescendants)
}

/**
 * 以先序遍历方式遍历当前节点的所有父子边。
 */
internal fun SoulsNode.soulsEdges(): BiPushStream<SoulsNode, SoulsNode> = BiPushStream.from {
    fun visit(node: SoulsNode): Boolean {
        for (child in node.children()) {
            if (!invoke(node, child)) return false
            if (!visit(child)) return false
        }
        return true
    }
    visit(this@soulsEdges)
}

/**
 * 以先序遍历方式遍历当前节点及其所有后代节点，并同时提供节点深度。
 */
internal fun SoulsNode.soulsNodeDepths(depth: Int = 0): BiPushStream<Int, SoulsNode> = BiPushStream.from {
    fun visit(node: SoulsNode, d: Int): Boolean {
        if (!invoke(d, node)) return false
        for (child in node.children()) if (!visit(child, d + 1)) return false
        return true
    }
    visit(this@soulsNodeDepths, depth)
}