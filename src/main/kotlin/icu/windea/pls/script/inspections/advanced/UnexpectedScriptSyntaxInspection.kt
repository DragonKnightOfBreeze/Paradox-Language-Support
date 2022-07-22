package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.annotations.*
import icu.windea.pls.script.psi.*

//TODO 0.7

/**
 * 不期望的脚本语法的检查。
 *
 * 包括以下几种情况：
 * * 不期望的封装变量引用（必须位于定义之内）
 * * 不期望的参数（必须位于定义之内）
 * * 不期望的参数条件语句（必须位于定义之内）
 * * 不期望的内联表达式（必须位于定义之内）
 * * 不期望的封装本地化引用（必须位于定义之内）
 * * 不期望的内联数学表达式的开始（"@["和"@\["） -> 提供快速修复：修正
 * * 不期望的封装本地化引用的开始（"["和"\["） -> 提供快速修复：修正
 * * 同一scripted_effect/scripted_trigger定义中存在多个内联数字表达式
 */
@Unstable
class UnexpectedScriptSyntaxInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxScriptVisitor() {
		//TODO
		override fun visitInlineMath(o: ParadoxScriptInlineMath) {
			super.visitInlineMath(o)
		}
	}
}