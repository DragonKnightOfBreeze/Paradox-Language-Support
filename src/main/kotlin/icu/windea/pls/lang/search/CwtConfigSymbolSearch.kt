package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*

class CwtConfigSymbolSearch : ExtensibleQueryFactory<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.SearchParameters>(EP_NAME) {
    class SearchParameters(
        val name: String?,
        val type: String,
        val gameType: ParadoxGameType,
        val project: Project,
        val scope: GlobalSearchScope
    )

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.Companion.create<QueryExecutor<CwtConfigSymbolIndexInfo, SearchParameters>>("icu.windea.pls.search.configSymbolSearch")
        @JvmField
        val INSTANCE = CwtConfigSymbolSearch()

        /**
         * @see CwtConfigSymbolSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            type: String,
            gameType: ParadoxGameType,
            project: Project,
            scope: GlobalSearchScope
        ): Query<CwtConfigSymbolIndexInfo> {
            return INSTANCE.createQuery(SearchParameters(name, type, gameType, project, scope))
        }
    }
}
