package icu.windea.pls.ai.services

import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.manipulators.ParadoxLocalisationAiResult
import kotlinx.coroutines.flow.*

abstract class ManipulateLocalisationAiService<R: ManipulateLocalisationAiRequest> {
    abstract fun manipulate(request: R): Flow<ParadoxLocalisationAiResult>?
}
