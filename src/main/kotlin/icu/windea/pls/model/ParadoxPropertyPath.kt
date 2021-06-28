package icu.windea.pls.model

class ParadoxPropertyPath(
	val subPaths:List<String>,
	val subPathInfos:List<ParadoxPropertyPathInfo>
):Iterable<String>{
	val length = subPaths.size
	val parentSubPaths = subPaths.dropLast(1)
	val path = subPaths.joinToString("/")
	val parent = parentSubPaths.joinToString("/")
	val originalPath = subPathInfos.joinToString("/"){ if(it.quoted) "\"${it.value}\"" else it.value }
	
	fun isEmpty() :Boolean{
		return length == 0
	}
	
	override fun iterator(): Iterator<String> {
		return subPaths.iterator()
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxPropertyPath && originalPath == other.originalPath
	}
	
	override fun hashCode(): Int {
		return originalPath.hashCode()
	}
	
	override fun toString(): String {
		return originalPath
	}
}