package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import com.intellij.util.QueryExecutor
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.core.cast
import icu.windea.pls.model.ParadoxGameType

/**
 * 规则的查询。
 *
 * 直接从规则分组中查询符合条件的规则对象。
 */
class CwtConfigSearch : ExtensibleQueryFactory<CwtConfig<*>, CwtConfigSearch.SearchParameters>(EP_NAME) {
    sealed class SearchParameters(
        val gameType: ParadoxGameType?,
        val project: Project
    ) {
        class ById<T : CwtIdMatchableConfig<*>>(
            val id: String?,
            val type: Class<T>,
            gameType: ParadoxGameType?,
            project: Project
        ) : SearchParameters(gameType, project)

        class ByFilePath<T : CwtFilePathMatchableConfig<*>>(
            val filePath: String?,
            val type: Class<T>,
            gameType: ParadoxGameType?,
            project: Project
        ) : SearchParameters(gameType, project)
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<CwtConfig<*>, SearchParameters>>("icu.windea.pls.search.configSearch")
        @JvmField val INSTANCE = CwtConfigSearch()

        /**
         * @see CwtConfigSearch.SearchParameters.ById
         */
        @JvmStatic
        inline fun <reified T : CwtIdMatchableConfig<*>> searchById(id: String?, gameType: ParadoxGameType?, project: Project): Query<T> {
            return searchById(id, T::class.java, gameType, project)
        }

        /**
         * @see CwtConfigSearch.SearchParameters.ById
         */
        @JvmStatic
        fun <T : CwtIdMatchableConfig<*>> searchById(id: String?, type: Class<T>, gameType: ParadoxGameType?, project: Project): Query<T> {
            return INSTANCE.createQuery(SearchParameters.ById(id, type, gameType, project)).cast()
        }

        /**
         * @see CwtConfigSearch.SearchParameters.ByFilePath
         */
        @JvmStatic
        inline fun <reified T : CwtFilePathMatchableConfig<*>> searchByFilePath(filePath: String?, gameType: ParadoxGameType?, project: Project): Query<T> {
            return searchByFilePath(filePath, T::class.java, gameType, project)
        }

        /**
         * @see CwtConfigSearch.SearchParameters.ByFilePath
         */
        @JvmStatic
        fun <T : CwtFilePathMatchableConfig<*>> searchByFilePath(filePath: String?, type: Class<T>, gameType: ParadoxGameType?, project: Project): Query<T> {
            return INSTANCE.createQuery(SearchParameters.ByFilePath(filePath, type, gameType, project)).cast()
        }
    }
}
