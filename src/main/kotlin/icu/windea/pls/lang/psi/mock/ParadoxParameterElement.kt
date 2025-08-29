package icu.windea.pls.lang.psi.mock

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.parameter.ParadoxParameterSupport
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxConditionParameter
import java.util.*
import javax.swing.Icon

/**
 * 定义的参数并不存在一个真正意义上的声明处，用这个模拟。
 *
 * [contextKey] 用于判断参数是否拥有相同的上下文，格式如下：
 * * 对于定义的参数：`<typeExpression>@<definitionName>`
 * * 对于内联脚本的参数：`inline_script@<inline_script_expression>`
 *
 * @see icu.windea.pls.script.psi.ParadoxParameter
 * @see ParadoxConditionParameter
 * @see ParadoxParameterSupport
 */
class ParadoxParameterElement(
    parent: PsiElement,
    private val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxMockPsiElement(parent) {
    override fun getIcon(): Icon {
        return PlsIcons.Nodes.Parameter
    }

    override fun getName(): String {
        return name
    }

    override fun getTypeName(): String {
        return PlsBundle.message("script.description.parameter")
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxParameterElement &&
            name == other.name &&
            contextKey == other.contextKey &&
            project == other.project &&
            gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, contextKey, project, gameType)
    }
}

