package icu.windea.pls.lang.match

import com.intellij.openapi.project.Project
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxPath

data class CwtTypeConfigMatchContext(
    val configGroup: CwtConfigGroup,
    val path: ParadoxPath? = null,
    val typeKey: String? = null,
    val rootKeys: List<String>? = null,
    val typeKeyPrefix: Lazy<String?>? = null,
    var matchPath: Boolean = true,
) {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
}

data class CwtComplexEnumConfigMatchContext(
    val configGroup: CwtConfigGroup,
    val path: ParadoxPath? = null,
    var matchPath: Boolean = true,
) {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
}

data class CwtRowConfigMatchContext(
    val configGroup: CwtConfigGroup,
    val path: ParadoxPath? = null,
    var matchPath: Boolean = true,
) {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
}
