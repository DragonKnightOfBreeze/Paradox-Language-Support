package icu.windea.pls.inject.injectors

import icu.windea.pls.core.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import icu.windea.pls.integrations.images.*
import java.awt.image.*
import javax.imageio.*
import javax.imageio.stream.*

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
            val stream = imageInput ?: return@run
            val image = PlsImageManager.read(imageIndex, param, stream, "tga", "png") ?: return@run
            return image
        }

        continueInvocation()
    }
}

