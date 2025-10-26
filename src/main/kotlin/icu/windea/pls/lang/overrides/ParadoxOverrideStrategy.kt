package icu.windea.pls.lang.overrides

/**
 * 覆盖方式。
 *
 * 说明：
 * - 用于进行查询排序，并提供相关的提示和代码检查。
 * - 全局封装变量的覆盖方式为 `FIOS`。
 * - 定义默认使用的覆盖方式为 `LIOS`。
 * - 本地化的覆盖方式为 `LIOS`。`replace` 目录下的拥有更高的优先级（尚不兼容这种情况）。
 *
 * 参见：[优先级规则](https://windea.icu/Paradox-Language-Support/ref-config-format.html#config-priority)
 *
 * @see ParadoxOverrideService
 */
enum class ParadoxOverrideStrategy(val id: String) {
    /** 只读一次（First In, Only Served）。先加载者生效，后加载者会被直接忽略。 */
    FIOS("FIOS"),
    /** 后读覆盖（Last In, Only Served）。后加载者覆盖先加载者。 */
    LIOS("LIOS"),
    /** 整文件覆盖（Duplicates）。必须用同路径文件进行整体覆盖。 */
    DUPL("DUPL"),
    /** 顺序读取（Ordered）。不能覆盖既有条目，后加载者会被按序新增或合并。 */
    ORDERED("ORDERED"),
    ;

    override fun toString() = id

    companion object {
        @JvmStatic
        private val map = entries.associateBy { it.id }

        @JvmStatic
        fun get(id: String): ParadoxOverrideStrategy? = map[id]
    }
}
