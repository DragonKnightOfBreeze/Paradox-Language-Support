package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.model.codeInsight.ParadoxTargetInfo

/**
 * 基于本地化文本片段的目标（封装变量/定义/本地化）查询。
 */
class ParadoxTextBasedTargetSearch : ExtensibleQueryFactory<ParadoxTargetInfo, ParadoxTextBasedTargetSearch.SearchParameters>(EP_NAME) {
    class SearchParameters(
        val text: String,
        val types: Set<ParadoxSearchTargetType>?,
        val project: Project,
        val scope: GlobalSearchScope
    ) {
        // 限制查询作用域：必须是本地化文件
        val restrictedScope by lazy { scope.withFileTypes(ParadoxLocalisationFileType) }
    }

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxTargetInfo, SearchParameters>>("icu.windea.pls.search.text2TargetSearch")
        @JvmField
        val INSTANCE = ParadoxTextBasedTargetSearch()

        /** @see ParadoxTextBasedTargetSearch.SearchParameters */
        @JvmStatic
        fun search(text: String, project: Project, scope: GlobalSearchScope): Query<ParadoxTargetInfo> {
            return INSTANCE.createQuery(SearchParameters(text, null, project, scope))
        }

        /** @see ParadoxTextBasedTargetSearch.SearchParameters */
        @JvmStatic
        fun search(text: String, project: Project, types: Set<ParadoxSearchTargetType>?, scope: GlobalSearchScope): Query<ParadoxTargetInfo> {
            return INSTANCE.createQuery(SearchParameters(text, types, project, scope))
        }
    }
}
