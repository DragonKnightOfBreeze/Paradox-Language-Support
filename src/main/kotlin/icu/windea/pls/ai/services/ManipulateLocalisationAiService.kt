package icu.windea.pls.ai.services

import icu.windea.pls.ai.model.requests.*
import icu.windea.pls.ai.model.results.*
import kotlinx.coroutines.flow.*

abstract class ManipulateLocalisationAiService<R: ManipulateLocalisationAiRequest>: AiService {
    abstract fun manipulate(request: R): Flow<LocalisationAiResult>?
}
