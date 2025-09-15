package icu.windea.pls.model

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
    val entryNames: Set<String>
) {
    Core("core", "Core", "", "", emptySet()),
    Stellaris("stellaris", "Stellaris", "stellaris", "281990", EntryNames.stellaris),
    Ck2("ck2", "Crusader Kings II", "ck2", "203770", EntryNames.other),
    Ck3("ck3", "Crusader Kings III", "ck3", "1158310", EntryNames.other),
    Eu4("eu4", "Europa Universalis IV", "eu4", "236850", EntryNames.other),
    Eu5("eu5", "Europa Universalis V", "eu5", "3450310", EntryNames.other),
    Hoi4("hoi4", "Hearts of Iron IV", "hoi4", "394360", EntryNames.other),
    Ir("ir", "Imperator: Rome", "ir", "859580", EntryNames.other),
    Vic2("vic2", "Victoria 2", "victoria2", "42960", EntryNames.other),
    Vic3("vic3", "Victoria 3", "victoria3", "529340", EntryNames.other),
    ;

    private object EntryNames {
        val stellaris = setOf("pdx_launcher/game", "pdx_launcher/common", "pdx_online_assets", "previewer_assets", "tweakergui_assets")
        val other = setOf("jomini")
    }

    companion object {
        private val values = entries.toList()
        private val valuesWithoutCore = values - Core
        private val valueMap = entries.associateBy { it.id }

        @JvmStatic
        fun get(id: String, withCore: Boolean = false): ParadoxGameType? {
            return valueMap[id]?.takeIf { withCore || it != Core }
        }

        @JvmStatic
        fun getAll(withCore: Boolean = false): List<ParadoxGameType> {
            return if (withCore) values else valuesWithoutCore
        }
    }
}
