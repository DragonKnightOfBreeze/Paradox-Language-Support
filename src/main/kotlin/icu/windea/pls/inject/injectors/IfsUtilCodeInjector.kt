package icu.windea.pls.inject.injectors

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.reference.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.inject.*
import icu.windea.pls.tool.*
import org.intellij.images.editor.*
import org.intellij.images.vfs.*
import javax.imageio.*

/**
 * 用于支持直接在IDE的编辑器中渲染DDS图片。
 */
@InjectTarget("org.intellij.images.vfs.IfsUtil", pluginId = "com.intellij.platform.images")
@Suppress("UNCHECKED_CAST")
class IfsUtilCodeInjector : BaseCodeInjector() {
    //org.intellij.images.vfs.IfsUtil
    //org.intellij.images.vfs.IfsUtil.refresh
    
    companion object {
        val TIME_MODIFICATION_STAMP_KEY by lazy { staticProperty<IfsUtil>("TIME_MODIFICATION_STAMP_KEY") as Key<Pair<Long?, Long?>> }
        val FORMAT_KEY by lazy { staticProperty<IfsUtil>("FORMAT_KEY") as Key<String> }
        val IMAGE_PROVIDER_REF_KEY by lazy { staticProperty<IfsUtil>("IMAGE_PROVIDER_REF_KEY") as Key<SoftReference<ImageDocument.ScaledImageProvider>> }
    }
    
    @Inject(Inject.Pointer.AFTER)
    fun refresh(file: VirtualFile, returnValue: Boolean): Boolean {
        if(returnValue) return true
        if(file.fileType != DdsFileType) return false
        
        val loadedTimeModificationStamp = file.getUserData(TIME_MODIFICATION_STAMP_KEY)
        val actualTimeModificationStamp = Pair.create(file.timeStamp, file.modificationStamp)
        val imageProviderRef = file.getUserData(IMAGE_PROVIDER_REF_KEY)
        if(actualTimeModificationStamp != loadedTimeModificationStamp || SoftReference.dereference(imageProviderRef) == null) {
            try {
                file.putUserData(IMAGE_PROVIDER_REF_KEY, null)
                
                //convert dds bytes to png bytes
                val inputStream = DdsConverter.convertBytes(file) ?: return false
                val imageInputStream = ImageIO.createImageInputStream(inputStream)
                imageInputStream.use {
                    val imageReaders = ImageIO.getImageReadersByFormatName("png")
                    if(imageReaders.hasNext()) {
                        val imageReader = imageReaders.next()
                        return try {
                            file.putUserData(FORMAT_KEY, imageReader.formatName)
                            val param = imageReader.defaultReadParam
                            imageReader.setInput(imageInputStream, true, true)
                            val minIndex = imageReader.minIndex
                            val image = imageReader.read(minIndex, param)
                            file.putUserData(IMAGE_PROVIDER_REF_KEY, SoftReference(ImageDocument.ScaledImageProvider { _, _ -> image }))
                            true
                        } finally {
                            imageReader.dispose()
                        }
                    }
                }
            } finally {
                // We perform loading no more needed
                file.putUserData(TIME_MODIFICATION_STAMP_KEY, actualTimeModificationStamp)
            }
        }
        
        return false
    }
}