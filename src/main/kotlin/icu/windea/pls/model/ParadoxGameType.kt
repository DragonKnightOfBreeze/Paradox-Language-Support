package icu.windea.pls.model

import icu.windea.pls.core.optimized
import icu.windea.pls.ep.analysis.ParadoxInferredGameTypeProvider
import icu.windea.pls.lang.analysis.ParadoxGameTypeManager
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint

/**
 * 游戏类型。
 *
 * @property id ID（代码、文档和显示文本中使用游戏类型 ID）。
 * @property title 标题（代码、文档和显示文本中使用的游戏名）。
 * @property gameId 游戏 ID（官方启动器使用的游戏 ID）。
 * @property steamId Steam ID（Steam 使用的游戏 ID）。
 * @property metadata 额外的元数据。
 *
 * @see ParadoxGameTypeInfo
 * @see ParadoxGameTypeMetadata
 * @see ParadoxGameTypeConstraint
 * @see ParadoxGameTypeManager
 * @see ParadoxInferredGameTypeProvider
 */
enum class ParadoxGameType(
    val id: String,
    val title: String,
    val gameId: String,
    val steamId: String,
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

    val metadata: ParadoxGameTypeMetadata get() = ParadoxGameTypeManager.getMetadata(this)

    companion object {
        private val values = entries.toList().optimized()
        private val valuesSpecific = entries.filter { it != Core }.optimized()
        private val map = values.associateBy { it.id }.optimized()
        private val mapSpecific = valuesSpecific.associateBy { it.id }.optimized()

        @JvmStatic
        fun getAll(): List<ParadoxGameType> = values

        @JvmStatic
        fun getAllSpecific(): List<ParadoxGameType> = valuesSpecific

        @JvmStatic
        fun get(id: String): ParadoxGameType? = map[id]

        @JvmStatic
        fun getSpecific(id: String): ParadoxGameType? = mapSpecific[id]

        @JvmStatic
        fun getDefault(): ParadoxGameType = ChronicleSettings.getInstance().state.defaultGameType
    }
}
