package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.searchers.CwtConfigSymbolSearcher
import icu.windea.pls.lang.search.util.CwtConfigSearchParameters
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo

/**
 * 规则符号的查询。
 *
 * @see CwtConfigSymbolSearcher
 */
class CwtConfigSymbolSearch : ExtensibleQueryFactory<CwtConfigSymbolIndexInfo, CwtConfigSymbolSearch.Parameters>(EP_NAME) {
    /**
     * 规则符号的查询参数。
     *
     * @property name 名字。
     * @property types 来自规则的符号的类型键。
     */
    data class Parameters(
        val name: String?,
        val types: Collection<String>,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : CwtConfigSearchParameters

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<CwtConfigSymbolIndexInfo, Parameters>>("icu.windea.pls.search.configSymbolSearch")
        @JvmField val INSTANCE = CwtConfigSymbolSearch()

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, type: String, gameType: ParadoxGameType?, project: Project, scope: GlobalSearchScope): Query<CwtConfigSymbolIndexInfo> {
            return INSTANCE.createQuery(Parameters(name, setOf(type), gameType, project, scope))
        }

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, types: Collection<String>, gameType: ParadoxGameType?, project: Project, scope: GlobalSearchScope): Query<CwtConfigSymbolIndexInfo> {
            return INSTANCE.createQuery(Parameters(name, types, gameType, project, scope))
        }
    }
}
