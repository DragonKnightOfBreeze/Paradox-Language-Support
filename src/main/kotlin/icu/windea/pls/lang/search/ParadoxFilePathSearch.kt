package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.search.searchers.ParadoxFilePathSearcher

/**
 * 文件路径的查询。
 *
 * @see ParadoxFilePathSearcher
 */
class ParadoxFilePathSearch : ExtensibleQueryFactory<VirtualFile, ParadoxFilePathSearch.Parameters>(EP_NAME) {
    /**
     * 文件路径的查询参数。
     *
     * @param filePath 相对于入口目录的文件路径。或者写在脚本文件中的路径引用表达式。
     * @param configExpression 对应的规则表达式。拥有数种写法的文件路径表达式。
     * @param ignoreLocale 使用指定的完整文件路径进行查找本地化文件时，是否忽略文件名中的本地化语言环境。
     * @param selector 查询选择器。用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
     */
    data class Parameters(
        val filePath: String?,
        val configExpression: CwtDataExpression?,
        val ignoreLocale: Boolean,
        override val selector: Selector,
    ) : ParadoxSearchParameters<VirtualFile>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<VirtualFile>(project, context) {
        fun distinct() = distinctBy { it.fileInfo?.path }
    }

    companion object {
        private val iconExpression = CwtDataExpression.resolve("icon[]", false)

        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<VirtualFile, Parameters>>("icu.windea.pls.search.filePathSearch")
        @JvmField val INSTANCE = ParadoxFilePathSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(filePath: String?, configExpression: CwtDataExpression? = null, selector: Selector, ignoreLocale: Boolean = false): ParadoxUnaryQuery<VirtualFile> {
            return INSTANCE.search(Parameters(filePath, configExpression, ignoreLocale, selector))
        }

        /** @see Parameters */
        @JvmStatic
        fun searchIcon(filePath: String?, selector: Selector, ignoreLocale: Boolean = false): ParadoxUnaryQuery<VirtualFile> {
            return search(filePath, iconExpression, selector, ignoreLocale)
        }

        /** @see Parameters */
        @JvmStatic
        fun searchInlineScript(expression: String, selector: Selector): ParadoxUnaryQuery<VirtualFile> {
            return search(ParadoxInlineScriptManager.getInlineScriptFilePath(expression), null, selector)
        }
    }
}
