package icu.windea.pls.model

import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 复杂枚举值的解析信息。
 *
 * @property name 名字。
 * @property enumName 枚举名。
 * @property config 对应的复杂枚举规则（如果声明于脚本文件中），或者列规则（如果声明于 CSV 文件中）。
 *
 * @see CwtComplexEnumConfig
 * @see CwtExtendedComplexEnumValueConfig
 */
data class ParadoxComplexEnumValueInfo(
    val name: String,
    val enumName: String,
    val config: CwtConfig<*>,
) {
    val configGroup: CwtConfigGroup get() = config.configGroup
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    override fun toString(): String {
        return "ParadoxComplexEnumValueInfo(name=$name, enumName=$enumName, gameType=$gameType)"
    }
}
