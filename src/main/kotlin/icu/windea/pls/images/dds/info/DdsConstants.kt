package icu.windea.pls.images.dds.info

object DdsConstants {
    // DdsInfo.dwMagic
    val DDS_MAGIC: Int = make4CC("DDS ")
    // DdsHeader.dwSize
    const val HEADER_SIZE = 124
    // DdsPixelFormat.dwSize
    const val PIXEL_FORMAT_SIZE = 32

    // DdsHeader.dwFlags
    // const val DDSD_CAPS: Int = 0x1
    // const val DDSD_HEIGHT: Int = 0x2
    // const val DDSD_WIDTH: Int = 0x4
    // const val DDSD_PITCH: Int = 0x8
    // const val DDSD_PIXELFORMAT: Int = 0x1000
    // const val DDSD_MIPMAPCOUNT: Int = 0x20000
    // const val DDSD_LINEARSIZE: Int = 0x80000
    // const val DDSD_DEPTH: Int = 0x800000
    // const val DDS_HEADER_FLAGS_TEXTURE: Int = DDSD_CAPS or DDSD_HEIGHT or DDSD_WIDTH or DDSD_PIXELFORMAT
    // const val DDS_HEADER_FLAGS_MIPMAP: Int = DDSD_MIPMAPCOUNT
    // const val DDS_HEADER_FLAGS_VOLUME: Int = DDSD_DEPTH
    // const val DDS_HEADER_FLAGS_PITCH: Int = DDSD_PITCH
    // const val DDS_HEADER_FLAGS_LINEARSIZE: Int = DDSD_LINEARSIZE

    // DdsHeader.dwCaps
    // const val DDSCAPS_COMPLEX: Int = 0x8
    // const val DDSCAPS_MIPMAP: Int = 0x400000
    // const val DDSCAPS_TEXTURE: Int = 0x1000
    // const val DDS_SURFACE_FLAGS_MIPMAP: Int = DDSCAPS_COMPLEX or DDSCAPS_MIPMAP
    // const val DDS_SURFACE_FLAGS_TEXTURE: Int = DDSCAPS_TEXTURE
    // const val DDS_SURFACE_FLAGS_CUBEMAP: Int = DDSCAPS_COMPLEX

    // DdsHeader.dwCaps2
    // const val DDSCAPS2_CUBEMAP: Int = 0x200
    // const val DDSCAPS2_CUBEMAP_POSITIVEX: Int = 0x400
    // const val DDSCAPS2_CUBEMAP_NEGATIVEX: Int = 0x800
    // const val DDSCAPS2_CUBEMAP_POSITIVEY: Int = 0x1000
    // const val DDSCAPS2_CUBEMAP_NEGATIVEY: Int = 0x2000
    // const val DDSCAPS2_CUBEMAP_POSITIVEZ: Int = 0x4000
    // const val DDSCAPS2_CUBEMAP_NEGATIVEZ: Int = 0x8000
    // const val DDSCAPS2_VOLUME: Int = 0x200000
    // const val DDS_CUBEMAP_POSITIVEX: Int = DDSCAPS2_CUBEMAP or DDSCAPS2_CUBEMAP_POSITIVEX
    // const val DDS_CUBEMAP_NEGATIVEX: Int = DDSCAPS2_CUBEMAP or DDSCAPS2_CUBEMAP_NEGATIVEX
    // const val DDS_CUBEMAP_POSITIVEY: Int = DDSCAPS2_CUBEMAP or DDSCAPS2_CUBEMAP_POSITIVEY
    // const val DDS_CUBEMAP_NEGATIVEY: Int = DDSCAPS2_CUBEMAP or DDSCAPS2_CUBEMAP_NEGATIVEY
    // const val DDS_CUBEMAP_POSITIVEZ: Int = DDSCAPS2_CUBEMAP or DDSCAPS2_CUBEMAP_POSITIVEZ
    // const val DDS_CUBEMAP_ALLFACES: Int = DDS_CUBEMAP_POSITIVEX or DDS_CUBEMAP_NEGATIVEX or DDS_CUBEMAP_POSITIVEY or DDS_CUBEMAP_NEGATIVEY or DDS_CUBEMAP_POSITIVEZ or DDSCAPS2_CUBEMAP_NEGATIVEZ
    // const val DDS_CUBEMAP_NEGATIVEZ: Int = DDSCAPS2_CUBEMAP or DDSCAPS2_CUBEMAP_NEGATIVEZ
    // const val DDS_FLAGS_VOLUME: Int = DDSCAPS2_VOLUME

    // DdsPixelFormat.dwFlags
    const val DDPF_ALPHAPIXELS: Int = 0x1
    const val DDPF_ALPHA: Int = 0x2
    const val DDPF_FOURCC: Int = 0x4
    const val DDPF_RGB: Int = 0x40
    // const val DDPF_YUV: Int = 0x200
    const val DDPF_LUMINANCE: Int = 0x20000
    const val DDS_RGBA: Int = DDPF_RGB or DDPF_ALPHAPIXELS

    // DdsPixelFormat.dwFourCC
    // val D3DFMT_DXT1: Int = make4CC("DXT1")
    // val D3DFMT_DXT2: Int = make4CC("DXT2")
    // val D3DFMT_DXT3: Int = make4CC("DXT3")
    // val D3DFMT_DXT4: Int = make4CC("DXT4")
    // val D3DFMT_DXT5: Int = make4CC("DXT5")
    val DDS_DX10: Int = make4CC("DX10")
    // val DXGI_FORMAT_BC4_UNORM: Int = make4CC("BC4U")
    // val DXGI_FORMAT_BC4_SNORM: Int = make4CC("BC4S")
    // val DXGI_FORMAT_BC5_UNORM: Int = make4CC("ATI2")
    // val DXGI_FORMAT_BC5_SNORM: Int = make4CC("BC5S")
    // val D3DFMT_R8G8_B8G8: Int = make4CC("RGBG")
    // val D3DFMT_G8R8_G8B8: Int = make4CC("GRGB")
    // val D3DFMT_UYVY: Int = make4CC("UYVY")
    // val D3DFMT_YUY2: Int = make4CC("YUY2")
    // val D3DFMT_MULTI2_ARGB8: Int = make4CC("MET1")

    // DdsHeaderDxt10.miscFlags
    // const val D3D10_RESOURCE_MISC_GENERATE_MIPS: Int = 0x1
    // const val D3D10_RESOURCE_MISC_SHARED: Int = 0x2
    // const val D3D10_RESOURCE_MISC_TEXTURECUBE: Int = 0x4
    // const val D3D10_RESOURCE_MISC_SHARED_KEYEDMUTEX: Int = 0x10
    // const val D3D10_RESOURCE_MISC_GDI_COMPATIBLE: Int = 0x20
    // const val DDS_RESOURCE_MISC_TEXTURECUBE: Int = D3D10_RESOURCE_MISC_TEXTURECUBE

    // DdsHeaderDxt10.miscFlags2
    // const val DDS_ALPHA_MODE_UNKNOWN: Int = 0x0
    // const val DDS_ALPHA_MODE_STRAIGHT: Int = 0x1
    // const val DDS_ALPHA_MODE_PREMULTIPLIED: Int = 0x2
    // const val DDS_ALPHA_MODE_OPAQUE: Int = 0x3
    // const val DDS_ALPHA_MODE_CUSTOM: Int = 0x4
    // const val DDS_MISC_FLAGS2_ALPHA_MODE_MASK: Int = 0x7

    fun getStringFrom4CC(v: Int): String {
        val a = (v and 0xFF).toChar()
        val b = (v shr 8 and 0xFF).toChar()
        val c = (v shr 16 and 0xFF).toChar()
        val d = (v shr 24 and 0xFF).toChar()
        return "$a$b$c$d"
    }

    fun make4CC(s: String): Int {
        require(s.length == 4) { "expected string with length 4" }
        return s.get(0).code or (s.get(1).code shl 8) or (s.get(2).code shl 16) or (s.get(3).code shl 24)
    }
}
