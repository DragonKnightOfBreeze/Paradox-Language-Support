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
 * @property mainEntry 主要的入口名称。参见 [ParadoxGameType.EntryNames]。
 * @property entryNames 额外的入口名称。参见 [ParadoxGameType.EntryNames]。
 */
enum class ParadoxGameType(
    val id: String,
    val title: String,
    val gameId: String,
    val steamId: String,
    val mainEntry: String,
    val entryNames: Set<String>
) {
    /** 通用游戏类型。用于对应共享的规则分组。 */
    Core("core", "Core", "", "", "", emptySet()),
    /** [Stellaris](https://store.steampowered.com/app/281990) */
    Stellaris("stellaris", "Stellaris", "stellaris", "281990", "", EntryNames.stellaris),
    /** [Crusader Kings II](https://store.steampowered.com/app/203770) */
    Ck2("ck2", "Crusader Kings II", "ck2", "203770", "game", EntryNames.other),
    /** [Crusader Kings III](https://store.steampowered.com/app/1158310) */
    Ck3("ck3", "Crusader Kings III", "ck3", "1158310", "game", EntryNames.other),
    /** [Europa Universalis IV](https://store.steampowered.com/app/236850) */
    Eu4("eu4", "Europa Universalis IV", "eu4", "236850", "", EntryNames.other),
    /** [Europa Universalis V](https://store.steampowered.com/app/3450310) */
    Eu5("eu5", "Europa Universalis V", "eu5", "3450310", "", EntryNames.other),
    /** [Hearts of Iron IV](https://store.steampowered.com/app/394360) */
    Hoi4("hoi4", "Hearts of Iron IV", "hoi4", "394360", "", EntryNames.other),
    /** [Imperator: Rome](https://store.steampowered.com/app/859580) */
    Ir("ir", "Imperator: Rome", "imperator_rome", "859580", "game", EntryNames.other),
    /** [Victoria 2](https://store.steampowered.com/app/42960) */
    Vic2("vic2", "Victoria 2", "victoria2", "42960", "game", EntryNames.other),
    /** [Victoria 3](https://store.steampowered.com/app/529340) */
    Vic3("vic3", "Victoria 3", "victoria3", "529340", "game", EntryNames.other),
    ;

    /**
     * 用于获取入口名称。
     *
     * 说明：
     * - 入口名称即入口目录相对于游戏或模组根目录的路径。
     * - 游戏与模组文件实际上需要位于入口目录中，而非游戏或模组根目录中。
     * - 游戏与模组文件的（相对）路径，一般指相对于入口目录的路径。
     * - 对于游戏来说，主要的入口名称为空字符串或 `"game"`，此外也有一些额外的入口名称（例 `"jomini"`），取决于游戏类型。
     * - 对于模组来说，入口名称为空字符串，即入口目录等同于模组根目录。
     */
    object EntryNames {
        val stellaris = setOf("pdx_launcher/game", "pdx_launcher/common", "pdx_online_assets", "previewer_assets", "tweakergui_assets")
        val other = setOf("jomini")
    }

    companion object {
        private val values = entries.toList()
        private val valuesWithoutCore = values - Core
        private val valueMap = entries.associateBy { it.id }

        /**
         * 得到游戏类型。指定 [withCore] 为 `true` 以包含通用游戏类型。
         */
        @JvmStatic
        fun get(id: String, withCore: Boolean = false): ParadoxGameType? {
            return valueMap[id]?.takeIf { withCore || it != Core }
        }

        /**
         * 得到（PLS 目前支持的）所有游戏类型。指定 [withCore] 为 `true` 以包含通用游戏类型。
         */
        @JvmStatic
        fun getAll(withCore: Boolean = false): List<ParadoxGameType> {
            return if (withCore) values else valuesWithoutCore
        }
    }
}
