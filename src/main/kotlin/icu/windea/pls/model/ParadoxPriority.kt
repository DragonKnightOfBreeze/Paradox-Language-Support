package icu.windea.pls.model

/**
 * 覆盖方式。
 */
enum class ParadoxPriority(val id: String) {
    /** 只读一次（选用最先读取到的那个） */
    FIOS("fios"),
    /** 后读覆盖（选用最后读取到的那个） */
    LIOS("lios"),
    /** 顺序读取 */
    ORDERED("ordered"),
    ;

    override fun toString() = id

    companion object {
        @JvmStatic
        private val map = entries.associateBy { it.id }

        @JvmStatic
        fun get(id: String): ParadoxPriority? = map[id]
    }
}
