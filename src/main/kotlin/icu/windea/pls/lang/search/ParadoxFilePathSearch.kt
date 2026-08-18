package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.searchers.ParadoxFilePathSearcher
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.createParadoxQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.lang.search.util.withFileExtensions
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constants.ChronicleConstants

/**
 * 文件路径的查询。
 *
 * @see ParadoxFilePathSearcher
 * @see ParadoxFilePathSearch.Parameters
 * @see ParadoxFilePathSearch.Selector
 */
class ParadoxFilePathSearch : ExtensibleQueryFactory<VirtualFile, ParadoxFilePathSearch.Parameters>(EP_NAME) {
    /**
     * 文件路径的查询参数。
     *
     * @param filePath 相对于入口目录的文件路径。或者写在脚本文件中的路径引用表达式。
     * @param configExpression 对应的规则表达式。拥有数种写法的文件路径表达式。
     * @param selector 查询选择器。用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
     * @param ignoreCase 查找文件时，是否忽略文件名的大小写。仅适用于指定了 [filePath] 且未指定 [configExpression] 的场合。
     * @param ignoreExtension 查找文件时，是否忽略文件名中的扩展名。仅适用于指定了 [filePath] 且未指定 [configExpression] 的场合。这意味着 [filePath] 的格式应形如 `some/icon`，而非 `some/icon.dds`。
     * @param ignoreLocale 查找文件时，是否忽略文件名中的本地化语言环境。仅适用于指定了 [filePath] 且未指定 [configExpression] 的场合。
     */
    data class Parameters(
        val filePath: String?,
        val configExpression: CwtDataExpression?,
        override val selector: Selector,
        val ignoreCase: Boolean = false,
        val ignoreExtension: Boolean = false,
        val ignoreLocale: Boolean = false,
    ) : ParadoxSearchParameters<VirtualFile>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<VirtualFile>(project, context) {
        fun distinct() = distinctBy { it.fileInfo?.path }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<VirtualFile, Parameters>>("icu.windea.pls.search.filePathSearch")
        @JvmField val INSTANCE = ParadoxFilePathSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(filePath: String?, configExpression: CwtDataExpression? = null, selector: Selector, ignoreCase: Boolean = false, ignoreExtension: Boolean = false, ignoreLocale: Boolean = false): ParadoxUnaryQuery<VirtualFile> {
            return INSTANCE.createParadoxQuery(Parameters(filePath, configExpression, selector, ignoreCase, ignoreExtension, ignoreLocale))
        }

        /**
         * @see Parameters
         * @see ChronicleConstants.imageFileExtensions
         */
        @JvmStatic
        fun searchImage(filePath: String?, configExpression: CwtDataExpression? = null, selector: Selector, ignoreCase: Boolean = false, ignoreExtension: Boolean = false): ParadoxUnaryQuery<VirtualFile> {
            val selector = selector.withFileExtensions(*ChronicleConstants.imageFileExtensions) // 3.0.1 optimize: limit file extensions
            return search(filePath, configExpression, selector, ignoreCase, ignoreExtension)
        }

        /**
         * @see Parameters
         * @see ChronicleConstants.imageFileExtensions
         */
        @JvmStatic
        fun searchModifierIcon(filePath: String?, selector: Selector): ParadoxUnaryQuery<VirtualFile> {
            val selector = selector.withFileExtensions(*ChronicleConstants.imageFileExtensions) // 3.0.1 optimize: limit file extensions
            return search(filePath, null, selector, ignoreCase = true, ignoreExtension = true)
        }

        /**
         * @see Parameters
         * @see ParadoxInlineScriptManager.getInlineScriptFilePath
         * @see ParadoxInlineScriptManager.inlineScriptFileExtension
         */
        @JvmStatic
        fun searchInlineScript(expression: String, selector: Selector): ParadoxUnaryQuery<VirtualFile> {
            val filePath = ParadoxInlineScriptManager.getInlineScriptFilePath(expression)
            val selector = selector.withFileExtensions(ParadoxInlineScriptManager.inlineScriptFileExtension) // 3.0.1 optimize: limit file extensions
            return search(filePath, null, selector)
        }
    }
}
