package icu.windea.pls

import icu.windea.pls.core.*

object PlsContext {
    /**
     * 用于标记当前线程是否正在编制索引。（为脚本文件或者本地化文件编制基于文件的索引）
     * @see icu.windea.pls.core.index.ParadoxFileBasedIndex
     */
    val indexStatus = ThreadLocal<Boolean>()
    
    /**
     * 用于标记规则是否需要被重载。（因此不能缓存上下文规则）
     */
    val overrideConfigStatus = ThreadLocal<Boolean>()
    
    /**
     * 用于在编制索引时获取全局缓存的键的前缀。（基于所在游戏或模组根目录的路径）
     * @see icu.windea.pls.core.index.ParadoxFileBasedIndex
     */
    val globalCacheKeyPrefix = ThreadLocal<String>()
    
    fun getGlobalCacheKeyPrefix(context: Any?): String? {
        val cached = globalCacheKeyPrefix.get()
        if(cached != null) return cached
        return buildGlobalCacheKeyPrefix(context)
    }
    
    fun buildGlobalCacheKeyPrefix(context: Any?): String? {
        val rootFile = selectRootFile(context) ?: return null
        val rootPath = rootFile.rootInfo?.rootPath?.toString() ?: return null
        return "$rootPath:"
    }
}