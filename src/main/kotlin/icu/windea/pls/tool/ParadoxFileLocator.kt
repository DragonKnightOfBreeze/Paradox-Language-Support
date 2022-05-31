package icu.windea.pls.tool

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.*

object ParadoxFileLocator {
	const val scriptedVariablesPath = "common/scripted_variables"
	
	fun getRootFile(context: VirtualFile): VirtualFile? {
		return context.fileInfo?.root
	}
	
	fun getScriptedVariablesDirectory(context: VirtualFile): VirtualFile?{
		val root = getRootFile(context) ?: return null
		VfsUtil.createDirectoryIfMissing(root, scriptedVariablesPath)
		return root.findFileByRelativePath(scriptedVariablesPath)
	}
}