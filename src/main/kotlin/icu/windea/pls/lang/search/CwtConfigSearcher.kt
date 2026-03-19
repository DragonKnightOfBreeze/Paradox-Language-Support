package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.Processor
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.match.CwtConfigMatchService

/**
 * 规则的查询器。
 *
 * 直接从规则分组中查询符合条件的规则对象。
 */
class CwtConfigSearcher : QueryExecutorBase<CwtConfig<*>, CwtConfigSearch.SearchParameters>() {
    override fun processQuery(queryParameters: CwtConfigSearch.SearchParameters, consumer: Processor<in CwtConfig<*>>) {
        ProgressManager.checkCanceled()

        val configGroup = PlsFacade.getConfigGroup(queryParameters.project, queryParameters.gameType)
        when (queryParameters) {
            is CwtConfigSearch.SearchParameters.ById<*> -> {
                CwtConfigMatchService.processMatchedConfigsById(queryParameters.id, configGroup, queryParameters.type) {
                    consumer.process(it)
                }
            }
            is CwtConfigSearch.SearchParameters.ByFilePath<*> -> {
                CwtConfigMatchService.processMatchedConfigsByFilePath(queryParameters.filePath, configGroup, queryParameters.type) {
                    consumer.process(it)
                }
            }
        }
    }
}
