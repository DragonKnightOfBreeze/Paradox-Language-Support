package icu.windea.pls.test

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import icu.windea.pls.core.toPath
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import java.io.File
import java.nio.file.Path

/**
 * 测试作用域。提供了一组实用的作用域方法。
 *
 * 适用于平台测试，尤其是需要注入上下文信息（如文件信息）或需要基于规则数据的场合。
 */
@Suppress("unused")
interface ChronicleTestScope {
    // region Common Methods

    fun CodeInsightTestFixture.findElementAtCaret(): PsiElement? {
        return file.findElementAt(caretOffset)
    }

    fun CodeInsightTestFixture.findReferenceAtCaret(): PsiReference? {
        return file.findReferenceAt(caretOffset)
    }

    /** @see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots */
    fun addAdditionalAllowedRoots(vararg roots: String?) {
        val additionalAllowedRoots = roots.mapNotNull { it?.toPath()?.toAbsolutePath()?.normalize()?.toString() }
        doAddAdditionalAllowedRoots(additionalAllowedRoots)
    }

    /** @see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots */
    fun addAdditionalAllowedRoots(vararg roots: Path?) {
        val additionalAllowedRoots = roots.mapNotNull { it?.toAbsolutePath()?.normalize()?.toString() }
        doAddAdditionalAllowedRoots(additionalAllowedRoots)
    }

    private fun doAddAdditionalAllowedRoots(additionalAllowedRoots: List<String>) {
        val oldValue = System.getProperty("vfs.additional-allowed-roots").orEmpty()
        val newList = listOf(oldValue).filter { it.isNotBlank() } + additionalAllowedRoots
        val newValue = newList.distinct().joinToString(File.pathSeparator)
        System.setProperty("vfs.additional-allowed-roots", newValue)
    }

    // endregion

    // region Highlighting Methods

    data class HighlightingTag(val start: String, val end: String)

    fun String.toTag(level: String) = HighlightingTag("<$level descr=\"${this.replace("\"", "\\\\\"")}\">", "</$level>")

    fun String.toErrorTag() = toTag("error")

    fun String.toWarningTag() = toTag("warning")

    fun String.toWeakWarningTag() = toTag("weak_warning")

    fun String.toInfoTag() = toTag("info")

    fun TextAttributesKey.toTag() = HighlightingTag("<info textAttributesKey=\"${this.externalName}\">", "</info>")

    // endregion

    // region Context Related Methods

    /**
     * 启用仅用于平台测试的特殊行为。这会启用从文件名推断文件类型和游戏类型。
     */
    fun markIntegrationTest() {
        ParadoxAnalysisInjectionManager.useDefaultFileExtensions(true)
        ParadoxAnalysisInjectionManager.useGameTypeInference(true)

        addAdditionalAllowedRoots(PathManager.getPluginsDir()) // Why should I add this? So unreasonable.
    }

    /**
     * 关闭仅用于平台测试的特殊行为。这会一并清空注入状态。
     */
    fun clearIntegrationTest() {
        ParadoxAnalysisInjectionManager.useDefaultFileExtensions(false)
        ParadoxAnalysisInjectionManager.useGameTypeInference(false)
        ParadoxAnalysisInjectionManager.clearMarkedRootInfo()
        ParadoxAnalysisInjectionManager.clearMarkedFileInfo()
        ParadoxAnalysisInjectionManager.clearMarkedRootDirectory()
        ParadoxAnalysisInjectionManager.clearMarkedConfigDirectory()
    }

    /**
     * 注入游戏或模组的根目录。
     *
     * 说明：
     * - 传入的路径相对于测试数据目录（`src/test/testData`）。
     * - 用于测试的游戏或模组文件通常不需要实际位于这个目录（或其子目录）中。
     * - 某些缓存，例如语义匹配结果的缓存，位于根目录级别。进行相关的平台测试时，调用这个方法是必要的。
     */
    fun markRootDirectory(relPath: String) {
        val testDataPath = "src/test/testData".toPathOrNull() ?: return
        val path = testDataPath.resolve(relPath)
        ParadoxAnalysisInjectionManager.markRootDirectory(relPath, path)
    }

    /**
     * 注入规则目录。
     *
     * 说明：
     * - 传入的路径相对于测试数据目录（`src/test/testData`）。
     * - 规则文件应位于这个目录的特定子目录中（而不是直接位于这个目录中）。例如通用的规则文件应位于 `core` 子目录中，Stellaris 的规则文件应位于 `stellaris` 子目录中。
     *
     * @see ParadoxGameType
     */
    fun markConfigDirectory(relPath: String) {
        val testDataPath = "src/test/testData".toPathOrNull() ?: return
        val path = testDataPath.resolve(relPath)
        ParadoxAnalysisInjectionManager.markConfigDirectory(relPath, path)
    }

    /**
     * 创建注入的根信息。可以指定游戏类型和游戏版本。
     */
    fun createRootInfo(gameType: ParadoxGameType, gameVersion: String? = null): ParadoxRootInfo.Injected {
        return ParadoxAnalysisInjectionManager.createRootInfo(gameType, gameVersion)
    }

    /**
     * （为后续配置的测试数据文件）注入文件信息。
     *
     * 说明：
     * - 传入的路径相对于入口目录（其不一定同时是游戏或模组目录）。例如应直接传入 `events/test_events.txt`，而非传入 `game/events/test_events.txt`。
     * - 这里注入的文件路径不需要与实际的文件路径对齐。
     *
     * @see ParadoxGameType
     * @see ParadoxGameTypeMetadata
     */
    fun markFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
        ParadoxAnalysisInjectionManager.markFileInfo(createRootInfo(gameType), path, entry, group)
    }

    /**
     * （为后续配置的测试数据文件）注入文件信息。
     *
     * 说明：
     * - 传入的路径相对于入口目录（其不一定同时是游戏或模组目录）。例如应直接传入 `events/test_events.txt`，而非传入 `game/events/test_events.txt`。
     * - 这里注入的文件路径不需要与实际的文件路径对齐。
     *
     * @see ParadoxGameType
     * @see ParadoxGameTypeMetadata
     */
    fun markFileInfo(rootInfo: ParadoxRootInfo, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
        ParadoxAnalysisInjectionManager.markFileInfo(rootInfo, path, entry, group)
    }

    /**
     * （为当前的测试数据文件）注入文件信息。
     *
     * 说明：
     * - 传入的路径相对于入口目录（其不一定同时是游戏或模组目录）。例如应直接传入 `events/test_events.txt`，而非传入 `game/events/test_events.txt`。
     * - 这里注入的文件路径不需要与实际的文件路径对齐。
     *
     * @see ParadoxGameType
     * @see ParadoxGameTypeMetadata
     */
    fun VirtualFile.injectFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
        ParadoxAnalysisInjectionManager.injectFileInfo(this, createRootInfo(gameType), path, entry, group)
    }

    /**
     * （为当前的测试数据文件）注入文件信息。
     *
     * 说明：
     * - 传入的路径相对于入口目录（其不一定同时是游戏或模组目录）。例如应直接传入 `events/test_events.txt`，而非传入 `game/events/test_events.txt`。
     * - 这里注入的文件路径不需要与实际的文件路径对齐。
     *
     * @see ParadoxGameType
     * @see ParadoxGameTypeMetadata
     */
    fun VirtualFile.injectFileInfo(rootInfo: ParadoxRootInfo, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
        ParadoxAnalysisInjectionManager.injectFileInfo(this, rootInfo, path, entry, group)
    }

    // endregion

    // region Config Related Methods

    /**
     * 为指定的一组游戏类型初始化规则分组。
     *
     * 说明：
     * - 使用注入的和内置的规则文件。
     * - 通用的规则分组总是会被初始化。
     */
    fun initConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
        ParadoxAnalysisInjectionManager.useOnlyBuiltInAndInjectedConfigFiles(true)
        ChronicleTestManager.initConfigGroups(project, gameTypes.toList(), onlyInjected = false)
        ParadoxAnalysisInjectionManager.useOnlyBuiltInAndInjectedConfigFiles(false)
    }

    /**
     * 为指定的一组游戏类型初始化规则分组。
     *
     * 说明：
     * - 仅使用注入的的规则文件。
     * - 通用的规则分组总是会被初始化。
     */
    fun initInjectedConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
        ParadoxAnalysisInjectionManager.useOnlyInjectedConfigFiles(true)
        ChronicleTestManager.initConfigGroups(project, gameTypes.toList(), onlyInjected = true)
        ParadoxAnalysisInjectionManager.useOnlyInjectedConfigFiles(false)
    }

    // endregion
}
