package icu.windea.pls.model

import icu.windea.pls.core.*
import icu.windea.pls.lang.*

/**
 * @property id ID。
 * @property title 标题。
 * @property steamId 对应的STEAM ID。
 * @property entryPaths 作为入口的根目录相对于游戏根目录的路径。匹配CWT规则时，除了游戏根目录之外，也需要基于这些目录。
 */
enum class ParadoxGameType(
    val id: String,
    val title: String,
    val steamId: String,
) {
    Stellaris("stellaris", "Stellaris", "281990") {
        override val entryPaths = setOf("pdx_launcher/game", "pdx_launcher/common", "pdx_online_assets", "previewer_assets")
    },
    Ck2("ck2", "Crusader Kings II", "203770") {
        override val entryPaths = setOf("jomini")
    },
    Ck3("ck3", "Crusader Kings III", "1158310") {
        override val entryPaths = setOf("jomini")
    },
    Eu4("eu4", "Europa Universalis IV", "236850") {
        override val entryPaths = setOf("tweakergui_assets")
    },
    Hoi4("hoi4", "Hearts of Iron IV", "394360") {
        override val entryPaths = setOf("jomini")
    },
    Ir("ir", "Imperator: Rome", "859580") {
        override val entryPaths = setOf("jomini")
    },
    Vic2("vic2", "Victoria II", "42960") {
        override val entryPaths = setOf("jomini")
    },
    Vic3("vic3", "Victoria III", "529340") {
        override val entryPaths = setOf("jomini")
    },
    ;
    
    open val entryPaths: Set<String> = emptySet()
    
    override fun toString(): String {
        return title
    }
    
    companion object {
        private val valueMapById = entries.associateBy { it.id }
        private val valueMapByTitle = entries.associateBy { it.title }
        private val valueMapBySteamId = entries.associateBy { it.steamId }
        
        @JvmStatic
        fun resolveById(id: String) = valueMapById[id]
        
        @JvmStatic
        fun resolveByTitle(title: String) = valueMapByTitle[title]
        
        @JvmStatic
        fun resolveBySteamId(steamId: String) = valueMapBySteamId[steamId]
        
        @JvmStatic
        fun canResolve(id: String) = id == "core" || valueMapById.containsKey(id)
        
        @JvmStatic
        fun placeholder() = Stellaris
    }
}

val ParadoxGameType?.id get() = this?.id ?: "core"

val ParadoxGameType?.title get() = this?.title ?: "Core"

val ParadoxGameType?.prefix get() = if(this == null) "" else "${id}:"

fun ParadoxGameType?.orDefault() = this ?: getSettings().defaultGameType
