package icu.windea.pls.core.index

import com.intellij.openapi.util.registry.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import java.util.*

object ParadoxFilePathIndex : ScalarIndexExtension<String>() {
	private val name = ID.create<String, Void>("paradox.file.path.index")
	
	override fun getName(): ID<String, Void> {
		return name
	}
	
	override fun getIndexer(): DataIndexer<String, Void, FileContent> {
		return DataIndexer { inputData ->
			val path = inputData.file.fileInfo?.path?.path
			if(path == null) emptyMap() else Collections.singletonMap(path, null)
		}
	}
	
	override fun getKeyDescriptor(): KeyDescriptor<String> {
		return EnumeratorStringDescriptor.INSTANCE
	}
	
	override fun getVersion(): Int {
		return 3 + (if(Registry.`is`("indexing.paradox.file.path.over.vfs")) 0xff else 0)
	}
	
	override fun getInputFilter(): FileBasedIndex.InputFilter {
		return FileBasedIndex.InputFilter { it.fileInfo != null }
	}
	
	override fun dependsOnFileContent(): Boolean {
		return false
	}
}