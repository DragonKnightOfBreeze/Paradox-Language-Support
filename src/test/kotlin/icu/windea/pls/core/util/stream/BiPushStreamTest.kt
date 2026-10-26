package icu.windea.pls.core.util.stream

import org.junit.Assert
import org.junit.Test

/**
 * [BiPushStream] 的单元测试。
 *
 * @see BiPushStream
 */
class BiPushStreamTest {
    private fun <T1, T2> List<Pair<T1, T2>>.toBiPushStream(): BiPushStream<T1, T2> = BiPushStream.from {
        for ((e1, e2) in this@toBiPushStream) if (!invoke(e1, e2)) break
    }

    // region source

    @Test
    fun of_emptyStream_test() {
        val stream = BiPushStream.of<SoulsWeaponCategory, SoulsWeapon>()
        val visited = mutableListOf<Pair<SoulsWeaponCategory, SoulsWeapon>>()
        stream.forEach { category, weapon -> visited += category to weapon }
        Assert.assertTrue(visited.isEmpty())
        Assert.assertFalse(stream.any { _, _ -> true })
        Assert.assertTrue(stream.all { _, _ -> false })
        Assert.assertTrue(stream.none { _, _ -> true })
        Assert.assertNull(stream.firstOrNull())
        Assert.assertNull(stream.firstOrNull { _, _ -> true })
        Assert.assertNull(stream.firstNotNullOrOrNull { _, _ -> "value" }) // 任意非空结果
        Assert.assertNull(stream.toOne())
        Assert.assertEquals(emptyList<Pair<SoulsWeaponCategory, SoulsWeapon>>(), stream.toList())
        Assert.assertEquals(emptySet<Pair<SoulsWeaponCategory, SoulsWeapon>>(), stream.toSet())
    }

    @Test
    fun from_pairs_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val stream = BiPushStream.from {
            for ((category, weapon) in pairs) if (!invoke(category, weapon)) break
        }
        Assert.assertEquals(pairs, stream.toList())
    }

    @Test
    fun dispatch_customProducer_test() {
        val stream = BiPushStream { downstream ->
            downstream("Solaire of Astora", "Astora") // 亚斯特拉的索拉尔，亚斯特拉
            downstream("Siegmeyer of Catarina", "Catarina") // 卡塔利纳的希格迈耶，卡塔利纳
        }
        val result = mutableListOf<Pair<String, String>>()
        stream.dispatch { character, homeland -> result += character to homeland; true }
        Assert.assertEquals(
            listOf("Solaire of Astora" to "Astora", "Siegmeyer of Catarina" to "Catarina"), // 亚斯特拉的索拉尔，亚斯特拉；卡塔利纳的希格迈耶，卡塔利纳
            result,
        )
    }

    // endregion

    // region of overloads

    @Test
    fun of_vararg_test() {
        val pairs = SoulsTestData.soulsWeapons.take(3).map { it.category to it }
        val result = BiPushStream.of(pairs[0], pairs[1], pairs[2]).toList()
        Assert.assertEquals(pairs, result)
    }

    @Test
    fun of_vararg_empty_test() {
        val result = BiPushStream.of(*emptyArray<Pair<Int, String>>()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_iterable_test() {
        val pairs = SoulsTestData.soulsBosses.map { it.game to it }
        val result = BiPushStream.of(pairs).toList()
        Assert.assertEquals(pairs, result)
    }

    @Test
    fun of_iterable_empty_test() {
        val result = BiPushStream.of(emptyList<Pair<Int, String>>()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_iterator_test() {
        val pairs = SoulsTestData.soulsSpells.map { it.category to it }
        val result = BiPushStream.of(pairs.iterator()).toList()
        Assert.assertEquals(pairs, result)
    }

    @Test
    fun of_iterator_empty_test() {
        val result = BiPushStream.of(emptyList<Pair<Int, String>>().iterator()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_iterator_respectsShortCircuit_test() {
        var nextCount = 0
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val delegate = pairs.iterator()
        val iterator = object : Iterator<Pair<SoulsWeaponCategory, SoulsWeapon>> {
            override fun hasNext() = delegate.hasNext()

            override fun next(): Pair<SoulsWeaponCategory, SoulsWeapon> {
                nextCount++
                return delegate.next()
            }
        }
        val result = BiPushStream.of(iterator).take(2).toList()
        Assert.assertEquals(pairs.take(2), result)
        Assert.assertEquals(2, nextCount)
    }

    @Test
    fun of_sequence_test() {
        val pairs = SoulsTestData.soulsCharacters.map { it.kind to it }
        val result = BiPushStream.of(pairs.asSequence()).toList()
        Assert.assertEquals(pairs, result)
    }

    @Test
    fun of_sequence_empty_test() {
        val result = BiPushStream.of(emptySequence<Pair<Int, String>>()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_sequence_isLazy_test() {
        var produced = 0
        val pairs = SoulsTestData.soulsBosses.map { it.game to it }
        val sequence = sequence {
            for (pair in pairs) {
                produced++
                yield(pair)
            }
        }
        val result = BiPushStream.of(sequence).take(2).toList()
        Assert.assertEquals(pairs.take(2), result)
        Assert.assertEquals(2, produced)
    }

    // endregion

    // region forEach

    @Test
    fun forEach_allElementsInOrder_test() {
        val pairs = SoulsTestData.soulsSpells.map { it.category to it }
        val visited = mutableListOf<Pair<SoulsSpellCategory, SoulsSpell>>()
        pairs.toBiPushStream().forEach { category, spell -> visited += category to spell }
        Assert.assertEquals(pairs, visited)
    }

    @Test
    fun forEach_emptyStream_test() {
        val visited = mutableListOf<Pair<SoulsSpellCategory, SoulsSpell>>()
        BiPushStream.of<SoulsSpellCategory, SoulsSpell>().forEach { category, spell -> visited += category to spell }
        Assert.assertTrue(visited.isEmpty())
    }

    // endregion

    // region process

    @Test
    fun process_allMatched_test() {
        val pairs = SoulsTestData.soulsBosses.map { it.game to it }
        val visited = mutableListOf<Pair<SoulsGame, SoulsCharacter>>()
        val result = pairs.toBiPushStream().process { game, boss -> visited += game to boss; true }
        Assert.assertTrue(result)
        Assert.assertEquals(pairs, visited)
    }

    @Test
    fun process_stopAtFirstUnmatched_test() {
        val pairs = SoulsTestData.soulsBosses.map { it.game to it }
        val stopIndex = pairs.indexOfFirst { (_, boss) -> boss.name == "The Pursuer" } // 咒缚者
        val visited = mutableListOf<Pair<SoulsGame, SoulsCharacter>>()
        val result = pairs.toBiPushStream().process { game, boss -> visited += game to boss; boss.name != "The Pursuer" } // 咒缚者
        Assert.assertFalse(result)
        Assert.assertEquals(pairs.subList(0, stopIndex + 1), visited)
    }

    @Test
    fun process_emptyStream_returnsTrue_test() {
        Assert.assertTrue(BiPushStream.of<SoulsGame, SoulsCharacter>().process { _, _ -> true })
    }

    // endregion

    // region any

    @Test
    fun any_found_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val matchIndex = pairs.indexOfFirst { (_, weapon) -> weapon.name == "Uchigatana" } // 打刀
        val visited = mutableListOf<Pair<SoulsWeaponCategory, SoulsWeapon>>()
        val result = pairs.toBiPushStream().any { category, weapon ->
            visited += category to weapon
            weapon.name == "Uchigatana" // 打刀
        }
        Assert.assertTrue(result)
        Assert.assertEquals(pairs.subList(0, matchIndex + 1), visited)
    }

    @Test
    fun any_notFound_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val visited = mutableListOf<Pair<SoulsWeaponCategory, SoulsWeapon>>()
        val result = pairs.toBiPushStream().any { category, weapon ->
            visited += category to weapon
            weapon.name == "Moonlight Butterfly Horn" // 月光蝴蝶角（不存在的数据）
        }
        Assert.assertFalse(result)
        Assert.assertEquals(pairs.size, visited.size)
    }

    @Test
    fun any_emptyStream_returnsFalse_test() {
        Assert.assertFalse(BiPushStream.of<SoulsGame, SoulsCharacter>().any { _, _ -> true })
    }

    // endregion

    // region all

    @Test
    fun all_matched_test() {
        val pairs = SoulsTestData.soulsCharacters.map { it.kind to it }
        val visited = mutableListOf<Pair<SoulsCharacterKind, SoulsCharacter>>()
        val result = pairs.toBiPushStream().all { kind, character -> visited += kind to character; character.kind == kind }
        Assert.assertTrue(result)
        Assert.assertEquals(pairs, visited)
    }

    @Test
    fun all_stopAtFirstUnmatched_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val stopIndex = pairs.indexOfFirst { (_, weapon) -> weapon.name == "Zweihander" } // 双手巨剑
        val visited = mutableListOf<Pair<SoulsWeaponCategory, SoulsWeapon>>()
        val result = pairs.toBiPushStream().all { category, weapon ->
            visited += category to weapon
            weapon.name != "Zweihander" // 双手巨剑
        }
        Assert.assertFalse(result)
        Assert.assertEquals(pairs.subList(0, stopIndex + 1), visited)
    }

    @Test
    fun all_emptyStream_returnsTrue_test() {
        Assert.assertTrue(BiPushStream.of<SoulsGame, SoulsCharacter>().all { _, _ -> false })
    }

    // endregion

    // region none

    @Test
    fun none_noMatch_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val result = pairs.toBiPushStream().none { category, weapon ->
            category == SoulsWeaponCategory.Staff && weapon.game == SoulsGame.DarkSouls1
        }
        Assert.assertTrue(result)
    }

    @Test
    fun none_stopAtFirstMatch_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val matchIndex = pairs.indexOfFirst { (_, weapon) -> weapon.name == "Uchigatana" } // 打刀
        val visited = mutableListOf<Pair<SoulsWeaponCategory, SoulsWeapon>>()
        val result = pairs.toBiPushStream().none { category, weapon ->
            visited += category to weapon
            weapon.name == "Uchigatana" // 打刀
        }
        Assert.assertFalse(result)
        Assert.assertEquals(pairs.subList(0, matchIndex + 1), visited)
    }

    @Test
    fun none_emptyStream_returnsTrue_test() {
        Assert.assertTrue(BiPushStream.of<SoulsGame, SoulsCharacter>().none { _, _ -> true })
    }

    // endregion

    // region firstNotNullOrOrNull

    @Test
    fun firstNotNullOrOrNull_found_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val matchIndex = pairs.indexOfFirst { (category, _) -> category == SoulsWeaponCategory.UltraGreatsword }
        var visited = 0
        val result = pairs.toBiPushStream().firstNotNullOrOrNull { category, weapon ->
            visited++
            weapon.name.takeIf { category == SoulsWeaponCategory.UltraGreatsword }
        }
        Assert.assertEquals(pairs[matchIndex].second.name, result)
        Assert.assertEquals(matchIndex + 1, visited)
    }

    @Test
    fun firstNotNullOrOrNull_allNull_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        var visited = 0
        val result = pairs.toBiPushStream().firstNotNullOrOrNull { _, _ ->
            visited++
            null
        }
        Assert.assertNull(result)
        Assert.assertEquals(pairs.size, visited)
    }

    @Test
    fun firstNotNullOrOrNull_emptyStream_returnsNull_test() {
        Assert.assertNull(BiPushStream.of<SoulsWeaponCategory, SoulsWeapon>().firstNotNullOrOrNull { _, weapon -> weapon.name })
    }

    // endregion

    // region firstOrNull

    @Test
    fun firstOrNull_noPredicate_returnsFirstPair_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        var produced = 0
        val stream = BiPushStream.from {
            for ((category, weapon) in pairs) {
                produced++
                if (!invoke(category, weapon)) break
            }
        }
        Assert.assertEquals(pairs.first(), stream.firstOrNull())
        Assert.assertEquals(1, produced)
    }

    @Test
    fun firstOrNull_emptyStream_returnsNull_test() {
        Assert.assertNull(BiPushStream.of<SoulsWeaponCategory, SoulsWeapon>().firstOrNull())
    }

    @Test
    fun firstOrNull_predicate_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val matchIndex = pairs.indexOfFirst { (_, weapon) -> weapon.game == SoulsGame.DarkSouls2 }
        var produced = 0
        val stream = BiPushStream.from {
            for ((category, weapon) in pairs) {
                produced++
                if (!invoke(category, weapon)) break
            }
        }
        val result = stream.firstOrNull { _, weapon -> weapon.game == SoulsGame.DarkSouls2 }
        Assert.assertEquals(pairs[matchIndex], result)
        Assert.assertEquals(matchIndex + 1, produced)
    }

    @Test
    fun firstOrNull_noMatch_returnsNull_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        Assert.assertNull(pairs.toBiPushStream().firstOrNull { _, weapon -> weapon.name == "Moonlight Butterfly Horn" }) // 月光蝴蝶角（不存在的数据）
    }

    // endregion

    // region find

    @Test
    fun find_aliasOfFirstOrNull_test() {
        val pairs = SoulsTestData.soulsSpells.map { it.category to it }
        val expected = pairs.first { (category, _) -> category == SoulsSpellCategory.Miracle }
        Assert.assertEquals(expected, pairs.toBiPushStream().find { category, _ -> category == SoulsSpellCategory.Miracle })
        Assert.assertNull(pairs.toBiPushStream().find { _, spell -> spell.name == "Wrath of the Gods" }) // 诸神之怒（不存在的数据）
    }

    // endregion

    // region map

    @Test
    fun map_transformInOrder_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val result = pairs.toBiPushStream().map { category, weapon -> "${category.name}: ${weapon.name}" }.toList()
        Assert.assertEquals(pairs.map { (category, weapon) -> "${category.name}: ${weapon.name}" }, result)
    }

    @Test
    fun map_isLazy_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        var transformed = 0
        val result = pairs.toBiPushStream()
            .map { _, weapon -> transformed++; weapon.name }
            .take(2)
            .toList()
        Assert.assertEquals(pairs.take(2).map { it.second.name }, result)
        Assert.assertEquals(2, transformed)
    }

    @Test
    fun map_emptyStream_test() {
        Assert.assertTrue(BiPushStream.of<SoulsWeaponCategory, SoulsWeapon>().map { _, weapon -> weapon.name }.toList().isEmpty())
    }

    // endregion

    // region mapNotNull

    @Test
    fun mapNotNull_transformAndFilter_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val expected = pairs
            .filter { (category, _) -> category == SoulsWeaponCategory.Katana }
            .map { (_, weapon) -> weapon.name }
        val result = pairs.toBiPushStream().mapNotNull { category, weapon ->
            weapon.name.takeIf { category == SoulsWeaponCategory.Katana }
        }.toList()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun mapNotNull_allNull_returnsEmpty_test() {
        val pairs = SoulsTestData.soulsSpells.map { it.category to it }
        val result = pairs.toBiPushStream().mapNotNull { _, _ ->
            null as String?
        }.toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun mapNotNull_emptyStream_test() {
        val result = BiPushStream.of<SoulsWeaponCategory, SoulsWeapon>().mapNotNull { _, weapon -> weapon.name }.toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun mapNotNull_isLazy_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        var transformed = 0
        val target = SoulsTestData.zweihander
        val result = pairs.toBiPushStream()
            .mapNotNull { _, weapon ->
                transformed++
                weapon.name.takeIf { weapon == target }
            }
            .take(1)
            .toList()
        Assert.assertEquals(listOf(target.name), result)
        Assert.assertEquals(2, transformed)
    }

    @Test
    fun mapNotNull_stopsWhenDownstreamStops_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        var transformed = 0
        val result = pairs.toBiPushStream()
            .mapNotNull { category, weapon ->
                transformed++
                weapon.name.takeIf { category == SoulsWeaponCategory.Katana }
            }
            .firstOrNull()
        Assert.assertEquals(pairs.first { (category, _) -> category == SoulsWeaponCategory.Katana }.second.name, result)
        Assert.assertEquals(3, transformed)
    }

    // endregion

    // region filter

    @Test
    fun filter_keepMatchedInOrder_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val expected = pairs.filter { (category, _) -> category == SoulsWeaponCategory.Katana }
        val result = pairs.toBiPushStream().filter { category, _ -> category == SoulsWeaponCategory.Katana }.toList()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun filter_isLazy_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        val firstKatanaIndex = pairs.indexOfFirst { (category, _) -> category == SoulsWeaponCategory.Katana }
        var visited = 0
        val result = pairs.toBiPushStream()
            .filter { category, _ -> visited++; category == SoulsWeaponCategory.Katana }
            .take(1)
            .toList()
        Assert.assertEquals(listOf(pairs[firstKatanaIndex]), result)
        Assert.assertEquals(firstKatanaIndex + 1, visited)
    }

    @Test
    fun filter_noMatch_returnsEmpty_test() {
        val pairs = SoulsTestData.soulsSpells.map { it.category to it }
        val result = pairs.toBiPushStream().filter { _, spell -> spell.name == "Wrath of the Gods" }.toList() // 诸神之怒（不存在的数据）
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun filter_emptyStream_test() {
        val result = BiPushStream.of<SoulsSpellCategory, SoulsSpell>()
            .filter { category, _ -> category == SoulsSpellCategory.Hex }
            .toList()
        Assert.assertTrue(result.isEmpty())
    }

    // endregion

    // region filterNotNull

    @Test
    fun filterNotNull_keepsNonNullPairsInOrder_test() {
        val pairs = listOf(
            SoulsTestData.solaire.name to 1,
            null to 2,
            SoulsTestData.siegmeyer.name to null,
            null to null,
            SoulsTestData.anri.name to 3,
        )
        val result = pairs.toBiPushStream().filterNotNull().toList()
        Assert.assertEquals(listOf(SoulsTestData.solaire.name to 1, SoulsTestData.anri.name to 3), result)
    }

    @Test
    fun filterNotNull_allNull_returnsEmpty_test() {
        val pairs = listOf<Pair<String?, Int?>>(null to null, null to 2)
        val result = pairs.toBiPushStream().filterNotNull().toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun filterNotNull_emptyStream_test() {
        val result = BiPushStream.of<String?, Int?>().filterNotNull().toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun filterNotNull_isLazy_test() {
        val pairs = listOf(null to 1, null to null, SoulsTestData.anri.name to 3, SoulsTestData.gwyn.name to 4)
        var visited = 0
        val source = BiPushStream.from {
            for ((e1, e2) in pairs) {
                visited++
                if (!invoke(e1, e2)) break
            }
        }
        val result = source.filterNotNull().take(1).toList()
        Assert.assertEquals(listOf(SoulsTestData.anri.name to 3), result)
        Assert.assertEquals(3, visited)
    }

    @Test
    fun filterNotNull_stopsWhenDownstreamStops_test() {
        val pairs = listOf(null to 1, null to null, SoulsTestData.gwyn.name to 4)
        var visited = 0
        val source = BiPushStream.from {
            for ((e1, e2) in pairs) {
                visited++
                if (!invoke(e1, e2)) break
            }
        }
        Assert.assertEquals(SoulsTestData.gwyn.name to 4, source.filterNotNull().firstOrNull())
        Assert.assertEquals(3, visited)
    }

    // endregion

    // region take

    @Test
    fun take_lessThanSize_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        Assert.assertEquals(pairs.take(3), pairs.toBiPushStream().take(3).toList())
    }

    @Test
    fun take_greaterThanSize_test() {
        val pairs = SoulsTestData.soulsWeapons.map { it.category to it }
        Assert.assertEquals(pairs, pairs.toBiPushStream().take(100).toList())
    }

    @Test
    fun take_zeroOrNegative_returnsEmptyWithoutProducing_test() {
        val pairs = SoulsTestData.soulsCharacters.map { it.kind to it }
        var produced = 0
        val stream = BiPushStream.from {
            for ((kind, character) in pairs) {
                produced++
                if (!invoke(kind, character)) break
            }
        }
        Assert.assertTrue(stream.take(0).toList().isEmpty())
        Assert.assertTrue(stream.take(-1).toList().isEmpty())
        Assert.assertEquals(0, produced)
    }

    @Test
    fun take_infiniteStream_test() {
        var produced = 0
        val stream = BiPushStream.from {
            var i = 0
            while (true) {
                produced++
                if (!invoke(i, i * i)) break
                i++
            }
        }
        Assert.assertEquals(listOf(0 to 0, 1 to 1, 2 to 4), stream.take(3).toList())
        Assert.assertEquals(3, produced)
    }

    @Test
    fun take_stopsWhenDownstreamStops_test() {
        var produced = 0
        val stream = BiPushStream.from {
            var i = 0
            while (true) {
                produced++
                if (!invoke(i, i * i)) break
                i++
            }
        }
        Assert.assertEquals(2 to 4, stream.take(10).firstOrNull { a, _ -> a >= 2 })
        Assert.assertEquals(3, produced)
    }

    @Test
    fun take_afterFilter_test() {
        val pairs = SoulsTestData.soulsSpells.map { it.game to it }
        val expected = pairs.filter { (game, _) -> game == SoulsGame.DarkSouls3 }.take(2)
        val result = pairs.toBiPushStream().filter { game, _ -> game == SoulsGame.DarkSouls3 }.take(2).toList()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun take_chained_test() {
        val pairs = SoulsTestData.soulsBosses.map { it.game to it }
        Assert.assertEquals(pairs.take(2), pairs.toBiPushStream().take(5).take(2).toList())
    }

    // endregion

    // region conversion

    @Test
    fun toOne_returnsFirstPair_test() {
        val pairs = SoulsTestData.soulsBosses.map { it.game to it }
        Assert.assertEquals(pairs.first(), pairs.toBiPushStream().toOne())
        Assert.assertNull(BiPushStream.of<SoulsGame, SoulsCharacter>().toOne())
    }

    @Test
    fun toList_keepsDuplicatesAndOrder_test() {
        val pair = SoulsTestData.asylumDemon.game to SoulsTestData.asylumDemon
        val expected = listOf(pair, pair, SoulsTestData.gwyn.game to SoulsTestData.gwyn)
        Assert.assertEquals(expected, expected.toBiPushStream().toList())
    }

    @Test
    fun toSet_removesDuplicates_test() {
        val first = SoulsTestData.asylumDemon.game to SoulsTestData.asylumDemon
        val second = SoulsTestData.gwyn.game to SoulsTestData.gwyn
        val stream = listOf(first, second, first).toBiPushStream()
        Assert.assertEquals(listOf(first, second), stream.toSet().toList())
    }

    @Test
    fun toList_emptyStream_test() {
        Assert.assertTrue(BiPushStream.of<SoulsGame, SoulsCharacter>().toList().isEmpty())
        Assert.assertTrue(BiPushStream.of<SoulsGame, SoulsCharacter>().toSet().isEmpty())
    }

    @Test
    fun repeatedTermination_producesSameResult_test() {
        val stream = SoulsTestData.soulsSpells.map { it.category to it }.toBiPushStream()
        Assert.assertEquals(stream.toList(), stream.toList())
        Assert.assertEquals(stream.firstOrNull(), stream.firstOrNull())
    }

    // endregion

    // region tree

    @Test
    fun edges_preorderTraversal_test() {
        val lordran = SoulsTestData.lordran
        val edgeNames = lordran.soulsEdges().map { parent, child -> parent.name to child.name }.toList()
        Assert.assertEquals("Lordran" to "Firelink Shrine", edgeNames.first()) // 罗德兰 -> 传火祭祀场
        Assert.assertTrue(edgeNames.contains("Firelink Shrine" to "Solaire of Astora")) // 传火祭祀场 -> 亚斯特拉的索拉尔
        Assert.assertTrue(edgeNames.contains("Anor Londo" to "Ornstein & Smough")) // 亚诺尔隆德 -> 猎龙者翁斯坦与刽子手斯摩
        Assert.assertTrue(edgeNames.contains("Kiln of the First Flame" to "Gwyn, Lord of Cinder")) // 初始之火的火炉 -> 薪王葛温
        // 树中边数等于节点数减一
        Assert.assertEquals(lordran.soulsDescendants().toList().size - 1, edgeNames.size)
    }

    @Test
    fun edges_allRegions_test() {
        val regions = SoulsTestData.soulsRegions
        val nodeCount = regions.sumOf { it.soulsDescendants().toList().size }
        val edges = regions.flatMap { it.soulsEdges().toList() }
        Assert.assertEquals(nodeCount - regions.size, edges.size)
        // 每个地区的根节点都是其子树中第一条边的父节点
        Assert.assertEquals(regions, edges.map { (parent, _) -> parent }.filterIsInstance<SoulsRegion>().distinct())
    }

    @Test
    fun edges_takeStopsTraversal_test() {
        val result = SoulsTestData.lordran.soulsEdges()
            .take(3)
            .map { parent, child -> parent.name to child.name }
            .toList()
        Assert.assertEquals(
            listOf(
                "Lordran" to "Firelink Shrine", // 罗德兰 -> 传火祭祀场
                "Firelink Shrine" to "Solaire of Astora", // 传火祭祀场 -> 亚斯特拉的索拉尔
                "Firelink Shrine" to "Siegmeyer of Catarina", // 传火祭祀场 -> 卡塔利纳的希格迈耶
            ),
            result,
        )
    }

    @Test
    fun edges_filterByChildType_test() {
        val locationEdges = SoulsTestData.lordran.soulsEdges()
            .filter { _, child -> child is SoulsLocation }
            .toList()
        Assert.assertEquals(SoulsTestData.lordran.locations.size, locationEdges.size)
        Assert.assertTrue(locationEdges.all { (parent, _) -> parent == SoulsTestData.lordran })
    }

    @Test
    fun edges_filterByParentAndChildType_test() {
        val characterEdges = SoulsTestData.lordran.soulsEdges()
            .filter { parent, child -> parent is SoulsLocation && child is SoulsCharacter }
            .toList()
        Assert.assertEquals(
            listOf(
                SoulsTestData.firelinkShrine to SoulsTestData.solaire,
                SoulsTestData.firelinkShrine to SoulsTestData.siegmeyer,
                SoulsTestData.anorLondo to SoulsTestData.ornsteinAndSmough,
                SoulsTestData.oolacileTownship to SoulsTestData.artorias,
                SoulsTestData.kilnOfTheFirstFlame to SoulsTestData.gwyn,
            ),
            characterEdges,
        )
    }

    @Test
    fun nodeDepths_preorderTraversal_test() {
        val depths = SoulsTestData.lordran.soulsNodeDepths().toList()
        Assert.assertEquals(0 to SoulsTestData.lordran, depths[0])
        Assert.assertEquals(1 to SoulsTestData.firelinkShrine, depths[1])
        Assert.assertEquals(2 to SoulsTestData.solaire, depths[2])
        Assert.assertEquals(2 to SoulsTestData.siegmeyer, depths[3])
        Assert.assertEquals(2 to SoulsTestData.longsword, depths[4])
        Assert.assertEquals(1 to SoulsTestData.undeadBurg, depths[5])
    }

    @Test
    fun nodeDepths_takeStopsTraversal_test() {
        val result = SoulsTestData.lordran.soulsNodeDepths().take(3).toList()
        Assert.assertEquals(
            listOf(
                0 to SoulsTestData.lordran,
                1 to SoulsTestData.firelinkShrine,
                2 to SoulsTestData.solaire,
            ),
            result,
        )
    }

    // endregion
}
