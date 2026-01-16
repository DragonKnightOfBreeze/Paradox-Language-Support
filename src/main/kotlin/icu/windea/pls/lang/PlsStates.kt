package icu.windea.pls.lang

object PlsStates {
    /**
     * 用于标记是否正在构建合并索引。
     *
     * @see icu.windea.pls.lang.index.ParadoxMergedIndex
     */
    val processMergedIndex = ThreadLocal<Boolean>()

    /**
     * 用于标记是否正在构建合并索引，并且正在解析引用。
     *
     * @see icu.windea.pls.lang.index.ParadoxMergedIndex
     */
    val resolveForMergedIndex = ThreadLocal<Boolean>()

    /**
     * 用于标记是否正在解析内部规则。
     *
     * @see icu.windea.pls.ep.config.configGroup.CwtFileBasedConfigGroupDataProvider
     */
    val resolveForInternalConfigs = ThreadLocal<Boolean>()

    /**
     * 用于标记是否是动态的上下文规则（例如需要基于脚本上下文）。
     *
     * @see icu.windea.pls.lang.resolve.CwtConfigContext
     */
    val dynamicContextConfigs = ThreadLocal<Boolean>()

    /**
     * 用于标记是否允许不完整的复杂脚本表达式（用于兼容代码补全）。
     *
     * @see icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
     */
    val incompleteComplexExpression = ThreadLocal<Boolean>()
}
