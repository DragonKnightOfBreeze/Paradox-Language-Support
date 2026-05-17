package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import com.intellij.util.QueryExecutor
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.core.cast
import icu.windea.pls.lang.search.searchers.CwtConfigSearcher
import icu.windea.pls.lang.search.util.CwtConfigSearchParameters
import icu.windea.pls.model.ParadoxGameType

/**
 * 规则的查询。
 *
 * 直接从规则分组中查询符合条件的规则对象。
 *
 * @see CwtConfigSearcher
 */
class CwtConfigSearch : ExtensibleQueryFactory<CwtConfig<*>, CwtConfigSearch.Parameters>(EP_NAME) {
    /**
     * 规则的查询参数。
     */
    sealed class Parameters(
        override val gameType: ParadoxGameType?,
        override val project: Project,
    ) : CwtConfigSearchParameters {
        override val scope: GlobalSearchScope get() = GlobalSearchScope.allScope(project)

        class ById<T : CwtIdMatchableConfig<*>>(
            val id: String?,
            val type: Class<T>,
            gameType: ParadoxGameType?,
            project: Project,
        ) : Parameters(gameType, project)

        class ByFilePath<T : CwtFilePathMatchableConfig<*>>(
            val filePath: String?,
            val type: Class<T>,
            gameType: ParadoxGameType?,
            project: Project,
        ) : Parameters(gameType, project)
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<CwtConfig<*>, Parameters>>("icu.windea.pls.search.configSearch")
        @JvmField val INSTANCE = CwtConfigSearch()

        /** @see Parameters.ById */
        @JvmStatic
        inline fun <reified T : CwtIdMatchableConfig<*>> searchById(id: String?, gameType: ParadoxGameType?, project: Project): Query<T> {
            return searchById(id, T::class.java, gameType, project)
        }

        /** @see Parameters.ById */
        @JvmStatic
        fun <T : CwtIdMatchableConfig<*>> searchById(id: String?, type: Class<T>, gameType: ParadoxGameType?, project: Project): Query<T> {
            return INSTANCE.createQuery(Parameters.ById(id, type, gameType, project)).cast()
        }

        /** @see Parameters.ByFilePath */
        @JvmStatic
        inline fun <reified T : CwtFilePathMatchableConfig<*>> searchByFilePath(filePath: String?, gameType: ParadoxGameType?, project: Project): Query<T> {
            return searchByFilePath(filePath, T::class.java, gameType, project)
        }

        /** @see Parameters.ByFilePath */
        @JvmStatic
        fun <T : CwtFilePathMatchableConfig<*>> searchByFilePath(filePath: String?, type: Class<T>, gameType: ParadoxGameType?, project: Project): Query<T> {
            return INSTANCE.createQuery(Parameters.ByFilePath(filePath, type, gameType, project)).cast()
        }
    }
}
