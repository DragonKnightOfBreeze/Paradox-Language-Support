package icu.windea.pls.lang.index

object ParadoxMergedIndexThreadContext {
    /**
     * 用于标记是否正在构建合并索引。
     *
     * @see ParadoxMergedIndex
     */
    val isProcessing = ThreadLocal<Boolean>()

    /**
     * 用于标记是否正在构建合并索引，并且正在解析引用。
     *
     * @see ParadoxMergedIndex
     */
    val isResolving = ThreadLocal<Boolean>()
}
