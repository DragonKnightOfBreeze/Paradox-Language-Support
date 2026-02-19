package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定义的查询。
 */
@Suppress("unused")
class ParadoxDefinitionSearch : ExtensibleQueryFactory<ParadoxDefinitionIndexInfo, ParadoxDefinitionSearch.SearchParameters>(EP_NAME) {
    /**
     * @property name 定义的名字。
     * @property typeExpression 定义的类型表达式。参见 [ParadoxDefinitionTypeExpression]。
     * @property selector 查询选择器。
     */
    class SearchParameters(
        val name: String?,
        val typeExpression: String?,
        override val selector: ParadoxSearchSelector<ParadoxDefinitionIndexInfo>,
    ) : ParadoxSearchParameters<ParadoxDefinitionIndexInfo> {
        private val _typeExpression = typeExpression?.let { ParadoxDefinitionTypeExpression.resolve(it) }
        val type = _typeExpression?.type
        val subtypes = _typeExpression?.subtypes
    }

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName<QueryExecutor<ParadoxDefinitionIndexInfo, SearchParameters>>("icu.windea.pls.search.definitionSearch")
        @JvmField
        val INSTANCE = ParadoxDefinitionSearch()

        /**
         * @see ParadoxDefinitionSearch.SearchParameters
         */
        @JvmStatic
        fun search(
            name: String?,
            typeExpression: String?,
            selector: ParadoxSearchSelector<ParadoxDefinitionIndexInfo>,
        ): ParadoxUnaryQuery<ParadoxDefinitionIndexInfo> {
            return INSTANCE.createParadoxQuery(SearchParameters(name, typeExpression, selector))
        }

        /**
         * @see ParadoxDefinitionSearch.SearchParameters
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
         * @see ParadoxDefinitionSearch.SearchParameters
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
         * @see ParadoxDefinitionSearch.SearchParameters
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
