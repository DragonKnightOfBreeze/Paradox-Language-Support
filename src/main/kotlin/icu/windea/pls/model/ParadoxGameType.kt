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
 * @property mainEntries 主要的入口名称。参见 [ParadoxGameType.Entries]。
 * @property extraEntries 次要的入口名称。参见 [ParadoxGameType.Entries]。
 */
enum class ParadoxGameType(
    val id: String,
    val title: String,
    val gameId: String,
    val steamId: String,
    val mainEntries: Set<String> = emptySet(),
    val extraEntries: Set<String> = emptySet(),
) {
    /** 通用游戏类型。用于对应共享的规则分组。 */
    Core("core", "Core", "", ""),
    /** [Stellaris](https://store.steampowered.com/app/281990) */
    Stellaris("stellaris", "Stellaris", "stellaris", "281990", Entries.empty, Entries.stellarisExtra),
    /** [Crusader Kings II](https://store.steampowered.com/app/203770) */
    Ck2("ck2", "Crusader Kings II", "ck2", "203770", Entries.game, Entries.jomini),
    /** [Crusader Kings III](https://store.steampowered.com/app/1158310) */
    Ck3("ck3", "Crusader Kings III", "ck3", "1158310", Entries.game, Entries.jomini),
    /** [Europa Universalis IV](https://store.steampowered.com/app/236850) */
    Eu4("eu4", "Europa Universalis IV", "eu4", "236850", Entries.empty, Entries.jomini),
    /** [Europa Universalis V](https://store.steampowered.com/app/3450310) */
    Eu5("eu5", "Europa Universalis V", "eu5", "3450310", Entries.eu5, Entries.jomini),
    /** [Hearts of Iron IV](https://store.steampowered.com/app/394360) */
    Hoi4("hoi4", "Hearts of Iron IV", "hoi4", "394360", Entries.empty, Entries.jomini),
    /** [Imperator: Rome](https://store.steampowered.com/app/859580) */
    Ir("ir", "Imperator Rome", "imperator_rome", "859580", Entries.game, Entries.jomini),
    /** [Victoria 2](https://store.steampowered.com/app/42960) */
    Vic2("vic2", "Victoria 2", "victoria2", "42960", Entries.game, Entries.jomini),
    /** [Victoria 3](https://store.steampowered.com/app/529340) */
    Vic3("vic3", "Victoria 3", "victoria3", "529340", Entries.game, Entries.jomini),
    ;

    val entryMap = (mainEntries + extraEntries).sortedDescending().associateWith { it.split('/') }.toMap()

    /**
     * 用于得到入口的名称。
     *
     * 说明：
     * - 入口的名称即入口目录相对于游戏或模组目录的路径。
     * - 入口分为主要入口和次要入口。主要入口也可能存在多个，其名称默认为空字符串。
     * - 游戏与模组文件实际上需要位于入口目录中，而非游戏或模组目录中。
     * - （PLS 认为）主要入口目录中的文件不能引用次要入口目录中的文件中的内容。
     * - 游戏与模组文件的（相对）路径，一般指相对于入口目录的路径。
     * - 对于游戏来说，入口名称取决于具体的游戏类型。主要入口的名称一般为空字符串（等同于游戏根目录）或 `game`（等同于游戏根目录下的 `game` 子目录）。
     * - 对于模组来说，入口名称为空字符串（等同于模组根目录）。
     */
    object Entries {
        val empty = emptySet<String>()
        val game = setOf("game")
        val jomini = setOf("jomini")
        val eu5 = setOf("game/in_game", "game/main_menu", "game/loading_screen")
        val stellarisExtra = setOf("pdx_launcher/game", "pdx_launcher/common", "pdx_online_assets", "previewer_assets", "tweakergui_assets")
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
