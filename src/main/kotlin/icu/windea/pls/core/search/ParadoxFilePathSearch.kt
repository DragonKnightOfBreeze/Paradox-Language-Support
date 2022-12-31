package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.selector.chained.*

/**
 * 文件路径的查询。
 */
class ParadoxFilePathSearch : ExtensibleQueryFactory<VirtualFile, ParadoxFilePathSearch.SearchParameters>(EP_NAME) {
	/**
	 * @param filePath 相对于游戏或模组根目录的文件路径。
	 * @param type 使用何种文件路径表达式类型。默认使用精确路径。
	 * @param ignoreCase 匹配路径时是否忽略大小写。 默认为`true`。
	 * @param selector 用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
	 */
	class SearchParameters(
		val filePath: String?,
		val type: CwtPathExpressionType,
		val ignoreCase: Boolean,
		val project: Project,
		val scope: SearchScope,
		override val selector: ChainedParadoxSelector<VirtualFile>
	) : ParadoxSearchParameters<VirtualFile>
	
	companion object {
		@JvmField
		val EP_NAME = ExtensionPointName.create<QueryExecutor<VirtualFile, SearchParameters>>("icu.windea.pls.paradoxFilePathSearch")
		@JvmField val INSTANCE = ParadoxFilePathSearch()
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxFilePathSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			filePath: String?,
			project: Project,
			type: CwtPathExpressionType = CwtPathExpressionType.Exact,
			ignoreCase: Boolean = true,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(filePath, type, ignoreCase, project, scope, selector))
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxFilePathSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			project: Project,
			type: CwtPathExpressionType = CwtPathExpressionType.Exact,
			ignoreCase: Boolean = true,
			scope: SearchScope = GlobalSearchScope.allScope(project),
			selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
		) = INSTANCE.createParadoxQuery(SearchParameters(null, type, ignoreCase, project, scope, selector))
	}
}

