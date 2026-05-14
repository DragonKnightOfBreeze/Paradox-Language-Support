package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.ParadoxShaderEffectSearch.*
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxShaderEffectIndexInfo

/**
 * 着色器效果（shader effect）的查询。
 */
class ParadoxShaderEffectSearch : ExtensibleQueryFactory<ParadoxShaderEffectIndexInfo, Parameters>(EP_NAME) {
    /**
     * 着色器效果（shader effect）的查询参数。
     *
     * @property name 名字。
     */
    data class Parameters(
        val name: String?,
        override val selector: ParadoxSearchSelector<ParadoxShaderEffectIndexInfo>
    ) : ParadoxSearchParameters<ParadoxShaderEffectIndexInfo> {
        fun createContext(scope: GlobalSearchScope = this.scope) = Context(name, gameType, project, scope)
    }

    data class Context(
        val name: String?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxShaderEffectIndexInfo, Parameters>>("icu.windea.pls.search.shaderEffectSearch")
        @JvmField val INSTANCE = ParadoxShaderEffectSearch()

        @JvmStatic
        fun selector(project: Project, context: Any? = null) = ParadoxSearchSelector<ParadoxShaderEffectIndexInfo>(project, context)

        /**
         * @see Parameters
         */
        @JvmStatic
        fun search(name: String?, selector: ParadoxSearchSelector<ParadoxShaderEffectIndexInfo>): ParadoxUnaryQuery<ParadoxShaderEffectIndexInfo> {
            return INSTANCE.search(Parameters(name, selector))
        }
    }
}
