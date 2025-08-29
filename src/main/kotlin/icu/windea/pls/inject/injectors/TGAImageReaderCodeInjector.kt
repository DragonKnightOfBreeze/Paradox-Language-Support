package icu.windea.pls.inject.injectors

import icu.windea.pls.core.memberProperty
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectTarget
import icu.windea.pls.integrations.images.PlsImageManager
import java.awt.image.BufferedImage
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream

/**
 * @see icu.windea.pls.images.spi.TgaImageReaderSpi
 * @see com.twelvemonkeys.imageio.plugins.tga.TGAImageReader
 * @see com.twelvemonkeys.imageio.plugins.tga.TGAImageReader.read
 */
@InjectTarget("com.twelvemonkeys.imageio.plugins.tga.TGAImageReader", pluginId = "icu.windea.pls")
class TGAImageReaderCodeInjector : CodeInjectorBase() {
    private val Any.imageInput: ImageInputStream? by memberProperty("imageInput", null)

    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE)
    fun Any.read(imageIndex: Int, param: ImageReadParam?): BufferedImage {
        run {
            val stream = this.imageInput ?: return@run
            val image = PlsImageManager.read(imageIndex, param, stream, "tga", "png") ?: return@run
            return image
        }

        continueInvocation()
    }
}

