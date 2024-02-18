package icu.windea.pls.inject.injectors

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.inject.*
import icu.windea.pls.util.image.*
import org.intellij.images.editor.*
import org.intellij.images.vfs.*
import java.io.*
import java.lang.ref.*
import javax.imageio.*

/**
 * 用于支持直接在IDE的编辑器中渲染DDS图片。
 */
@InjectTarget("org.intellij.images.vfs.IfsUtil", pluginId = "com.intellij.platform.images")
class IfsUtilCodeInjector : CodeInjectorBase() {
    //org.intellij.images.vfs.IfsUtil
    //org.intellij.images.vfs.IfsUtil.refresh
    
    //即使目标DDS文件不存在于本地（例如来自Git提交记录），也可以正常渲染
    
    //这里必须懒加载，不能在初始化代码注入器时就加载IfsUtil
    private val TIME_MODIFICATION_STAMP_KEY by lazy { staticProperty<IfsUtil, Key<Pair<Long?, Long?>>>("TIME_MODIFICATION_STAMP_KEY").get() }
    private val FORMAT_KEY by lazy { staticProperty<IfsUtil, Key<String>>("FORMAT_KEY").get() }
    private val IMAGE_PROVIDER_REF_KEY by lazy { staticProperty<IfsUtil, Key<SoftReference<ImageDocument.ScaledImageProvider>>>("IMAGE_PROVIDER_REF_KEY").get() }
    
    @InjectMethod(InjectMethod.Pointer.AFTER, static = true)
    fun refresh(file: VirtualFile, returnValue: Boolean): Boolean {
        if(returnValue) return true
        if(file.fileType != DdsFileType) return false
        
        val loadedTimeModificationStamp = file.getUserData(TIME_MODIFICATION_STAMP_KEY)
        val actualTimeModificationStamp = Pair.create(file.timeStamp, file.modificationStamp)
        val imageProviderRef = file.getUserData(IMAGE_PROVIDER_REF_KEY)
        if(actualTimeModificationStamp != loadedTimeModificationStamp || com.intellij.reference.SoftReference.dereference(imageProviderRef) == null) {
            try {
                file.putUserData(IMAGE_PROVIDER_REF_KEY, null)
                
                //convert dds bytes to png bytes
                val bytes = ImageManager.convertDdsToPng(file.inputStream) ?: return false
                val inputStream = ByteArrayInputStream(bytes)
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