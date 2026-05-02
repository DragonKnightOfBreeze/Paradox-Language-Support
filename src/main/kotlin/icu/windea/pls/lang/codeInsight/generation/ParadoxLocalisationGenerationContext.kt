package icu.windea.pls.lang.codeInsight.generation

import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

data class ParadoxLocalisationGenerationContext(
    val project: Project,
    val context: Any?,
    val locale: CwtLocaleConfig,
    val tooltip: String? = null,
    val infos: List<ParadoxLocalisationGenerationInfo> = emptyList(),
    val children: List<ParadoxLocalisationGenerationContext> = emptyList(),
)
