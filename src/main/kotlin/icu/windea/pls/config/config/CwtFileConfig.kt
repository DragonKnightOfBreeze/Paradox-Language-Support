package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtFileConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile

/**
 * 文件规则。
 *
 * 对应一整个 CWT 规则文件。
 *
 * @see CwtFile
 */
interface CwtFileConfig : CwtConfig<CwtFile> {
    val name: String
    val path: String
    val properties: List<CwtPropertyConfig>
    val values: List<CwtValueConfig>

    interface Resolver {
        fun resolve(file: CwtFile, filePath: String, configGroup: CwtConfigGroup): CwtFileConfig
    }

    companion object : Resolver by CwtFileConfigResolverImpl()
}
