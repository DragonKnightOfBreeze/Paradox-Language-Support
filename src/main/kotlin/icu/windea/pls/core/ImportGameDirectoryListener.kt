package icu.windea.pls.core

import com.intellij.openapi.project.*

/**
 * 当用户打开项目时，如果检测到项目中包含模组文件夹，但未将对应游戏类型的游戏目录作为库添加到模组文件夹对应的项目或模块中，则要求用户导入。
 */
class ImportGameDirectoryListener: ProjectManagerListener {
	override fun projectOpened(project: Project) {
		//需要延迟通知
		//TODO
	}
}