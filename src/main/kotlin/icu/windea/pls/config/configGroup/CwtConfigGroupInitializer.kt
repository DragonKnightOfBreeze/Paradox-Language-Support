package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import icu.windea.pls.model.ParadoxGameType

/**
 * 规则分组的初始化器。
 */
class CwtConfigGroupInitializer(
     val project: Project,
     val gameType: ParadoxGameType,
) : CwtConfigGroupDataHolderBase()
