package icu.windea.pls.lang.psi.mock

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import java.util.*
import javax.swing.*

/**
 * 动态值值并不存在一个真正意义上的声明处，用这个模拟。
 */
class ParadoxDynamicValueElement(
    parent: PsiElement,
    private val name: String,
    val dynamicValueTypes: Set<String>,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxMockPsiElement(parent) {
    constructor(parent: PsiElement, name: String, dynamicValueType: String, readWriteAccess: ReadWriteAccessDetector.Access, gameType: ParadoxGameType, project: Project)
        : this(parent, name, setOf(dynamicValueType), readWriteAccess, gameType, project)

    val dynamicValueType = when {
        dynamicValueTypes.size == 2 && "event_target" in dynamicValueTypes && "global_event_target" in dynamicValueTypes -> "event_target"
        else -> dynamicValueTypes.joinToString(" | ")
    }

    override fun getIcon(): Icon {
        val dynamicValueType = dynamicValueTypes.first() //first is ok
        return PlsIcons.Nodes.DynamicValue(dynamicValueType)
    }

    override fun getName(): String {
        return name
    }

    override fun getTypeName(): String {
        val dynamicValueType = dynamicValueTypes.first() //first is ok
        return when (dynamicValueType) {
            "variable" -> PlsBundle.message("script.description.variable")
            else -> PlsBundle.message("script.description.dynamicValue")
        }
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxDynamicValueElement &&
            name == other.name &&
            dynamicValueTypes.any { it in other.dynamicValueTypes } &&
            project == other.project &&
            gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
