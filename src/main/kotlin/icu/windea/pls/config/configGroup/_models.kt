package icu.windea.pls.config.configGroup

import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupDataModel.Empty.typeModel
import icu.windea.pls.ep.config.configGroup.CwtFileBasedConfigGroupProcessor
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

/**
 * 规则分组文件信息。
 *
 * @see CwtFileBasedConfigGroupProcessor
 */
data class CwtConfigGroupFileInfo(
    val filePath: String,
    val file: VirtualFile,
    val source: CwtConfigGroupFileSource,
)

/**
 * 规则文件当地来源。
 *
 * @see CwtFileBasedConfigGroupProcessor
 */
enum class CwtConfigGroupFileSource {
    BuiltIn,
    Remote,
    Local,
    Injected,
}

/**
 * 提供一组预定义的绑定到指定规则分组的模拟规则。这些规则是合成的，规则文件中不存在声明处。
 */
class CwtConfigGroupMockConfigModel(configGroup: CwtConfigGroup) {
    val anyProperty = CwtPropertyConfig.mock(configGroup, "\$any", "\$any")
    val anyValue = CwtValueConfig.mock(configGroup, "\$any")

    val bool = CwtValueConfig.mock(configGroup, "bool")
    val int = CwtValueConfig.mock(configGroup, "int")
    val float = CwtValueConfig.mock(configGroup, "float")
    val scalar = CwtValueConfig.mock(configGroup, "scalar")
    val wildcardScalar = CwtValueConfig.mock(configGroup, "wildcard_scalar")

    val scriptValue = CwtValueConfig.mock(configGroup, "<script_value>")
    val variable = CwtValueConfig.mock(configGroup, "value[variable]")
}

/**
 * 提供一组预定义的绑定到指定规则分组的 [ModificationTracker]。
 */
class CwtConfigGroupModificationTrackerModel(configGroup: CwtConfigGroup) {
    val scriptValue = ChronicleModificationTrackers.scriptFileFromDefinitionTypes(configGroup, ParadoxDefinitionTypes.scriptValue)
    val definitionParameter = ChronicleModificationTrackers.scriptFileFromDefinitionTypes(configGroup, typeModel.supportParameters)
    val definitionScopeContext = ChronicleModificationTrackers.scriptFileFromDefinitionTypes(configGroup, typeModel.supportScopeInference)
}
