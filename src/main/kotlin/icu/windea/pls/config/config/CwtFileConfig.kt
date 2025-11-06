package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtFileConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile

/**
 * 文件规则。
 *
 * 对应一整个 CWT 规则文件。
 *
 * @property name 文件名。
 * @property path 文件路径（相对于所属规则分组的目录）。
 * @property configs 子规则列表（其中的属性与值对应的成员规则）。
 * @property configs 子规则列表（其中的属性与值对应的成员规则）。
 * @property properties 子属性规则列表（其中的属性对应的成员规则）。
 * @property values 子值规则列表（其中的值对应的成员规则）。
 *
 * @see CwtFile
 */
interface CwtFileConfig : CwtConfig<CwtFile> {
    val name: String
    val path: String
    val configs: List<CwtMemberConfig<*>>
    val properties: List<CwtPropertyConfig>
    val values: List<CwtValueConfig>

    interface Resolver {
        fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig
    }

    companion object : Resolver by CwtFileConfigResolverImpl()
}
