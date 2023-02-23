package icu.windea.pls.core.findUsages

import com.intellij.openapi.project.*

class ParadoxLocalisationFindUsagesOptions(project: Project): ParadoxFindUsagesOptions(project) {
    @JvmField var isSearch
}