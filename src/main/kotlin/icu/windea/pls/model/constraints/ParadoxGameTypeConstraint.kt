package icu.windea.pls.model.constraints

import icu.windea.pls.core.optimized
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameType.*

enum class ParadoxGameTypeConstraint {
    General {
        override fun test(gameType: ParadoxGameType?) = gameType == null || gameType == Core
        override fun list(): List<ParadoxGameType> = listOf(Core)
    },
    Specific {
        override fun test(gameType: ParadoxGameType?) = gameType != null && gameType != Core
        override fun list(): List<ParadoxGameType> = ParadoxGameType.getAllSpecific()
    },
    JominiBased {
        private val list = listOf(Ck3, Vic3, Eu5)
        override fun test(gameType: ParadoxGameType?) = gameType in list
        override fun list() = list
    },
    MetadataJsonUsed {
        private val list = listOf(Vic3, Eu5)
        override fun test(gameType: ParadoxGameType?) = gameType in list
        override fun list() = list
    },
    DescriptorMoUsed {
        private val list = Specific.list().filterNot { it in MetadataJsonUsed.list() }.optimized()
        override fun test(gameType: ParadoxGameType?) = gameType in list
        override fun list() = list
    },
    ;

    abstract fun test(gameType: ParadoxGameType?): Boolean

    abstract fun list(): List<ParadoxGameType>
}
