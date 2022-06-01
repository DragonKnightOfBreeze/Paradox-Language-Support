package icu.windea.pls.core.quickFix

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.ui.configuration.*
import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 导入游戏目录或模组目录的快速修复。
 */
class ImportGameOrModDirectoryFix(
	element: PsiElement
) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
	override fun getFamilyName() = PlsBundle.message("core.quickFix.importGameOrModDirectory")
	
	override fun getText() = PlsBundle.message("core.quickFix.importGameOrModDirectory")
	
	//https://intellij-support.jetbrains.com/hc/en-us/community/posts/206141379-Showing-Project-Strucuture-dialog-programmatically
	
	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
		//打开Project Structure > Project Settings，接着打开模块的库设置，如果不行，则打开全局的库设置
		//库需要被添加到模块才能真正生效，并显示到Project > External Libraries中
		val projectSettingsService = ProjectSettingsService.getInstance(project)
		if(projectSettingsService.canOpenModuleDependenciesSettings()) {
			val module = ModuleUtilCore.findModuleForPsiElement(file)
			if(module != null) {
				projectSettingsService.openModuleLibrarySettings(module)
				return
			}
		}
		projectSettingsService.openGlobalLibraries()
	}
	
	override fun startInWriteAction() = false
	
	override fun availableInBatchMode() = false
}