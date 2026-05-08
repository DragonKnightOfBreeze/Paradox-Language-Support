package icu.windea.pls.lang.match

import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
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

data class CwtSubtypeConfigMatchContext(
    val configGroup: CwtConfigGroup,
    val configs: List<CwtSubtypeConfig>,
    val typeKey: String,
    val options: ParadoxMatchOptions? = null,
) {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    // NOTE 2.1.8 构建索引时不能内联展开子成员（可能需要研究是否可以绕过，或者是否存在可行替代方案）
    val inline = !ParadoxMatchService.isDumb(options)
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
