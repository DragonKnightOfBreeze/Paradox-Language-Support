package icu.windea.pls.lang.psi.mock

import com.intellij.codeInsight.highlighting.*
import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.model.*
import java.util.*
import javax.swing.*

/**
 * 定义的参数并不存在一个真正意义上的声明处，用这个模拟。
 *
 * [contextKey] 用于判断参数是否拥有相同的上下文，格式如下：
 * * 对于定义的参数：`<typeExpression>@<definitionName>`
 * * 对于内联脚本的参数：`inline_script@<inline_script_expression>`
 *
 * @see icu.windea.pls.script.psi.ParadoxParameter
 * @see icu.windea.pls.script.psi.ParadoxConditionParameter
 * @see ParadoxParameterSupport
 */
class ParadoxParameterElement(
    parent: PsiElement,
    private val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    val rangeInParent: TextRange?,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val gameType: ParadoxGameType,
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

    override fun getPresentation(): ItemPresentation {
        return ParadoxParameterElementPresentation(this)
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

