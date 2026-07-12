package icu.windea.pls.model.constraints

import icu.windea.pls.core.optimized
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameType.*

/**
 * 游戏约束。
 *
 * 用于测试指定的 [ParadoxGameType] 是否符合特定的条件，以及列出符合这类条件的所有 [ParadoxGameType]。
 *
 * 用法示例：`gameType matchesBy ParadoxGameTypeConstraint.JominiBased`
 *
 * @see ParadoxGameType
 */
@Suppress("unused")
enum class ParadoxGameTypeConstraint {
    Specific {
        override fun test(gameType: ParadoxGameType) = gameType != Core
        override fun list(): List<ParadoxGameType> = ParadoxGameType.getAllSpecific()
    },
    JominiBased {
        private val list = listOf(Ck3, Vic3, Eu5)
        override fun test(gameType: ParadoxGameType) = gameType in list
        override fun list() = list
    },
    MetadataJsonUsed {
        private val list = listOf(Vic3, Eu5)
        override fun test(gameType: ParadoxGameType) = gameType in list
        override fun list() = list
    },
    DescriptorModUsed {
        private val list = Specific.list().filterNot { it in MetadataJsonUsed.list() }.optimized()
        override fun test(gameType: ParadoxGameType) = gameType in list
        override fun list() = list
    },
    ;

    abstract fun test(gameType: ParadoxGameType): Boolean

    abstract fun list(): List<ParadoxGameType>
}
