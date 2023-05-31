package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

typealias ParadoxDefinitionHierarchySelector = ChainedParadoxSelector<ParadoxDefinitionHierarchyInfo>

fun definitionHierarchySelector(project: Project, context: Any? = null) = ParadoxDefinitionHierarchySelector(project, context)