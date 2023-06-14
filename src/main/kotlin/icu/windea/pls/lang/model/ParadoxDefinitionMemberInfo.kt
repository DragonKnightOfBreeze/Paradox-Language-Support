package icu.windea.pls.lang.model

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

/**
 * @property elementPath 相对于所属定义的定义成员路径。
 */
class ParadoxDefinitionMemberInfo(
    val elementPath: ParadoxElementPath,
    val definitionInfo: ParadoxDefinitionInfo,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
    val element: ParadoxScriptMemberElement
    //element直接作为属性的话可能会有些问题，不过这个缓存会在所在脚本文件变更时被清除，应当问题不大
    //element不能转为SmartPsiElementPointer然后作为属性，这会导致与ParadoxDefinitionInfo.element引发递归异常
) {
    val isDefinition: Boolean get() = element is ParadoxScriptDefinitionElement && elementPath.isEmpty()
    val isParameterized: Boolean get() = elementPath.isParameterized
}
