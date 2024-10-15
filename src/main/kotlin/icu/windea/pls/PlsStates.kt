package icu.windea.pls

object PlsStates {
    /**
     * 用于标记当前线程是否正在编制索引。（更具体点，是否正在为脚本文件或者本地化文件编制基于文件的索引）
     * @see icu.windea.pls.lang.index.ParadoxFileBasedIndex
     */
    val indexing = ThreadLocal<Boolean>()

    /**
     * 用于标记规则是否需要被重载。（此时不能缓存上下文规则）
     */
    val overrideConfig = ThreadLocal<Boolean>()

    /**
     * 用于标记是否允许不完整的复杂脚本表达式。（用于兼容代码补全）
     */
    val incompleteComplexExpression = ThreadLocal<Boolean>()
}
