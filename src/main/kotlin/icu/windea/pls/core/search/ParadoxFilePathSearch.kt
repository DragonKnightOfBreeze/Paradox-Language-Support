package icu.windea.pls.core.search

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.search.selectors.chained.*

/**
 * 文件路径的查询。
 */
class ParadoxFilePathSearch : ExtensibleQueryFactory<VirtualFile, ParadoxFilePathSearch.SearchParameters>(EP_NAME) {
	/**
	 * @param filePath 相对于游戏或模组根目录的文件路径。或者写在脚本文件中的路径引用表达式。
	 * @param configExpression 对应的CWT规则表达式。拥有数种写法的文件路径表达式。
	 * @param selector 用于指定如何选择需要查找的文件，尤其是当存在覆盖与重载的情况时。
	 * @param ignoreCase 匹配路径时是否忽略大小写。
	 * @param ignoreLocale 使用指定的完整文件路径进行查找本地化文件时，是否忽略文件名中的本地化语言区域。
	 */
	class SearchParameters(
		val filePath: String?,
		val configExpression: CwtDataExpression?,
		override val selector: ChainedParadoxSelector<VirtualFile>,
		val ignoreCase: Boolean,
		val ignoreLocale: Boolean
	) : ParadoxSearchParameters<VirtualFile>
	
	companion object {
		@JvmField val EP_NAME = ExtensionPointName.create<QueryExecutor<VirtualFile, SearchParameters>>("icu.windea.pls.paradoxFilePathSearch")
		@JvmField val INSTANCE = ParadoxFilePathSearch()
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxFilePathSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			filePath: String?,
			configExpression: CwtDataExpression? = null,
			selector: ChainedParadoxSelector<VirtualFile>,
			ignoreCase: Boolean = true,
			ignoreLocale: Boolean = false
		): ParadoxQuery<VirtualFile, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(filePath, configExpression, selector, ignoreCase, ignoreLocale))
		}
		
		/**
		 *  @see icu.windea.pls.core.search.ParadoxFilePathSearch.SearchParameters
		 */
		@JvmStatic
		fun search(
			configExpression: CwtDataExpression? = null,
			selector: ChainedParadoxSelector<VirtualFile>,
			ignoreCase: Boolean = true,
			ignoreLocale: Boolean = false
		): ParadoxQuery<VirtualFile, SearchParameters> {
			return INSTANCE.createParadoxQuery(SearchParameters(null, configExpression, selector, ignoreCase, ignoreLocale))
		}
	}
}

