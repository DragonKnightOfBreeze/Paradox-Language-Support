package icu.windea.pls.core

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.core.model.*

/**
 * 监听描述符文件或者游戏类型标记文件。
 * 如果文件内容发生变化、或者文件被创建、被删除或者被移动，导致有必要重新解析相关的文件，则要重新解析。
 */
class DescriptorOrMarkerFileListener : AsyncFileListener {
	override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier {
		return object : AsyncFileListener.ChangeApplier {
			override fun afterVfsChange() {
				for(event in events) {
					doReparse(event)
				}
			}
		}
	}
	
	private fun doReparse(event: VFileEvent) {
		//描述符文件被删除或者被移动
		//模组根目录的游戏类型标记文件被删除或者被移动
		//游戏类型变更，也要重新解析，因为folders配置可能会变更
		when {
			event is VFileCreateEvent -> {
				val fileName = event.childName
				if(!isPossibleDescriptorOrMarkerFile(fileName)) return
				val rootFile = event.parent
				val oldRootInfo = rootFile.getUserData(PlsKeys.rootInfoKey)
				if(oldRootInfo != null){
					ParadoxRootInfo.values.remove(oldRootInfo)
					rootFile.putUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = resolveRootInfo(rootFile) ?: return
				if(fileName == rootInfo.descriptorFile.name) {
					reparseFilesInRoot(rootFile)
				} else if(fileName == rootInfo.markerFile?.name && rootInfo.gameType != oldRootInfo?.gameType) {
					reparseFilesInRoot(rootFile)
				}
			}
			event is VFileDeleteEvent -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				reparseFilesInRoot(file.parent) //不能从file.parent.children中获取file，因为已经不合法
			}
			event is VFileMoveEvent -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				reparseFilesInRoot(event.oldParent)
				val rootFile = event.newParent
				val oldRootInfo = rootFile.getUserData(PlsKeys.rootInfoKey)
				if(oldRootInfo != null){
					ParadoxRootInfo.values.remove(oldRootInfo)
					rootFile.putUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = resolveRootInfo(rootFile) ?: return
				if(file == rootInfo.descriptorFile) {
					reparseFilesInRoot(rootFile)
				} else if(file == rootInfo.markerFile && rootInfo.gameType != oldRootInfo?.gameType) {
					reparseFilesInRoot(rootFile)
				}
			}
			event is VFileContentChangeEvent -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				val rootFile = file.parent ?: return
				val oldFileInfo = rootFile.getUserData(PlsKeys.rootInfoKey)
				if(oldFileInfo != null){
					ParadoxRootInfo.values.remove(oldFileInfo)
					rootFile.putUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = resolveRootInfo(rootFile) ?: return
				if(file == rootInfo.descriptorFile) {
					file.putUserData(PlsKeys.descriptorInfoKey, null) //清空描述符信息缓存
				} else if(file == rootInfo.markerFile && file.name == launcherSettingsFileName) {
					reparseFilesInRoot(rootFile) //这种情况下也需要重新解析
				}
			}
			event is VFilePropertyChangeEvent && event.propertyName == "name" -> {
				val file = event.file
				if(!isPossibleDescriptorOrMarkerFile(file.name)) return
				val rootFile = file.parent
				val oldRootInfo = rootFile.getUserData(PlsKeys.rootInfoKey)
				if(oldRootInfo != null){
					ParadoxRootInfo.values.remove(oldRootInfo)
					rootFile.putUserData(PlsKeys.rootInfoKey, null)
				}
				val rootInfo = resolveRootInfo(rootFile) ?: return
				if(file == rootInfo.descriptorFile) {
					reparseFilesInRoot(rootFile)
				} else if(file == rootInfo.markerFile && rootInfo.gameType != oldRootInfo?.gameType) {
					reparseFilesInRoot(rootFile)
				}
			}
		}
	}
	
	private fun isPossibleDescriptorOrMarkerFile(fileName: String): Boolean {
		return when {
			fileName.equals(descriptorFileName, true) -> true
			fileName.equals(launcherSettingsFileName, true) -> true
			fileName.startsWith('.') -> ParadoxGameType.resolve(fileName.drop(1)) != null
			else -> false
		}
	}
}