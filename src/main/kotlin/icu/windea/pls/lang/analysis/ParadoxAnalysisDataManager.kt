package icu.windea.pls.lang.analysis

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.CollectionFactory
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.ParadoxFileType
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

object ParadoxAnalysisDataManager {
    object Keys : KeyRegistry() {
        // 直接保存到文件级别的用户数据（注意：尝试获取时不会立即初始化）

        /** 用于在根目录级别保存根信息（[ParadoxRootInfo]）。 */
        val cachedRootInfo by registerKey<LazyValue<ParadoxRootInfo>>(this)
        /** 用于在文件级别保存文件信息（[ParadoxFileInfo]）。 */
        val cachedFileInfo by registerKey<LazyValue<ParadoxFileInfo>>(this)
        /** 用于在文件级别保存语言环境规则（[CwtLocaleConfig]）。 */
        val cachedLocaleConfig by registerKey<LazyValue<CwtLocaleConfig>>(this)

        /** 用于为文件注入根信息（[ParadoxRootInfo]）。 */
        val injectedRootInfo by registerKey<ParadoxRootInfo>(this)
        /** 用于为文件注入文件信息（[ParadoxFileInfo]）。 */
        val injectedFileInfo by registerKey<ParadoxFileInfo>(this)
        /** 用于为文件注入语言环境规则（[CwtLocaleConfig]）。 */
        val injectedLocaleConfig by registerKey<CwtLocaleConfig>(this)
        /** 用于为脚本文件注入一组顶级键，解析时会加上作为前缀。 */
        val injectedRootKeys by registerKey<List<String>>(this)

        /** 用于切分图片文件。 */
        val sliceInfos by registerKey<MutableSet<String>>(this)
    }

    val trackedFiles: MutableMap<VirtualFile, Unit> = CollectionFactory.createConcurrentWeakIdentityMap()

    /** 是否直接根据文件扩展名决定是否需要将文件类型重载为对应的文件类型（[ParadoxFileType]）。可用于集成测试。 */
    @Volatile var useDefaultFileExtensions: Boolean = false
    /** 是否从文件名推断游戏类型（[ParadoxGameType]）。可用于集成测试。 */
    @Volatile var useGameTypeInference: Boolean = false

    /** 接下来需要注入的根信息。可用于集成测试。 */
    @Volatile var markedRootInfo: ParadoxRootInfo? = null
    /** 接下来需要注入的文件信息。需要匹配可能的规则分组（[ParadoxFileGroup]）。可用于集成测试。 */
    @Volatile var markedFileInfo: ParadoxFileInfo? = null
    /** 接下来需要注入的游戏或模组的根目录的路径（相对于上下文根目录），可用于集成测试。 */
    @Volatile var markedRootPath: String? = null
    /** 接下来需要注入的游戏或模组的根目录。可用于集成测试。 */
    @Volatile var markedRootDirectory: Path? = null
    /** 接下来需要注入的规则目录的路径（相对于上下文根目录）。需要在加载规则数据前预先手动指定。可用于集成测试。 */
    @Volatile var markedConfigPath: String? = null
    /** 接下来需要注入的规则目录。需要在加载规则数据前预先手动指定。可用于集成测试。 */
    @Volatile var markedConfigDirectory: Path? = null

    /** 初始化规则分组时，是否仅使用内置的和注入的规则文件。 */
    @Volatile var useOnlyBuiltInAndInjectedConfigFiles: Boolean = false
    /** 初始化规则分组时，是否仅使用注入的规则文件。 */
    @Volatile var useOnlyInjectedConfigFiles: Boolean = false

    // 3.0.1 optimize: inline only
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> getData(file: VirtualFile, key: Key<T>): T? {
        return file.getUserData(key)
    }

    // 3.0.1 optimize: inline only
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> setData(file: VirtualFile, key: Key<T>, value: T?) {
        // skip for `StubVirtualFile` (unsupported)
        if (VirtualFileService.isStubFile(file)) return
        // put data
        file.putUserData(key, value)
        // auto track files
        if (value != null) trackedFiles.put(file, Unit) else trackedFiles.remove(file)
    }
}
