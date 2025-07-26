package icu.windea.pls.lang.psi.mock

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo
import java.util.*
import javax.swing.*

/**
 * 用于为CWT规则文件中的一些符号提供对引用解析和查找用法的支持。
 */
class CwtConfigSymbolElement(
    parent: PsiElement,
    private val name: String,
    val configType: CwtConfigType,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val gameType: ParadoxGameType,
    private val project: Project
) : CwtMockPsiElement(parent) {
    override fun getIcon(): Icon? {
        return configType.icon
    }

    override fun getName(): String {
        return name
    }

    override fun getTypeName(): String? {
        return configType.prefix
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is CwtConfigSymbolElement &&
            name == other.name &&
            configType == other.configType &&
            project == other.project &&
            gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, configType, project, gameType)
    }
}
