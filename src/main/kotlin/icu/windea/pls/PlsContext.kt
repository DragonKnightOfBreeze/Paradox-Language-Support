package icu.windea.pls

import icu.windea.pls.core.*

object PlsContext {
    /**
     * 用于标记当前线程是否正在编制索引。（为脚本文件或者本地化文件编制基于文件的索引）
     * @see icu.windea.pls.core.index.ParadoxFileBasedIndex
     */
    val indexStatusThreadLocal = ThreadLocal<Boolean>()
    
    /**
     * 用于在编制索引时获取全局缓存的键的前缀。（基于所在游戏或模组根目录的路径）
     * @see icu.windea.pls.core.index.ParadoxFileBasedIndex
     */
    val globalCacheKeyPrefixThreadLocal = ThreadLocal<String>()
    
    fun isIndexing() = indexStatusThreadLocal.get() == true
    
    fun getGlobalCacheKeyPrefix(context: Any?): String? {
        val cached = globalCacheKeyPrefixThreadLocal.get()
        if(cached != null) return cached
        return buildGlobalCacheKeyPrefix(context)
    }
    
    fun buildGlobalCacheKeyPrefix(context: Any?): String? {
        val rootFile = selectRootFile(context) ?: return null
        val rootPath = rootFile.rootInfo?.rootPath?.toString() ?: return null
        return "$rootPath:"
    }
}