package icu.windea.pls.model

import icu.windea.pls.ep.analysis.ParadoxInferredGameTypeProvider
import icu.windea.pls.lang.analysis.ParadoxGameTypeManager
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import icu.windea.pls.model.ParadoxGameType

/**
 * 游戏类型。
 *
 * @property id ID（用作插件代码中的游戏类型 ID）。
 * @property title 标题（用作游戏的名字）。
 * @property gameId 官方启动器使用的游戏 ID。
 * @property steamId Steam 使用的游戏 ID。
 * @property metadata 额外的元数据。
 *
 * @see ParadoxGameTypeMetadata
 * @see ParadoxInferredGameTypeProvider
 */
enum class ParadoxGameType(
    val id: String,
    val title: String,
    val gameId: String,
    val steamId: String
) {
    /** 通用游戏类型。用于对应共享的规则分组。 */
    Core("core", "Core", "", ""),
    /** [Stellaris](https://store.steampowered.com/app/281990) */
    Stellaris("stellaris", "Stellaris", "stellaris", "281990"),
    /** [Crusader Kings II](https://store.steampowered.com/app/203770) */
    Ck2("ck2", "Crusader Kings II", "ck2", "203770"),
    /** [Crusader Kings III](https://store.steampowered.com/app/1158310) */
    Ck3("ck3", "Crusader Kings III", "ck3", "1158310"),
    /** [Europa Universalis IV](https://store.steampowered.com/app/236850) */
    Eu4("eu4", "Europa Universalis IV", "eu4", "236850"),
    /** [Europa Universalis V](https://store.steampowered.com/app/3450310) */
    Eu5("eu5", "Europa Universalis V", "eu5", "3450310"),
    /** [Hearts of Iron IV](https://store.steampowered.com/app/394360) */
    Hoi4("hoi4", "Hearts of Iron IV", "hoi4", "394360"),
    /** [Imperator: Rome](https://store.steampowered.com/app/859580) */
    Ir("ir", "Imperator Rome", "imperator_rome", "859580"),
    /** [Victoria 2](https://store.steampowered.com/app/42960) */
    Vic2("vic2", "Victoria 2", "victoria2", "42960"),
    /** [Victoria 3](https://store.steampowered.com/app/529340) */
    Vic3("vic3", "Victoria 3", "victoria3", "529340"),
    ;

    val metadata: ParadoxGameTypeMetadata = ParadoxGameTypeManager.getGameTypeMetadata(this)

    companion object {
        @JvmStatic
        private val map = entries.associateBy { it.id }
        @JvmStatic
        private val values = entries.toList()
        @JvmStatic
        private val valuesNoCore = values - Core

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
         * 得到（插件目前支持的）所有游戏类型。
         *
         * @param withCore 是否包含通用游戏类型（[ParadoxGameType.Core]）。
         */
        @JvmStatic
        fun getAll(withCore: Boolean = false): List<ParadoxGameType> {
            return if (withCore) values else valuesNoCore
        }

        /**
         * 得到默认的游戏类型。
         */
        @JvmStatic
        fun getDefault(): ParadoxGameType {
            return PlsSettings.getInstance().state.defaultGameType
        }
    }
}
