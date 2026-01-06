package icu.windea.pls.lang.analysis

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.CollectionFactory
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.LazyValue
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.ParadoxFileType
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path
import kotlin.reflect.KProperty

@Suppress("unused", "ktPropBy")
@Service
class ParadoxAnalysisDataService : Disposable {
    object Keys : KeyRegistry()

    private val trackedFiles = CollectionFactory.createConcurrentWeakIdentityMap<VirtualFile, Any>()

    // 全局状态（注意：不能使用 `ThreadLocal`）

    /** 是否直接根据文件扩展名决定是否需要将文件类型重载为对应的文件类型（[ParadoxFileType]）。可用于集成测试。 */
    @Volatile var useDefaultFileExtensions: Boolean = false
    /** 是否从文件名推断游戏类型（[ParadoxGameType]）。可用于集成测试。 */
    @Volatile var useGameTypeInference: Boolean = false
    /** 接下来需要注入的根信息。可用于集成测试。 */
    @Volatile var markedRootInfo: ParadoxRootInfo? = null
    /** 接下来需要注入的文件信息。需要匹配可能的规则分组（[ParadoxFileGroup]）。可用于集成测试。 */
    @Volatile var markedFileInfo: ParadoxFileInfo? = null
    /** 接下来需要注入的游戏或模组的根目录。可用于集成测试。 */
    @Volatile var markedRootDirectory: Path? = null
    /** 接下来需要注入的规则目录，需要在加载规则数据前，预先手动指定。可用于集成测试。 */
    @Volatile var markedConfigDirectory: Path? = null

    // 直接保存到文件级别的用户数据（注意：尝试获取时不会立即初始化）

    /** 用于在根目录级别保存根信息（[ParadoxRootInfo]）。 */
    var VirtualFile.cachedRootInfo: LazyValue<ParadoxRootInfo>? by registerKey(Keys)
    /** 用于在文件级别保存文件信息（[ParadoxFileInfo]）。 */
    var VirtualFile.cachedFileInfo: LazyValue<ParadoxFileInfo>? by registerKey(Keys)
    /** 用于在文件级别保存语言环境规则（[CwtLocaleConfig]）。 */
    var VirtualFile.cachedLocaleConfig: LazyValue<CwtLocaleConfig>? by registerKey(Keys)

    /** 用于为文件注入根信息（[ParadoxRootInfo]）。 */
    var VirtualFile.injectedRootInfo: ParadoxRootInfo? by registerKey(Keys)
    /** 用于为文件注入文件信息（[ParadoxFileInfo]）。 */
    var VirtualFile.injectedFileInfo: ParadoxFileInfo? by registerKey(Keys)
    /** 用于为文件注入语言环境规则（[CwtLocaleConfig]）。 */
    var VirtualFile.injectedLocaleConfig: CwtLocaleConfig? by registerKey(Keys)
    /** 用于为脚本文件注入一组顶级键，解析时会加上作为前缀。 */
    var VirtualFile.injectedRootKeys: List<String>? by registerKey(Keys)

    /** 用于切分图片文件。 */
    var VirtualFile.sliceInfos: MutableSet<String>? by registerKey(Keys)

    private operator fun <T> Key<T>.setValue(thisRef: VirtualFile, property: KProperty<*>, value: T?) {
        if (PlsFileManager.isStubFile(thisRef)) return // skip for `StubVirtualFile` (unsupported)
        thisRef.putUserData(this, value)
        trackedFiles.put(thisRef, EMPTY_OBJECT) // auto track file when write user data
    }

    override fun dispose() {
        // 避免内存泄露
        markedRootInfo = null
        markedFileInfo = null
        for (file in trackedFiles.keys) {
            file.cachedRootInfo = null
            file.cachedFileInfo = null
            file.cachedLocaleConfig = null
            file.injectedRootInfo = null
            file.injectedFileInfo = null
            file.injectedLocaleConfig = null
            file.injectedRootKeys = null
            file.sliceInfos = null
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): ParadoxAnalysisDataService = service()
    }
}
