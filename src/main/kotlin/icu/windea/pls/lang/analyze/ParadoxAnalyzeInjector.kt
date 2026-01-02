package icu.windea.pls.lang.analyze

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.ThreadLocal
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.lang.ParadoxFileType
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath

@Suppress("unused")
object ParadoxAnalyzeInjector {
    /** 用于为文件注入根信息（[ParadoxRootInfo]）。 */
    private val PlsKeys.injectedRootInfo by createKey<ParadoxRootInfo>(PlsKeys)
    /** 用于为文件注入文件信息（[ParadoxFileInfo]）。 */
    private val PlsKeys.injectedFileInfo by createKey<ParadoxFileInfo>(PlsKeys)
    /** 用于为文件注入语言环境规则（[CwtLocaleConfig]）。 */
    private val PlsKeys.injectedLocaleConfig by createKey<CwtLocaleConfig>(PlsKeys)
    /** 用于为脚本文件注入一组顶级键，解析时会加上作为前缀。 */
    private val PlsKeys.injectedRootKeys by createKey<List<String>>(PlsKeys)

    /** 是否直接根据文件扩展名决定是否需要将文件类型重载为对应的文件类型（[ParadoxFileType]）。通常用于集成测试。 */
    private val useDefaultFileExtensions = ThreadLocal<Boolean>()
    /** 是否从文件名推断游戏类型（[ParadoxGameType]）。通常用于集成测试。 */
    private val useGameTypeInference = ThreadLocal<Boolean>()
    /** 接下来需要注入的根信息。通常用于集成测试。 */
    private val markedRootInfo = ThreadLocal<ParadoxRootInfo>()
    /** 接下来需要注入的文件信息。通常用于集成测试。 */
    private val markedFileInfo = ThreadLocal<ParadoxFileInfo>()

    // region Get Methods

    fun getInjectedRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        return rootFile.getUserData(PlsKeys.injectedRootInfo)
    }

    fun getInjectedFileInfo(file: VirtualFile): ParadoxFileInfo? {
        return file.getUserData(PlsKeys.injectedFileInfo)
    }

    fun getInjectedLocaleConfig(file: VirtualFile): CwtLocaleConfig? {
        return file.getUserData(PlsKeys.injectedLocaleConfig)
    }

    fun getInjectedRootKeys(file: VirtualFile): List<String> {
        return file.getUserData(PlsKeys.injectedRootKeys) ?: emptyList()
    }

    fun useDefaultFileExtensions(): Boolean {
        return useDefaultFileExtensions.get() == true
    }

    fun useGameTypeInference(): Boolean {
        return useGameTypeInference.get() == true
    }

    fun getMarkedRootInfo(): ParadoxRootInfo? {
        return markedRootInfo.get()
    }

    fun getMarkedFileInfo(): ParadoxFileInfo? {
        return markedFileInfo.get()
    }

    // endregion

    // region Manipulation Methods

    fun injectRootInfo(rootFile: VirtualFile, rootInfo: ParadoxRootInfo?): Boolean {
        if (PlsFileManager.isStubFile(rootFile)) return false // skip for `StubVirtualFile` (unsupported)
        rootFile.putUserData(PlsKeys.injectedRootInfo, rootInfo)
        return true
    }

    fun injectFileInfo(file: VirtualFile, fileInfo: ParadoxFileInfo?): Boolean {
        if (PlsFileManager.isStubFile(file)) return false // skip for `StubVirtualFile` (unsupported)
        file.putUserData(PlsKeys.injectedFileInfo, fileInfo)
        return true
    }

    fun injectLocaleConfig(file: VirtualFile, localeConfig: CwtLocaleConfig?): Boolean {
        if (PlsFileManager.isStubFile(file)) return false // skip for `StubVirtualFile` (unsupported)
        file.putUserData(PlsKeys.injectedLocaleConfig, localeConfig)
        return true
    }

    fun injectRootKeys(file: VirtualFile, rootKeys: List<String>): Boolean {
        if (PlsFileManager.isStubFile(file)) return false // skip for `StubVirtualFile` (unsupported)
        file.putUserData(PlsKeys.injectedRootKeys, rootKeys.orNull())
        return true
    }

    fun configureUseDefaultFileExtensions(value: Boolean) {
        val v = useDefaultFileExtensions
        if (value) v.set(true) else v.remove()
    }

    fun configureUseGameTypeInference(value: Boolean) {
        val v = useGameTypeInference
        if (value) v.set(true) else v.remove()
    }

    fun markRootInfo(rootInfo: ParadoxRootInfo) {
        markedRootInfo.set(rootInfo)
    }

    fun clearMarkedRootInfo() {
        markedRootInfo.remove()
    }

    fun markFileInfo(fileInfo: ParadoxFileInfo) {
        markedFileInfo.set(fileInfo)
    }

    fun markFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
        val filePath = ParadoxPath.resolve(path)
        val fileEntry = entry
        val fileGroup = group ?: ParadoxFileGroup.resolvePossible(path.substringAfterLast('/'))
        val fileInfo = ParadoxFileInfo(filePath, fileEntry, fileGroup, ParadoxRootInfo.Injected(gameType))
        markedFileInfo.set(fileInfo)
    }

    fun clearMarkedFileInfo() {
        markedFileInfo.remove()
    }

    // endregion
}
