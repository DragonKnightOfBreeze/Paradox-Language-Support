package com.windea.plugin.idea.paradox.tool

import com.windea.plugin.idea.paradox.*
import java.io.*
import java.util.concurrent.*

object ModOverriddenFileSelector {
	private val ignoredFileShortNames = arrayOf("README", "CHANGELOG", "CREDITS")
	
	/**
	 * 根据指定的游戏路径和一组模组路径，以及保存路径，找出与原版有冲突的脚本文件，统一保存到该保存路径中。
	 * 默认要求至少2个mod的文件与原版有冲突才会保存。
	 */
	fun selectOverriddenFiles(gamePath: String, modPaths: List<String>, targetPath: String, minSize: Int = 2) {
		doSelectOverriddenFiles(gamePath, modPaths, targetPath, minSize)
	}
	
	private fun doSelectOverriddenFiles(gamePath: String, modPaths: List<String>, targetPath: String, minSize: Int) {
		val gameFile = File(gamePath)
		val modDirs = modPaths.map { modPath -> File(modPath) }
		val savedFile = File(targetPath)
		
		println("Prepare vanilla files...")
		val gameScriptFiles = gameFile.walk().filter { it.isFile && it.extension == "txt" }.map { it.relativeTo(gameFile) }
		val gameScriptFileMap = gameScriptFiles.associateBy { it.path }
		val savedScriptFileMap = ConcurrentHashMap<File, CopyOnWriteArrayList<Pair<File, File>>>()
		
		println("Prepare mod names...")
		val modNameMap = modDirs.associateWith { it.getModName() }
		
		println("Add overridden files...")
		for(modDir in modDirs) {
			val modScriptFiles = modDir.walk().filter { it.isFile && it.extension == "txt" }.map { it.relativeTo(modDir) }
			for(modScriptFile in modScriptFiles) {
				val shortName = modScriptFile.nameWithoutExtension
				//如果找到了有冲突的文件，则添加到缓存中
				//排除readme和changelog和credits
				if(shortName.toUpperCase() !in ignoredFileShortNames) {
					val key = modScriptFile.path
					val gameScriptFile = gameScriptFileMap[key]
					//排除不存在的情况
					if(gameScriptFile != null) {
						println("Add overridden file '${modScriptFile.name}'.")
						savedScriptFileMap.getOrPut(gameScriptFile) { CopyOnWriteArrayList() }.add(modDir to modScriptFile)
					}
				}
			}
		}
		
		println("Select overridden files...")
		val fqSavedScriptFileMap = savedScriptFileMap.filterValues { v -> v.size >= minSize }
		val size = fqSavedScriptFileMap.size
		
		println("Save overridden files...")
		for((gameScriptFile, modScriptFileMap) in fqSavedScriptFileMap) {
			gameFile.resolve(gameScriptFile).copyTo(savedFile.resolve(gameScriptFile.path))
			for((modFile, modScriptFile) in modScriptFileMap) {
				val marker = modNameMap[modFile] ?: modFile.name
				modFile.resolve(modScriptFile).copyTo(savedFile.resolve(modScriptFile.path.handlePath(marker)))
			}
		}
		
		println("Save overridden files of $size groups in target directory '$targetPath'")
	}
	
	//找到当前模组目录的模组名字
	private fun File.getModName(): String? {
		return try {
			this.resolve("descriptor.mod").useLines {
				it.map { l -> l.trim() }.find { l -> l.startsWith("name") }
					?.substringAfter("=")?.trim()?.unquote()?.replace(":", "")
			}
		} catch(e: Exception) {
			null
		}
	}
	
	//处理原始的文件路径
	private fun String.handlePath(marker: Any): String {
		val separatorIndex = this.lastIndexOf('.')
		return when {
			separatorIndex == -1 -> this + marker
			else -> {
				this.take(separatorIndex) + "__" + marker + this.substring(separatorIndex, this.length)
			}
		}
	}
}
