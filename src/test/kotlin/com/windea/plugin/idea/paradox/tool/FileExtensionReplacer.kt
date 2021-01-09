package com.windea.plugin.idea.paradox.tool

import java.io.*

fun main() {
	val rootPath = "paradox-language-support/src/main/resources/rules/stellaris"
	val extension = "txt"
	val newExtension = "yml"
	replaceFileExtension(rootPath,extension,newExtension)
}

fun replaceFileExtension(rootPath:String,extension:String,newExtension:String){
	val rootFile = File(rootPath)
	rootFile.walk().forEach {
		if(it.isFile){
			it.renameTo(File(it.path.replace(".$extension",".$newExtension")))
		}
	}
}
