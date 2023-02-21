package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.selector.chained.*

/**
 * 文件路径的查询。
 */
class ParadoxFilePathSearch : ExtensibleQueryFactory<VirtualFile, ParadoxFilePathSearch.SearchParameters>(EP_NAME) {
	/**
	 * @param filePath 相对于游戏或模组根目录的文件路径。或者写在脚本文件中的路径引用表达式。
	 * @param configExpression 对应的CWT规则表达式。拥有数种写法的文件路径表达式。
	 * @param ignoreCase 匹配路径时是否忽略大小写。 默认为`true`。
	 * @param selector 用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
	 */
	class SearchParameters(
		val filePath: String?,
		val configExpression: CwtDataExpression?,
		val ignoreCase: Boolean,
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
			configExpression: CwtDataExpression? = null,
			ignoreCase: Boolean = true,
			selector: ChainedParadoxSelector<VirtualFile>
		): ParadoxQuery<VirtualFile, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(filePath, configExpression, ignoreCase, selector))
		}
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxFilePathSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			configExpression: CwtDataExpression? = null,
			ignoreCase: Boolean = true,
			selector: ChainedParadoxSelector<VirtualFile>
		): ParadoxQuery<VirtualFile, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, configExpression, ignoreCase, selector))
		}
	}
}

