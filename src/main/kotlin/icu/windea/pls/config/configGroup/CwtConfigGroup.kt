package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

/**
 * CWT规则分组。
 * @property gameType 对应的游戏类型。如果为null，则会得到共用的核心规则分组。
 * @property project 对应的项目。如果不需要访问PSI，可以直接传入默认项目。
 */
class CwtConfigGroup(
    val info: CwtConfigGroupInfo,
    val gameType: ParadoxGameType?,
    val project: Project,
) : UserDataHolderBase() {
    object Keys : KeyRegistry
}