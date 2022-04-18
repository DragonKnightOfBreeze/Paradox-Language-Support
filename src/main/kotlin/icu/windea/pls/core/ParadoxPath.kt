package icu.windea.pls.core

class ParadoxPath(
	val subPaths:List<String>
):Iterable<String>{
	val length = subPaths.size
	val parentSubPaths = subPaths.dropLast(1)
	val path = subPaths.joinToString("/")
	val parent = parentSubPaths.joinToString("/")
	val root = parentSubPaths.firstOrNull().orEmpty()
	val fileName = subPaths.lastOrNull().orEmpty()
	val fileExtension = fileName.substringAfterLast('.')
	
	fun isEmpty() :Boolean{
		return length == 0
	}
	
	override fun iterator(): Iterator<String> {
		return subPaths.iterator()
	}
	
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
