package icu.windea.pls.ai.services

import com.intellij.openapi.diagnostic.*
import dev.langchain4j.kotlin.model.chat.*
import icu.windea.pls.*
import icu.windea.pls.core.coroutines.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.invoke.*
import kotlin.contracts.*

abstract class PlsAiManipulateLocalisationService : PlsAiService {
    protected val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    fun Flow<StreamingChatModelReply>.toResultFlow(): Flow<ParadoxLocalisationResult> {
        return toLineFlow({
            when (it) {
                is StreamingChatModelReply.PartialResponse -> it.partialResponse
                is StreamingChatModelReply.CompleteResponse -> ""
                is StreamingChatModelReply.Error -> throw it.cause
            }
        }, {
            ParadoxLocalisationResult.fromLine(it)
        }).onCompletion { e ->
            when {
                e is CancellationException -> logger.warn("[AI RESPONSE] Cancelled.")
                e != null -> logger.warn("[AI RESPONSE] Failed.", e)
                else -> logger.info("[AI RESPONSE] Done.")
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun checkResultFlow(resultFlow: Flow<ParadoxLocalisationResult>?) {
        contract {
            returns() implies (resultFlow != null)
        }
        if (resultFlow == null) { //resultFlow返回null，这意味着AI设置不合法，例如API KEY未填写（但不包括API kEY已填写但正确的情况）
            throw IllegalStateException(PlsBundle.message("manipulation.localisation.error.1"))
        }
    }

    fun checkResult(context: ParadoxLocalisationContext, result: ParadoxLocalisationResult) {
        if (context.key.isEmpty()) { //输出内容的格式不合法
            throw IllegalStateException(PlsBundle.message("manipulation.localisation.error.2"))
        }
        if (context.key != result.key) { //不期望的结果，直接报错，中断收集
            throw IllegalStateException(PlsBundle.message("manipulation.localisation.error.3", context.key, result.key))
        }
    }
}
