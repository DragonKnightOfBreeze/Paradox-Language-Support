package icu.windea.pls.ep.priority

/**
 * 覆盖顺序。
 */
enum class ParadoxPriority {
    /** 只读一次（选用最先读取到的那个） */
    FIOS,
    /** 后读覆盖（选用最后读取到的那个） */
    LIOS,
    /** 顺序读取 */
    ORDERED;

    companion object {
        private val valueMap = entries.associateBy { it.name }

        @JvmStatic
        fun resolve(name: String): ParadoxPriority? {
            return valueMap[name.uppercase()]
        }
    }
}
