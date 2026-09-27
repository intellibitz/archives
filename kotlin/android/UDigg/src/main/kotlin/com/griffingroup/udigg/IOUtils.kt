package com.griffingroup.udigg

import java.io.InputStream
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset

object IOUtils {
    private const val DEFAULT_BUFFER_SIZE = 1024 * 4

    @JvmStatic
    fun toString(input: InputStream, encoding: String? = null): String {
        return if (encoding != null) {
            input.bufferedReader(Charset.forName(encoding)).use { it.readText() }
        } else {
            input.bufferedReader().use { it.readText() }
        }
    }

    @JvmStatic
    fun copy(input: InputStream, output: Writer, encoding: String? = null) {
        val reader = if (encoding != null) {
            input.bufferedReader(Charset.forName(encoding))
        } else {
            input.bufferedReader()
        }
        copy(reader, output)
    }

    @JvmStatic
    fun copy(input: Reader, output: Writer): Long {
        val buffer = CharArray(DEFAULT_BUFFER_SIZE)
        var count = 0L
        var n: Int
        while (input.read(buffer).also { n = it } != -1) {
            output.write(buffer, 0, n)
            count += n
        }
        return count
    }
}
