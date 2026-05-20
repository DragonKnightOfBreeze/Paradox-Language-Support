package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.searchers.ParadoxDefinitionSearcher
import icu.windea.pls.lang.search.util.ParadoxQuery
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.createParadoxQuery
import icu.windea.pls.lang.search.util.distinctBy
import icu.windea.pls.lang.search.util.withTransform
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定义的查询。
 *
 * @see ParadoxDefinitionSearcher
 */
class ParadoxDefinitionSearch : ExtensibleQueryFactory<ParadoxDefinitionIndexInfo, ParadoxDefinitionSearch.Parameters>(EP_NAME) {
    /**
     * 定义的查询参数。
     *
     * @property name 定义的名字。
     * @property typeExpression 定义的类型表达式。参见 [ParadoxDefinitionTypeExpression]。
     */
    data class Parameters(
        val name: String?,
        val typeExpression: String?,
        override val selector: Selector,
    ) : ParadoxSearchParameters<ParadoxDefinitionIndexInfo>

    class Selector(project: Project, context: Any?) : ParadoxSearchSelector<ParadoxDefinitionIndexInfo>(project, context) {
        fun distinct() = distinctBy { it.name }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDefinitionIndexInfo, Parameters>>("icu.windea.pls.search.definitionSearch")
        @JvmField val INSTANCE = ParadoxDefinitionSearch()

        /** @see Selector */
        @JvmStatic
        fun selector(project: Project, context: Any? = null) = Selector(project, context)

        /** @see Parameters */
        @JvmStatic
        fun search(name: String?, typeExpression: String?, selector: Selector): ParadoxUnaryQuery<ParadoxDefinitionIndexInfo> {
            return INSTANCE.createParadoxQuery(Parameters(name, typeExpression, selector))
        }

        /** @see Parameters */
        @JvmStatic
        fun searchElement(name: String?, typeExpression: String?, selector: Selector): ParadoxQuery<ParadoxDefinitionIndexInfo, ParadoxDefinitionElement> {
            return search(name, typeExpression, selector).withTransform { it.element }
        }

        /** @see Parameters */
        @JvmStatic
        fun searchFile(name: String?, typeExpression: String?, selector: Selector): ParadoxQuery<ParadoxDefinitionIndexInfo, PsiFile> {
            return search(name, typeExpression, selector).withTransform { it.fileElement }
        }

        /** @see Parameters */
        @JvmStatic
        fun searchProperty(name: String?, typeExpression: String?, selector: Selector): ParadoxQuery<ParadoxDefinitionIndexInfo, ParadoxScriptProperty> {
            return search(name, typeExpression, selector).withTransform { it.propertyElement }
        }
    }
}
