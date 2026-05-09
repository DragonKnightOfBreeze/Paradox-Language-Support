package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxQuery
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.lang.search.util.withTransform
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定义注入的查询。
 */
class ParadoxDefinitionInjectionSearch : ExtensibleQueryFactory<ParadoxDefinitionInjectionIndexInfo, ParadoxDefinitionInjectionSearch.Parameters>(EP_NAME) {
    /**
     * 定义注入的查询参数。
     *
     * @property mode 注入模式。
     * @property target 目标定义的名字。
     * @property type 目标定义的类型。
     */
    class Parameters(
        val mode: String?,
        val target: String?,
        val type: String?,
        override val selector: ParadoxSearchSelector<ParadoxDefinitionInjectionIndexInfo>,
    ) : ParadoxSearchParameters<ParadoxDefinitionInjectionIndexInfo>

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDefinitionInjectionIndexInfo, Parameters>>("icu.windea.pls.search.definitionInjectionSearch")
        @JvmField val INSTANCE = ParadoxDefinitionInjectionSearch()

        /**
         * @see ParadoxDefinitionInjectionSearch.Parameters
         */
        @JvmStatic
        fun search(
            mode: String?,
            target: String?,
            type: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionInjectionIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxDefinitionInjectionIndexInfo> {
            return INSTANCE.search(Parameters(mode, target, type, selector))
        }

        /**
         * @see ParadoxDefinitionInjectionSearch.Parameters
         */
        @JvmStatic
        fun searchElement(
            mode: String?,
            target: String?,
            type: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionInjectionIndexInfo>,
        ): ParadoxQuery<ParadoxDefinitionInjectionIndexInfo, ParadoxScriptProperty> {
            return INSTANCE.search(Parameters(mode, target, type, selector)).withTransform { it.element }
        }
    }
}
