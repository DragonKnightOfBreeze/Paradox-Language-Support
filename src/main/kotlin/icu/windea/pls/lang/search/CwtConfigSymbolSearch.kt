package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import com.intellij.util.QueryExecutor
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo

class CwtConfigSymbolSearch : ExtensibleQueryFactory<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.SearchParameters>(EP_NAME) {
    class SearchParameters(
        val name: String?,
        val types: Collection<String>,
        val gameType: ParadoxGameType?,
        val project: Project,
        val scope: GlobalSearchScope
    )

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<CwtConfigSymbolIndexInfo, SearchParameters>>("icu.windea.pls.search.configSymbolSearch")
        @JvmField
        val INSTANCE = CwtConfigSymbolSearch()

        /**
         * @see CwtConfigSymbolSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            type: String,
            gameType: ParadoxGameType?,
            project: Project,
            scope: GlobalSearchScope
        ): Query<CwtConfigSymbolIndexInfo> {
            return INSTANCE.createQuery(SearchParameters(name, setOf(type), gameType, project, scope))
        }

        /**
         * @see CwtConfigSymbolSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            types: Collection<String>,
            gameType: ParadoxGameType?,
            project: Project,
            scope: GlobalSearchScope
        ): Query<CwtConfigSymbolIndexInfo> {
            return INSTANCE.createQuery(SearchParameters(name, types, gameType, project, scope))
        }
    }
}
