package icu.windea.pls.images.spi

import com.twelvemonkeys.imageio.plugins.tga.*

/**
 * @see icu.windea.pls.inject.injectors.TGAImageReaderCodeInjector
 */
class TgaImageReaderSpi : DelegatedImageReaderSpi(TGAImageReaderSpi())
