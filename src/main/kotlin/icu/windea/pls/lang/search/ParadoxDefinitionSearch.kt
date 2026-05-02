package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.util.ParadoxQuery
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.lang.search.util.ParadoxUnaryQuery
import icu.windea.pls.lang.search.util.search
import icu.windea.pls.lang.search.util.withTransform
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定义的查询。
 */
class ParadoxDefinitionSearch : ExtensibleQueryFactory<ParadoxDefinitionIndexInfo, ParadoxDefinitionSearch.Parameters>(EP_NAME) {
    /**
     * 定义的查询参数。
     *
     * @property name 定义的名字。
     * @property typeExpression 定义的类型表达式。参见 [ParadoxDefinitionTypeExpression]。
     */
    class Parameters(
        val name: String?,
        val typeExpression: String?,
        override val selector: ParadoxSearchSelector<ParadoxDefinitionIndexInfo>,
    ) : ParadoxSearchParameters<ParadoxDefinitionIndexInfo> {
        private val _typeExpression = typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val type = _typeExpression?.type
        val subtypes = _typeExpression?.subtypes
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDefinitionIndexInfo, Parameters>>("icu.windea.pls.search.definitionSearch")
        @JvmField val INSTANCE = ParadoxDefinitionSearch()

        /**
         * @see ParadoxDefinitionSearch.Parameters
         */
        @JvmStatic
        fun search(
            name: String?,
            typeExpression: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxDefinitionIndexInfo> {
            return INSTANCE.search(Parameters(name, typeExpression, selector))
        }

        /**
         * @see ParadoxDefinitionSearch.Parameters
         */
        @JvmStatic
        fun searchElement(
            name: String?,
            typeExpression: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionIndexInfo>,
        ): ParadoxQuery<ParadoxDefinitionIndexInfo, ParadoxDefinitionElement> {
            return search(name, typeExpression, selector).withTransform { it.element }
        }

        /**
         * @see ParadoxDefinitionSearch.Parameters
         */
        @JvmStatic
        fun searchFile(
            name: String?,
            typeExpression: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionIndexInfo>,
        ): ParadoxQuery<ParadoxDefinitionIndexInfo, PsiFile> {
            return search(name, typeExpression, selector).withTransform { it.fileElement }
        }

        /**
         * @see ParadoxDefinitionSearch.Parameters
         */
        @JvmStatic
        fun searchProperty(
            name: String?,
            typeExpression: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionIndexInfo>,
        ): ParadoxQuery<ParadoxDefinitionIndexInfo, ParadoxScriptProperty> {
            return search(name, typeExpression, selector).withTransform { it.propertyElement }
        }
    }
}
