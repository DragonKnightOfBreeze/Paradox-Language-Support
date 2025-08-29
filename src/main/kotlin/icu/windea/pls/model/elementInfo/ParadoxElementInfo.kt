package icu.windea.pls.model.elementInfo

import com.intellij.openapi.project.Project
import icu.windea.pls.model.ParadoxGameType

/**
 * 元素信息。记录的信息包括名字、游戏类型与项目。
 */
interface ParadoxElementInfo {
    val name: String
    val gameType: ParadoxGameType
    val project: Project
}
