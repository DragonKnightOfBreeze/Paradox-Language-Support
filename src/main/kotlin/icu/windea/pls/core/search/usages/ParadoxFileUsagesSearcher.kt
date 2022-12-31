package icu.windea.pls.core.search.usages

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import kotlin.experimental.*

/**
 * 文件（相对于游戏或模组根目录）的使用的查询。
 * @see CwtPathExpressionType
 */
class ParadoxFileUsagesSearcher: QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
	override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
		//0.7.8 这里不能仅仅用fileName去查找，需要基于CWT规则文件判断
		val target = queryParameters.elementToSearch
		if(target !is PsiFile) return
		val fileInfo = target.fileInfo
		if(fileInfo == null) return
		val gameType = fileInfo.rootInfo.gameType
		val filePath = fileInfo.path.toString()
		val project = queryParameters.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		val extraWords = SmartList<String>()
		configGroup.info.filePathExpressions
			.firstNotNullOfOrNull { CwtPathExpressionType.FilePath.extract(it, filePath) }
			?.takeIfNotEmpty()
			?.let { extraWords.add(it) }
		if(target.name.endsWith(".dds", true)) {
			configGroup.info.iconPathExpressions
				.firstNotNullOfOrNull { CwtPathExpressionType.Icon.extract(it, filePath) }
				?.takeIfNotEmpty()
				?.let { extraWords.add(it) }
		}
		if(extraWords.isEmpty()) return
		DumbService.getInstance(project).runReadActionInSmartMode {
			//这里不能直接使用target.useScope，否则文件高亮会出现问题
			val useScope = queryParameters.effectiveSearchScope
			for(extraWord in extraWords) {
				val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_STRINGS or UsageSearchContext.IN_COMMENTS
				queryParameters.optimizer.searchWord(extraWord, useScope, searchContext, true, target)
			}
		}
	}
}