package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

/**
 * 本地封装变量的查询。（本地：同一脚本文件）
 */
class ParadoxLocalScriptedVariableSearch : ExtensibleQueryFactory<ParadoxScriptScriptedVariable, ParadoxLocalScriptedVariableSearch.SearchParameters>(EP_NAME) {
	/**
	 * @property name 变量的名字，不以"@"开始。
	 * @property context 需要从哪个[PsiElement]开始，在整个脚本文件内，向上查找。
	 */
	class SearchParameters(
		val name: String?,
		val context: PsiElement,
		override val selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>
	) : ParadoxSearchParameters<ParadoxScriptScriptedVariable>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<ParadoxScriptScriptedVariable, SearchParameters>>("icu.windea.pls.paradoxLocalScriptedVariableSearch")
		@JvmField val INSTANCE = ParadoxLocalScriptedVariableSearch()
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxLocalScriptedVariableSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			name: String,
			context: PsiElement,
			selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(name, context, selector))
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxLocalScriptedVariableSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			context: PsiElement,
			selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(null, context, selector))
	}
}