package icu.windea.pls.model

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.ep.resolve.parameter.support
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import javax.swing.Icon

/**
 * 参数信息。
 *
 * [contextKey] 用于判断参数是否拥有相同的上下文，格式如下：
 * - 对于定义的参数：`<typeExpression>@<definitionName>`
 * - 对于内联脚本的参数：`inline_script@<inline_script_expression>`
 *
 * @see ParadoxParameter
 * @see ParadoxConditionParameter
 * @see ParadoxParameterSupport
 */
data class ParadoxParameterInfo(
    val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }

    companion object {
        val EMPTY by lazy { ParadoxParameterInfo("", "", null, "", ParadoxGameType.Core, getDefaultProject()) }
    }
}

@Suppress("unused")
fun ParadoxParameterInfo.toPsiElement(parent: PsiElement, readWriteAccess: ReadWriteAccessDetector.Access): ParadoxParameterLightElement {
    return ParadoxParameterLightElement(parent, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        .also { ParadoxParameterSupport.Keys.sync(this, it) }
}

fun ParadoxParameterLightElement.toInfo(): ParadoxParameterInfo {
    return ParadoxParameterInfo(name, contextName, contextIcon, contextKey, gameType, project)
        .also { ParadoxParameterSupport.Keys.sync(this, it) }
}
