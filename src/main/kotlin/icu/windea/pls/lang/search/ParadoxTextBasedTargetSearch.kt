package icu.windea.pls.lang.search

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import com.intellij.util.QueryExecutor
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchTargetType
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 基于本地化文本片段的目标的查询。
 *
 * 目前支持的目标类型：
 * - 封装变量 - [ParadoxSearchTargetType.ScriptedVariable] - [ParadoxScriptScriptedVariable]
 * - 定义 - [ParadoxSearchTargetType.Definition] - [ParadoxDefinitionElement]
 * - 本地化 - [ParadoxSearchTargetType.Localisation] - [ParadoxLocalisationProperty]
 */
class ParadoxTextBasedTargetSearch : ExtensibleQueryFactory<NavigatablePsiElement, ParadoxTextBasedTargetSearch.Parameters>(EP_NAME) {
    class Parameters(
        val text: String,
        val types: Set<ParadoxSearchTargetType>?,
        val project: Project,
        val scope: GlobalSearchScope
    ) {
        // 限制查询作用域：必须是本地化文件
        val restrictedScope by lazy { scope.withFileTypes(ParadoxLocalisationFileType) }
    }

    companion object {
        @JvmField val EP_NAME = ExtensionPointName<QueryExecutor<NavigatablePsiElement, Parameters>>("icu.windea.pls.search.textBasedTargetSearch")
        @JvmField val INSTANCE = ParadoxTextBasedTargetSearch()

        /**
         * @see ParadoxTextBasedTargetSearch.Parameters
         */
        @JvmStatic
        fun search(text: String, project: Project, scope: GlobalSearchScope): Query<NavigatablePsiElement> {
            return INSTANCE.createQuery(Parameters(text, null, project, scope))
        }

        /**
         * @see ParadoxTextBasedTargetSearch.Parameters
         */
        @JvmStatic
        fun search(text: String, project: Project, types: Set<ParadoxSearchTargetType>?, scope: GlobalSearchScope): Query<NavigatablePsiElement> {
            return INSTANCE.createQuery(Parameters(text, types, project, scope))
        }
    }
}
