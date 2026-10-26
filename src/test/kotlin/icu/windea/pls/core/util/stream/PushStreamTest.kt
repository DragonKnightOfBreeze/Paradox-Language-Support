package icu.windea.pls.core.util.stream

import org.junit.Assert
import org.junit.Test

/**
 * [PushStream] 的单元测试。
 *
 * @see PushStream
 */
class PushStreamTest {
    private fun <T> List<T>.toPushStream(): PushStream<T> = PushStream.from {
        for (e in this@toPushStream) if (!invoke(e)) break
    }

    // region source

    @Test
    fun of_emptyStream_test() {
        val stream = PushStream.of<SoulsCharacter>()
        val visited = mutableListOf<SoulsCharacter>()
        stream.forEach { visited += it }
        Assert.assertTrue(visited.isEmpty())
        Assert.assertFalse(stream.any { true })
        Assert.assertTrue(stream.all { false })
        Assert.assertTrue(stream.none { true })
        Assert.assertNull(stream.firstOrNull())
        Assert.assertNull(stream.firstOrNull { true })
        Assert.assertNull(stream.firstNotNullOrOrNull { it.name })
        Assert.assertNull(stream.toOne())
        Assert.assertEquals(emptyList<SoulsCharacter>(), stream.toList())
        Assert.assertEquals(emptySet<SoulsCharacter>(), stream.toSet())
    }

    @Test
    fun from_list_test() {
        val stream = PushStream.from {
            for (boss in SoulsTestData.soulsBosses) if (!invoke(boss)) break
        }
        Assert.assertEquals(SoulsTestData.soulsBosses, stream.toList())
    }

    @Test
    fun dispatch_customProducer_test() {
        val stream = PushStream { downstream ->
            downstream("Solaire of Astora") // 亚斯特拉的索拉尔
            downstream("Siegmeyer of Catarina") // 卡塔利纳的希格迈耶
        }
        val result = mutableListOf<String>()
        stream.dispatch { result += it; true }
        Assert.assertEquals(listOf("Solaire of Astora", "Siegmeyer of Catarina"), result) // 亚斯特拉的索拉尔、卡塔利纳的希格迈耶
    }

    // endregion

    // region of overloads

    @Test
    fun of_vararg_test() {
        val result = PushStream.of(SoulsTestData.solaire, SoulsTestData.siegmeyer, SoulsTestData.anri).toList()
        Assert.assertEquals(listOf(SoulsTestData.solaire, SoulsTestData.siegmeyer, SoulsTestData.anri), result)
    }

    @Test
    fun of_vararg_empty_test() {
        val result = PushStream.of(*emptyArray<SoulsCharacter>()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_iterable_test() {
        val result = PushStream.of(SoulsTestData.soulsBosses).toList()
        Assert.assertEquals(SoulsTestData.soulsBosses, result)
    }

    @Test
    fun of_iterable_empty_test() {
        val result = PushStream.of(emptyList<SoulsCharacter>()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_iterator_test() {
        val result = PushStream.of(SoulsTestData.soulsWeapons.iterator()).toList()
        Assert.assertEquals(SoulsTestData.soulsWeapons, result)
    }

    @Test
    fun of_iterator_empty_test() {
        val result = PushStream.of(emptyList<SoulsWeapon>().iterator()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_iterator_respectsShortCircuit_test() {
        var nextCount = 0
        val delegate = SoulsTestData.soulsBosses.iterator()
        val iterator = object : Iterator<SoulsCharacter> {
            override fun hasNext() = delegate.hasNext()

            override fun next(): SoulsCharacter {
                nextCount++
                return delegate.next()
            }
        }
        val result = PushStream.of(iterator).take(2).toList()
        Assert.assertEquals(SoulsTestData.soulsBosses.take(2), result)
        Assert.assertEquals(2, nextCount)
    }

    @Test
    fun of_sequence_test() {
        val result = PushStream.of(SoulsTestData.soulsSpells.asSequence()).toList()
        Assert.assertEquals(SoulsTestData.soulsSpells, result)
    }

    @Test
    fun of_sequence_empty_test() {
        val result = PushStream.of(emptySequence<SoulsSpell>()).toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun of_sequence_isLazy_test() {
        var produced = 0
        val sequence = sequence {
            for (boss in SoulsTestData.soulsBosses) {
                produced++
                yield(boss)
            }
        }
        val result = PushStream.of(sequence).take(2).toList()
        Assert.assertEquals(SoulsTestData.soulsBosses.take(2), result)
        Assert.assertEquals(2, produced)
    }

    // endregion

    // region forEach

    @Test
    fun forEach_allElementsInOrder_test() {
        val visited = mutableListOf<String>()
        SoulsTestData.soulsSpells.toPushStream().forEach { visited += it.name }
        Assert.assertEquals(SoulsTestData.soulsSpells.map { it.name }, visited)
    }

    @Test
    fun forEach_emptyStream_test() {
        val visited = mutableListOf<SoulsSpell>()
        PushStream.of<SoulsSpell>().forEach { visited += it }
        Assert.assertTrue(visited.isEmpty())
    }

    // endregion

    // region process

    @Test
    fun process_allMatched_test() {
        val visited = mutableListOf<String>()
        val result = SoulsTestData.soulsBosses.toPushStream().process { visited += it.name; true }
        Assert.assertTrue(result)
        Assert.assertEquals(SoulsTestData.soulsBosses.map { it.name }, visited)
    }

    @Test
    fun process_stopAtFirstUnmatched_test() {
        val bosses = SoulsTestData.soulsBosses
        val stopIndex = bosses.indexOfFirst { it.name == "The Pursuer" } // 咒缚者
        val visited = mutableListOf<String>()
        val result = bosses.toPushStream().process { visited += it.name; it.name != "The Pursuer" } // 咒缚者
        Assert.assertFalse(result)
        Assert.assertEquals(bosses.subList(0, stopIndex + 1).map { it.name }, visited)
    }

    @Test
    fun process_emptyStream_returnsTrue_test() {
        Assert.assertTrue(PushStream.of<SoulsCharacter>().process { true })
    }

    // endregion

    // region any

    @Test
    fun any_found_test() {
        val spells = SoulsTestData.soulsSpells
        val matchIndex = spells.indexOfFirst { it.category == SoulsSpellCategory.Pyromancy }
        val visited = mutableListOf<String>()
        val result = spells.toPushStream().any { visited += it.name; it.category == SoulsSpellCategory.Pyromancy }
        Assert.assertTrue(result)
        Assert.assertEquals(spells.subList(0, matchIndex + 1).map { it.name }, visited)
    }

    @Test
    fun any_notFound_test() {
        val visited = mutableListOf<String>()
        val result = SoulsTestData.soulsWeapons.toPushStream().any { visited += it.name; it.name == "Moonlight Butterfly Horn" } // 月光蝴蝶角（不存在的数据）
        Assert.assertFalse(result)
        Assert.assertEquals(SoulsTestData.soulsWeapons.size, visited.size)
    }

    @Test
    fun any_emptyStream_returnsFalse_test() {
        Assert.assertFalse(PushStream.of<SoulsCharacter>().any { true })
    }

    // endregion

    // region all

    @Test
    fun all_matched_test() {
        val visited = mutableListOf<String>()
        val result = SoulsTestData.soulsCharacters.toPushStream().all {
            visited += it.name
            it.kind == SoulsCharacterKind.Boss || it.kind == SoulsCharacterKind.Npc
        }
        Assert.assertTrue(result)
        Assert.assertEquals(SoulsTestData.soulsCharacters.map { it.name }, visited)
    }

    @Test
    fun all_stopAtFirstUnmatched_test() {
        val bosses = SoulsTestData.soulsBosses
        val stopIndex = bosses.indexOfFirst { it.name == "Artorias the Abysswalker" } // 深渊漫步者阿尔特留斯
        val visited = mutableListOf<String>()
        val result = bosses.toPushStream().all { visited += it.name; it.name != "Artorias the Abysswalker" } // 深渊漫步者阿尔特留斯
        Assert.assertFalse(result)
        Assert.assertEquals(bosses.subList(0, stopIndex + 1).map { it.name }, visited)
    }

    @Test
    fun all_emptyStream_returnsTrue_test() {
        Assert.assertTrue(PushStream.of<SoulsCharacter>().all { false })
    }

    // endregion

    // region none

    @Test
    fun none_noMatch_test() {
        val result = SoulsTestData.soulsWeapons.toPushStream()
            .none { it.category == SoulsWeaponCategory.Staff && it.game == SoulsGame.DarkSouls1 }
        Assert.assertTrue(result)
    }

    @Test
    fun none_stopAtFirstMatch_test() {
        val spells = SoulsTestData.soulsSpells
        val matchIndex = spells.indexOfFirst { it.category == SoulsSpellCategory.Hex }
        val visited = mutableListOf<String>()
        val result = spells.toPushStream().none { visited += it.name; it.category == SoulsSpellCategory.Hex }
        Assert.assertFalse(result)
        Assert.assertEquals(spells.subList(0, matchIndex + 1).map { it.name }, visited)
    }

    @Test
    fun none_emptyStream_returnsTrue_test() {
        Assert.assertTrue(PushStream.of<SoulsCharacter>().none { true })
    }

    // endregion

    // region firstNotNullOrOrNull

    @Test
    fun firstNotNullOrOrNull_found_test() {
        val weapons = SoulsTestData.soulsWeapons
        val matchIndex = weapons.indexOfFirst { it.category == SoulsWeaponCategory.UltraGreatsword }
        val visited = mutableListOf<String>()
        val result = weapons.toPushStream().firstNotNullOrOrNull { weapon ->
            visited += weapon.name
            weapon.name.takeIf { weapon.category == SoulsWeaponCategory.UltraGreatsword }
        }
        Assert.assertEquals(weapons[matchIndex].name, result)
        Assert.assertEquals(matchIndex + 1, visited.size)
    }

    @Test
    fun firstNotNullOrOrNull_allNull_test() {
        val visited = mutableListOf<String>()
        val result = SoulsTestData.soulsWeapons.toPushStream().firstNotNullOrOrNull { weapon ->
            visited += weapon.name
            null
        }
        Assert.assertNull(result)
        Assert.assertEquals(SoulsTestData.soulsWeapons.size, visited.size)
    }

    @Test
    fun firstNotNullOrOrNull_emptyStream_returnsNull_test() {
        Assert.assertNull(PushStream.of<SoulsWeapon>().firstNotNullOrOrNull { it.name })
    }

    // endregion

    // region firstOrNull

    @Test
    fun firstOrNull_noPredicate_returnsFirstElement_test() {
        var produced = 0
        val stream = PushStream.from {
            for (boss in SoulsTestData.soulsBosses) {
                produced++
                if (!invoke(boss)) break
            }
        }
        Assert.assertEquals(SoulsTestData.soulsBosses.first(), stream.firstOrNull())
        Assert.assertEquals(1, produced)
    }

    @Test
    fun firstOrNull_emptyStream_returnsNull_test() {
        Assert.assertNull(PushStream.of<SoulsCharacter>().firstOrNull())
    }

    @Test
    fun firstOrNull_predicate_test() {
        val bosses = SoulsTestData.soulsBosses
        val matchIndex = bosses.indexOfFirst { it.game == SoulsGame.DarkSouls2 }
        var produced = 0
        val stream = PushStream.from {
            for (boss in bosses) {
                produced++
                if (!invoke(boss)) break
            }
        }
        val result = stream.firstOrNull { it.game == SoulsGame.DarkSouls2 }
        Assert.assertEquals(bosses[matchIndex], result)
        Assert.assertEquals(matchIndex + 1, produced)
    }

    @Test
    fun firstOrNull_noMatch_returnsNull_test() {
        Assert.assertNull(SoulsTestData.soulsBosses.toPushStream().firstOrNull { it.kind == SoulsCharacterKind.Npc })
    }

    // endregion

    // region find

    @Test
    fun find_aliasOfFirstOrNull_test() {
        val expected = SoulsTestData.soulsSpells.first { it.category == SoulsSpellCategory.Miracle }
        Assert.assertEquals(expected, SoulsTestData.soulsSpells.toPushStream().find { it.category == SoulsSpellCategory.Miracle })
        Assert.assertNull(SoulsTestData.soulsSpells.toPushStream().find { it.name == "Wrath of the Gods" }) // 诸神之怒（不存在的数据）
    }

    // endregion

    // region map

    @Test
    fun map_transformInOrder_test() {
        val result = SoulsTestData.soulsBosses.toPushStream().map { it.name }.toList()
        Assert.assertEquals(SoulsTestData.soulsBosses.map { it.name }, result)
    }

    @Test
    fun map_isLazy_test() {
        var transformed = 0
        val result = SoulsTestData.soulsWeapons.toPushStream()
            .map { transformed++; it.name }
            .take(2)
            .toList()
        Assert.assertEquals(SoulsTestData.soulsWeapons.take(2).map { it.name }, result)
        Assert.assertEquals(2, transformed)
    }

    @Test
    fun map_emptyStream_test() {
        Assert.assertTrue(PushStream.of<SoulsSpell>().map { it.name }.toList().isEmpty())
    }

    // endregion

    // region mapNotNull

    @Test
    fun mapNotNull_transformAndFilter_test() {
        val expected = SoulsTestData.soulsWeapons
            .filter { it.category == SoulsWeaponCategory.Katana }
            .map { it.name }
        val result = SoulsTestData.soulsWeapons.toPushStream().mapNotNull { weapon ->
            weapon.name.takeIf { weapon.category == SoulsWeaponCategory.Katana }
        }.toList()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun mapNotNull_allNull_returnsEmpty_test() {
        val result = SoulsTestData.soulsSpells.toPushStream().mapNotNull { _ ->
            null as String?
        }.toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun mapNotNull_emptyStream_test() {
        val result = PushStream.of<SoulsWeapon>().mapNotNull { weapon -> weapon.name }.toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun mapNotNull_isLazy_test() {
        var transformed = 0
        val target = SoulsTestData.zweihander
        val result = SoulsTestData.soulsWeapons.toPushStream()
            .mapNotNull { weapon ->
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
        var transformed = 0
        val result = SoulsTestData.soulsWeapons.toPushStream()
            .mapNotNull { weapon ->
                transformed++
                weapon.name.takeIf { weapon.category == SoulsWeaponCategory.Katana }
            }
            .firstOrNull()
        Assert.assertEquals(SoulsTestData.soulsWeapons.first { it.category == SoulsWeaponCategory.Katana }.name, result)
        Assert.assertEquals(3, transformed)
    }

    @Test
    fun mapNotNull_afterMap_test() {
        val expected = SoulsTestData.soulsSpells
            .filter { it.category == SoulsSpellCategory.Sorcery }
            .map { it.name }
        val result = SoulsTestData.soulsSpells.toPushStream()
            .map { spell -> spell.category to spell.name }
            .mapNotNull { (category, name) -> name.takeIf { category == SoulsSpellCategory.Sorcery } }
            .toList()
        Assert.assertEquals(expected, result)
    }

    // endregion

    // region filter

    @Test
    fun filter_keepMatchedInOrder_test() {
        val expected = SoulsTestData.soulsWeapons.filter { it.game == SoulsGame.DarkSouls2 }
        val result = SoulsTestData.soulsWeapons.toPushStream().filter { it.game == SoulsGame.DarkSouls2 }.toList()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun filter_isLazy_test() {
        val weapons = SoulsTestData.soulsWeapons
        val firstKatanaIndex = weapons.indexOfFirst { it.category == SoulsWeaponCategory.Katana }
        var visited = 0
        val result = weapons.toPushStream().filter { visited++; it.category == SoulsWeaponCategory.Katana }.take(1).toList()
        Assert.assertEquals(listOf(weapons[firstKatanaIndex]), result)
        Assert.assertEquals(firstKatanaIndex + 1, visited)
    }

    @Test
    fun filter_noMatch_returnsEmpty_test() {
        val result = SoulsTestData.soulsSpells.toPushStream().filter { it.name == "Wrath of the Gods" }.toList() // 诸神之怒（不存在的数据）
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun filter_emptyStream_test() {
        Assert.assertTrue(PushStream.of<SoulsSpell>().filter { it.category == SoulsSpellCategory.Hex }.toList().isEmpty())
    }

    // endregion

    // region filterNotNull

    @Test
    fun filterNotNull_keepsNonNullInOrder_test() {
        val result = listOf(2, null, 1, null, 3).toPushStream().filterNotNull().toList()
        Assert.assertEquals(listOf(2, 1, 3), result)
    }

    @Test
    fun filterNotNull_allNull_returnsEmpty_test() {
        val result = listOf<Int?>(null, null, null).toPushStream().filterNotNull().toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun filterNotNull_emptyStream_test() {
        val result = PushStream.of<Int?>().filterNotNull().toList()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun filterNotNull_isLazy_test() {
        var visited = 0
        val source = PushStream.from {
            for (e in listOf(null, 2, 3, null)) {
                visited++
                if (!invoke(e)) break
            }
        }
        val result = source.filterNotNull().take(1).toList()
        Assert.assertEquals(listOf(2), result)
        Assert.assertEquals(2, visited)
    }

    @Test
    fun filterNotNull_stopsWhenDownstreamStops_test() {
        var visited = 0
        val source = PushStream.from {
            for (e in listOf(null, null, 1, 2)) {
                visited++
                if (!invoke(e)) break
            }
        }
        Assert.assertEquals(1, source.filterNotNull().firstOrNull())
        Assert.assertEquals(3, visited)
    }

    @Test
    fun filterNotNull_afterMap_test() {
        val expected = SoulsTestData.soulsSpells
            .filter { it.category == SoulsSpellCategory.Miracle }
            .map { it.name }
        val result = SoulsTestData.soulsSpells.toPushStream()
            .map { spell -> spell.name.takeIf { spell.category == SoulsSpellCategory.Miracle } }
            .filterNotNull()
            .toList()
        Assert.assertEquals(expected, result)
    }

    // endregion

    // region take

    @Test
    fun take_lessThanSize_test() {
        Assert.assertEquals(SoulsTestData.soulsWeapons.take(3), SoulsTestData.soulsWeapons.toPushStream().take(3).toList())
    }

    @Test
    fun take_greaterThanSize_test() {
        Assert.assertEquals(SoulsTestData.soulsWeapons, SoulsTestData.soulsWeapons.toPushStream().take(100).toList())
    }

    @Test
    fun take_zeroOrNegative_returnsEmptyWithoutProducing_test() {
        var produced = 0
        val stream = PushStream.from {
            for (boss in SoulsTestData.soulsBosses) {
                produced++
                if (!invoke(boss)) break
            }
        }
        Assert.assertTrue(stream.take(0).toList().isEmpty())
        Assert.assertTrue(stream.take(-1).toList().isEmpty())
        Assert.assertEquals(0, produced)
    }

    @Test
    fun take_infiniteStream_test() {
        var produced = 0
        val stream = PushStream.from {
            var i = 0
            while (true) {
                produced++
                if (!invoke(i)) break
                i++
            }
        }
        Assert.assertEquals(listOf(0, 1, 2, 3, 4), stream.take(5).toList())
        Assert.assertEquals(5, produced)
    }

    @Test
    fun take_stopsWhenDownstreamStops_test() {
        var produced = 0
        val stream = PushStream.from {
            var i = 0
            while (true) {
                produced++
                if (!invoke(i)) break
                i++
            }
        }
        Assert.assertEquals(2, stream.take(10).firstOrNull { it >= 2 })
        Assert.assertEquals(3, produced)
    }

    @Test
    fun take_afterFilter_test() {
        val expected = SoulsTestData.soulsSpells.filter { it.game == SoulsGame.DarkSouls3 }.take(2)
        val result = SoulsTestData.soulsSpells.toPushStream().filter { it.game == SoulsGame.DarkSouls3 }.take(2).toList()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun take_chained_test() {
        Assert.assertEquals(
            SoulsTestData.soulsBosses.take(2),
            SoulsTestData.soulsBosses.toPushStream().take(5).take(2).toList(),
        )
    }

    // endregion

    // region conversion

    @Test
    fun toOne_returnsFirstElement_test() {
        Assert.assertEquals(SoulsTestData.soulsBosses.first(), SoulsTestData.soulsBosses.toPushStream().toOne())
        Assert.assertNull(PushStream.of<SoulsCharacter>().toOne())
    }

    @Test
    fun toList_keepsDuplicatesAndOrder_test() {
        val expected = listOf(SoulsTestData.asylumDemon, SoulsTestData.asylumDemon, SoulsTestData.gwyn)
        Assert.assertEquals(expected, expected.toPushStream().toList())
    }

    @Test
    fun toSet_removesDuplicates_test() {
        val stream = listOf(SoulsTestData.asylumDemon, SoulsTestData.gwyn, SoulsTestData.asylumDemon).toPushStream()
        Assert.assertEquals(listOf(SoulsTestData.asylumDemon, SoulsTestData.gwyn), stream.toSet().toList())
    }

    @Test
    fun toList_emptyStream_test() {
        Assert.assertTrue(PushStream.of<SoulsCharacter>().toList().isEmpty())
        Assert.assertTrue(PushStream.of<SoulsCharacter>().toSet().isEmpty())
    }

    @Test
    fun repeatedTermination_producesSameResult_test() {
        val stream = SoulsTestData.soulsSpells.toPushStream()
        Assert.assertEquals(stream.toList(), stream.toList())
        Assert.assertEquals(stream.firstOrNull(), stream.firstOrNull())
    }

    // endregion

    // region tree

    @Test
    fun descendants_preorderTraversal_test() {
        val descendants = SoulsTestData.lordran.soulsDescendants().toList()
        Assert.assertEquals(SoulsTestData.lordran, descendants[0])
        Assert.assertEquals(SoulsTestData.firelinkShrine, descendants[1])
        Assert.assertEquals(SoulsTestData.solaire, descendants[2])
        Assert.assertEquals(SoulsTestData.siegmeyer, descendants[3])
        Assert.assertEquals(SoulsTestData.longsword, descendants[4])
        Assert.assertEquals(SoulsTestData.undeadBurg, descendants[5])
        Assert.assertEquals(SoulsTestData.zweihander, descendants.last())
        // 该地区的树中每个节点只出现一次
        Assert.assertEquals(descendants.size, descendants.distinct().size)
    }

    @Test
    fun descendants_takeStopsTraversal_test() {
        val result = SoulsTestData.lordran.soulsDescendants().take(3).toList()
        Assert.assertEquals(
            listOf(SoulsTestData.lordran, SoulsTestData.firelinkShrine, SoulsTestData.solaire),
            result,
        )
    }

    @Test
    fun descendants_filterByType_test() {
        val result = SoulsTestData.lordran.soulsDescendants().filter { it is SoulsWeapon }.toList()
        Assert.assertEquals(
            listOf(
                SoulsTestData.longsword,
                SoulsTestData.uchigatana,
                SoulsTestData.moonlightGreatsword,
                SoulsTestData.zweihander,
            ),
            result,
        )
    }

    @Test
    fun descendants_firstOrNull_test() {
        val result = SoulsTestData.lordran.soulsDescendants().firstOrNull { it is SoulsSpell }
        Assert.assertEquals(SoulsTestData.soulArrow, result)
    }

    @Test
    fun descendants_any_test() {
        Assert.assertTrue(SoulsTestData.drangleic.soulsDescendants().any { it == SoulsTestData.fumeUltraGreatsword })
        Assert.assertFalse(SoulsTestData.drangleic.soulsDescendants().any { it == SoulsTestData.zweihander })
    }

    // endregion

    // region chaining

    @Test
    fun chainedOperations_test() {
        val expected = SoulsTestData.soulsBosses
            .filter { it.game == SoulsGame.DarkSouls3 }
            .map { it.name }
            .take(2)
        val result = SoulsTestData.soulsBosses.toPushStream()
            .filter { it.game == SoulsGame.DarkSouls3 }
            .map { it.name }
            .take(2)
            .toList()
        Assert.assertEquals(expected, result)
    }

    // endregion
}
