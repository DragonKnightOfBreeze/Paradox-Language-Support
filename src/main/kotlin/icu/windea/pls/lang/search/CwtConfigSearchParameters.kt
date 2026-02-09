package icu.windea.pls.lang.search

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.model.ParadoxGameType

interface CwtConfigSearchParameters {
    val gameType: ParadoxGameType?
    val project: Project
    val scope: GlobalSearchScope
}
