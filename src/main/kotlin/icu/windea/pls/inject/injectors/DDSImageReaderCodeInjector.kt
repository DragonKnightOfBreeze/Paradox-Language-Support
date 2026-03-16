package icu.windea.pls.inject.injectors

import icu.windea.pls.core.memberProperty
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.integrations.images.ImageIntegrationManager
import java.awt.image.BufferedImage
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream

/**
 * @see icu.windea.pls.images.spi.DdsImageReaderSpi
 * @see com.twelvemonkeys.imageio.plugins.dds.DDSImageReader
 * @see com.twelvemonkeys.imageio.plugins.dds.DDSImageReader.read
 */
@InjectionTarget("com.twelvemonkeys.imageio.plugins.dds.DDSImageReader", pluginId = "icu.windea.pls")
class DDSImageReaderCodeInjector : CodeInjectorBase() {
    private val Any.imageInput: ImageInputStream? by memberProperty("imageInput", null)

    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE)
    fun Any.read(imageIndex: Int, param: ImageReadParam?): BufferedImage {
        runSafely r@{
            val stream = this.imageInput ?: return@r
            val image = ImageIntegrationManager.read(imageIndex, param, stream, "dds", "png") ?: return@r
            return image
        }
        continueInvocation()
    }
}
