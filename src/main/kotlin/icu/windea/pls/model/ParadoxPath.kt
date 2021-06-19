package icu.windea.pls.model

data class ParadoxPath(
	val subPaths:List<String>
):Iterable<String>{
	val path = subPaths.joinToString("/")
	val fileName = subPaths.lastOrNull().orEmpty()
	val fileExtension = fileName.substringAfterLast('.')
	val parentsubPaths = subPaths.dropLast(1)
	val parent = parentsubPaths.joinToString("/")
	val root = parentsubPaths.firstOrNull().orEmpty()
	val length = subPaths.size
	val parentLength = parentsubPaths.size
	
	val size = subPaths.size
	
	fun isEmpty() :Boolean{
		return size == 0
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
