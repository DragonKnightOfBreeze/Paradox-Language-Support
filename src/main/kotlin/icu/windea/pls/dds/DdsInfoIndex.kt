package icu.windea.pls.dds

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.util.image.*
import org.intellij.images.util.*
import java.io.*
import java.lang.invoke.*

//org.intellij.images.index.ImageInfoIndex

object DdsInfoIndex {
	private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
	
	private val valueExternalizer: DataExternalizer<DdsInfo> = object : DataExternalizer<DdsInfo> {
		override fun save(storage: DataOutput, info: DdsInfo) {
			DataInputOutputUtil.writeINT(storage, info.width)
			DataInputOutputUtil.writeINT(storage, info.height)
		}
		
		override fun read(storage: DataInput): DdsInfo {
			val width = DataInputOutputUtil.readINT(storage)
			val height = DataInputOutputUtil.readINT(storage)
			return DdsInfo(width, height)
		}
	}
	
	private val gist: VirtualFileGist<DdsInfo> = GistManager.getInstance().newVirtualFileGist("DdsInfo", 1, valueExternalizer) { _, file ->
		if(!file.isInLocalFileSystem) return@newVirtualFileGist null
		val fileType = file.fileType
		if(fileType != DdsFileType) return@newVirtualFileGist null
		//直接委托给ImageInfoReader
		val pngFile = ParadoxImageResolver.getPngFile(file) ?: return@newVirtualFileGist null
		val content = try {
			pngFile.contentsToByteArray()
		} catch(e: IOException) {
			logger.error(e.message, e)
			return@newVirtualFileGist null
		}
		val info = ImageInfoReader.getInfo(content) ?: return@newVirtualFileGist null
		DdsInfo(info.width, info.height)
	}
	
	fun getInfo(file: VirtualFile, project: Project): DdsInfo? {
		return gist.getFileData(project, file)
	}
}