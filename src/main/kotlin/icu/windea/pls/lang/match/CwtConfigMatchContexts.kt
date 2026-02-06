package icu.windea.pls.lang.match

import com.intellij.openapi.project.Project
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxPath

data class CwtRowConfigMatchContext(
    val configGroup: CwtConfigGroup,
    val path: ParadoxPath?,
) {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
}
