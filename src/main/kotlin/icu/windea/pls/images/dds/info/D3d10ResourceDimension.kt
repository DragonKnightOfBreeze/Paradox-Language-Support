package icu.windea.pls.images.dds.info

/**
 * 标识正在使用的资源的类型。
 *
 * 参见：[D3D10_RESOURCE_DIMENSION (d3d10.h) - Win32 apps | Microsoft Learn](https://learn.microsoft.com/windows/win32/api/d3d10/ne-d3d10-d3d10_resource_dimension)
 */
enum class D3d10ResourceDimension(val value: Int) {
    UNKNOWN(0),
    BUFFER(1),
    TEXTURE1D(2),
    TEXTURE2D(3),
    TEXTURE3D(4),
    ;

    companion object {
        @JvmStatic
        private val map = entries.associateBy { it.value }

        @JvmStatic
        fun get(value: Int) = map.get(value) ?: throw NoSuchElementException("unknown resource dimension from value $value")
    }
}
