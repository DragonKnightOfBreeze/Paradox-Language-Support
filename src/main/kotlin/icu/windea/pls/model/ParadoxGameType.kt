package icu.windea.pls.model

/**
 * 游戏类型。
 *
 * 参见：[GameRegistration.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.Services/Registrations/GameRegistration.cs)
 *
 * @property id ID。
 * @property title 标题（通常作为游戏名）。
 * @property gameId 官方启动器使用的游戏 ID。
 * @property steamId Steam 使用的游戏 ID。
 * @property entryInfo 入口信息。
 *
 * @see ParadoxEntryInfo
 */
enum class ParadoxGameType(
    val id: String,
    val title: String,
    val gameId: String,
    val steamId: String,
    val entryInfo: ParadoxEntryInfo,
) {
    /** 通用游戏类型。用于对应共享的规则分组。 */
    Core("core", "Core", "", "", EntryInfos.empty),
    /** [Stellaris](https://store.steampowered.com/app/281990) */
    Stellaris("stellaris", "Stellaris", "stellaris", "281990", EntryInfos.stellaris),
    /** [Crusader Kings II](https://store.steampowered.com/app/203770) */
    Ck2("ck2", "Crusader Kings II", "ck2", "203770", EntryInfos.ofGameAndJomini),
    /** [Crusader Kings III](https://store.steampowered.com/app/1158310) */
    Ck3("ck3", "Crusader Kings III", "ck3", "1158310", EntryInfos.ofGameAndJomini),
    /** [Europa Universalis IV](https://store.steampowered.com/app/236850) */
    Eu4("eu4", "Europa Universalis IV", "eu4", "236850", EntryInfos.ofJomini),
    /** [Europa Universalis V](https://store.steampowered.com/app/3450310) */
    Eu5("eu5", "Europa Universalis V", "eu5", "3450310", EntryInfos.eu5),
    /** [Hearts of Iron IV](https://store.steampowered.com/app/394360) */
    Hoi4("hoi4", "Hearts of Iron IV", "hoi4", "394360", EntryInfos.ofJomini),
    /** [Imperator: Rome](https://store.steampowered.com/app/859580) */
    Ir("ir", "Imperator Rome", "imperator_rome", "859580", EntryInfos.ofGameAndJomini),
    /** [Victoria 2](https://store.steampowered.com/app/42960) */
    Vic2("vic2", "Victoria 2", "victoria2", "42960", EntryInfos.ofGameAndJomini),
    /** [Victoria 3](https://store.steampowered.com/app/529340) */
    Vic3("vic3", "Victoria 3", "victoria3", "529340", EntryInfos.ofGameAndJomini),
    ;

    private object Entries {
        val ofGame = setOf("game")
        val ofJomini = setOf("jomini")
        val eu5GameMain = setOf(
            "game/in_game", "game/main_menu", "game/loading_screen",
            "game/dlc/*/in_game", "game/dlc/*/main_menu", "game/dlc/*/loading_screen"
        )
        val eu5GameExtra = setOf(
            "clausewitz/main_menu", "clausewitz/loading_screen",
            "jomini/main_menu", "jomini/loading_screen",
        )
        val eu5ModMain = setOf("in_game", "main_menu", "loading_screen")
        val stellarisGameExtra = setOf("pdx_launcher/game", "pdx_launcher/common", "pdx_online_assets", "previewer_assets", "tweakergui_assets")
    }

    private object EntryInfos {
        val empty = ParadoxEntryInfo()
        val ofJomini = ParadoxEntryInfo(gameExtra = Entries.ofJomini)
        val ofGameAndJomini = ParadoxEntryInfo(gameMain = Entries.ofGame, gameExtra = Entries.ofJomini)
        val stellaris = ParadoxEntryInfo(gameExtra = Entries.stellarisGameExtra)
        val eu5 = ParadoxEntryInfo(gameMain = Entries.eu5GameMain, gameExtra = Entries.eu5GameExtra, modMain = Entries.eu5ModMain)
    }

    companion object {
        @JvmStatic
        private val values = entries.toList()
        @JvmStatic
        private val valuesNoCore = values - Core
        @JvmStatic
        private val map = entries.associateBy { it.id }

        /**
         * 得到 [id] 对应的游戏类型。
         *
         * @param withCore 是否包含通用游戏类型（[ParadoxGameType.Core]）。
         */
        @JvmStatic
        fun get(id: String, withCore: Boolean = false): ParadoxGameType? {
            return map[id]?.takeIf { withCore || it != Core }
        }

        /**
         * 得到（PLS 目前支持的）所有游戏类型。
         *
         * @param withCore 是否包含通用游戏类型（[ParadoxGameType.Core]）。
         */
        @JvmStatic
        fun getAll(withCore: Boolean = false): List<ParadoxGameType> {
            return if (withCore) values else valuesNoCore
        }
    }
}
