package icu.windea.pls.ai.services

import icu.windea.pls.ai.model.requests.ManipulateLocalisationAiRequest
import icu.windea.pls.ai.model.results.LocalisationAiResult
import kotlinx.coroutines.flow.Flow

abstract class ManipulateLocalisationAiService<R: ManipulateLocalisationAiRequest>: AiService {
    abstract fun manipulate(request: R): Flow<LocalisationAiResult>?
}
