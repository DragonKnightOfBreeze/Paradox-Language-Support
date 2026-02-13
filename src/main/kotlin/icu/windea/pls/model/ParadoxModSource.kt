package icu.windea.pls.model

/**
 * 模组的来源。
 */
enum class ParadoxModSource(val id: String) {
    Local("local"),
    Steam("steam"),
    Paradox("pdx"),
    ;

    override fun toString(): String = id
}
