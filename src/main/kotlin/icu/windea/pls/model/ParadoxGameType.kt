package icu.windea.pls.model

import icu.windea.pls.*

/**
 * @property id ID。
 * @property title 标题。
 * @property gameId `launcher-settings.json`中使用的游戏ID。
 * @property steamId Steam使用的游戏ID。
 * @property entryNames 额外的入口名称。即入口目录相对于游戏或模组目录的路径。
 */
enum class ParadoxGameType(
    val id: String,
    val title: String,
    val gameId: String,
    val steamId: String,
    val entryNames: Set<String> = emptySet()
) {
    Stellaris("stellaris", "Stellaris", "stellaris", "281990", stellarisEntryNames),
    Ck2("ck2", "Crusader Kings II", "ck2", "203770", otherEntryNames),
    Ck3("ck3", "Crusader Kings III", "ck3", "1158310", otherEntryNames),
    Eu4("eu4", "Europa Universalis IV", "eu4", "236850", otherEntryNames),
    Hoi4("hoi4", "Hearts of Iron IV", "hoi4", "394360", otherEntryNames),
    Ir("ir", "Imperator: Rome", "ir", "859580", otherEntryNames),
    Vic2("vic2", "Victoria 2", "victoria2", "42960", otherEntryNames),
    Vic3("vic3", "Victoria 3", "victoria3", "529340", otherEntryNames),
    ;

    override fun toString() = title

    companion object {
        private val valueMap = entries.associateBy { it.id }

        @JvmStatic
        fun resolve(id: String) = valueMap[id]

        @JvmStatic
        fun canResolve(id: String) = id == "core" || valueMap.containsKey(id)

        @JvmStatic
        fun placeholder() = Stellaris
    }
}

private val stellarisEntryNames = setOf("pdx_launcher/game", "pdx_launcher/common", "pdx_online_assets", "previewer_assets", "tweakergui_assets")
private val otherEntryNames = setOf("jomini")

val ParadoxGameType?.id get() = this?.id ?: "core"

val ParadoxGameType?.title get() = this?.title ?: "Core"

val ParadoxGameType?.prefix get() = if (this == null) "" else "${id}:"

fun ParadoxGameType?.orDefault() = this ?: PlsFacade.getSettings().defaultGameType
