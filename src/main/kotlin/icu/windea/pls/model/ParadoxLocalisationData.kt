package icu.windea.pls.model

/**
 * 用作输出的本地化数据。
 */
data class ParadoxLocalisationData(
    val key: String,
    val text: String,
) {
    companion object {
        @JvmField
        val EMPTY = ParadoxLocalisationData("", "")

        @JvmStatic
        fun fromLine(line: String): ParadoxLocalisationData {
            val line0 = line.trim()
            val colonIndex = line0.indexOf(':')
            if (colonIndex == -1) return EMPTY
            val key = line0.substring(0, colonIndex) //do not validate key here
            val lqIndex = line0.indexOf('"', colonIndex + 1)
            val text = line0.substring(lqIndex + 1).removeSuffix("\"")
            return ParadoxLocalisationData(key, text)
        }
    }
}
