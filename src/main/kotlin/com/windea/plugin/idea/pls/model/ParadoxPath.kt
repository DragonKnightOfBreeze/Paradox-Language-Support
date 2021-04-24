package com.windea.plugin.idea.pls.model

data class ParadoxPath(
	val subpaths:List<String>
){
	val path = subpaths.joinToString("/")
	val fileName = subpaths.lastOrNull().orEmpty()
	val fileExtension = fileName.substringAfterLast('.')
	val parentSubpaths = subpaths.dropLast(1)
	val parent = parentSubpaths.joinToString("/")
	val root = parentSubpaths.firstOrNull().orEmpty()
	val length = subpaths.size
	val parentLength = parentSubpaths.size
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxPath && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}
