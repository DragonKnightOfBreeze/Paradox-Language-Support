package icu.windea.pls

object PlsStates {
    /**
     * 用于标记当前线程是否正在编制索引。（更具体点，是否正在为脚本文件或者本地化文件编制基于文件的索引）
     * @see icu.windea.pls.lang.index.ParadoxFileBasedIndex
     */
    val indexing = ThreadLocal<Boolean>()
    
    /**
     * 用于标记是否是动态的上下文规则。（例如需要基于脚本上下文）
     */
    val dynamicContextConfigs = ThreadLocal<Boolean>()
    
    /**
     * 用于标记是否允许不完整的复杂脚本表达式。（用于兼容代码补全）
     */
    val incompleteComplexExpression = ThreadLocal<Boolean>()
}
