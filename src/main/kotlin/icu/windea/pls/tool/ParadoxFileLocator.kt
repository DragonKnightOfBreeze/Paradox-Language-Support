package icu.windea.pls.tool

import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*

object ParadoxFileLocator {
	const val scriptedVariablesPath = "common/scripted_variables"
	
	private fun scriptedVariablesFileName(prefix: String) = "${prefix}scripted_variables.txt"
	
	fun getRootFile(context: VirtualFile): VirtualFile? {
		return context.fileInfo?.rootFile
	}
	
	fun getScriptedVariablesDirectory(context: VirtualFile): VirtualFile? {
		val root = getRootFile(context) ?: return null
		VfsUtil.createDirectoryIfMissing(root, scriptedVariablesPath)
		return root.findFileByRelativePath(scriptedVariablesPath)
	}
	
	fun getGeneratedFileName(directory: VirtualFile): VirtualFile? {
		if(!directory.isDirectory) return null
		val directoryPath = directory.fileInfo?.path ?: return null
		if(scriptedVariablesPath.matchesPath(directoryPath.path)) {
			val fileName = scriptedVariablesFileName(getSettings().generation.fileNamePrefix.orEmpty())
			return directory.findOrCreateChildData(ParadoxFileLocator, fileName)
		}
		return null
	}
}
