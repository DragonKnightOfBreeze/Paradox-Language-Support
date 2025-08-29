package icu.windea.pls.images.spi

import com.twelvemonkeys.imageio.plugins.tga.TGAImageReaderSpi

/**
 * @see icu.windea.pls.inject.injectors.TGAImageReaderCodeInjector
 */
class TgaImageReaderSpi : DelegatedImageReaderSpi(TGAImageReaderSpi())
