package com.windea.plugin.idea.paradox.script.editor

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptGoToDeclarationHandler: GotoDeclarationHandler {
	override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<out PsiElement?>? {
		return when(sourceElement) {
			null -> null
			is ParadoxScriptVariable -> {
				//查找当前文件，如果没有，再查找当前项目
				val name = sourceElement.name
				findScriptVariableInFile(name, sourceElement.containingFile)?.toSingletonArray()?.let { return it }
				findScriptVariables(name, sourceElement.project).toTypedArray()
			}
			//字符串可以是脚本文件属性，也可以是本地化文件属性
			is ParadoxScriptString -> {
				//查找当前项目的本地化文件属性，如果没有，再查找当前项目的本地化文件属性
				val name = sourceElement.value
				val project = sourceElement.project
				return findScriptProperties(name, project).ifEmpty {
					findLocalisationPropertiesOrAll(name, inferredParadoxLocale, project) //优先查找推断的语言区域
				}.toTypedArray()
			}
			else -> null
		}
	}
}
