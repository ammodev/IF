package com.github.stefvanschie.inventoryframework.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A utility class for reading csv files
 *
 * @since 0.5.0
 */
object CSVUtil {
    private val UNICODE_CHARACTER_PATTERN: Pattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})")

    /**
     * Reads the entire file and returns it as a list of strings.
     *
     * @param inputStream the input stream to read from
     * @return a list of strings containing the values inside the file
     * @throws IOException when reading fails for any reason
     * @since 0.5.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readAll(inputStream: InputStream): List<Array<String?>> {
        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            val strings: MutableList<Array<String?>> = ArrayList()
            var line: String

            while ((reader.readLine().also { line = it }) != null) {
                if (line.isEmpty()) {
                    continue
                }

                val splittingIndices: MutableList<Int> = ArrayList()
                val chars = line.toCharArray()
                var quote = false

                for (i in chars.indices) {
                    if (chars[i] == '"') {
                        quote = !quote
                    } else if (chars[i] == ',' && !quote) {
                        splittingIndices.add(i)
                    }
                }

                val array = arrayOfNulls<String>(splittingIndices.size + 1)

                for (i in 0 until splittingIndices.size + 1) {
                    array[i] = line.substring(
                        if (i - 1 < 0) 0 else splittingIndices[i - 1] + 1,
                        if (i == splittingIndices.size) line.length else splittingIndices[i]
                    )
                }

                for (i in array.indices) {
                    array[i] = array[i]!!.trim { it <= ' ' }

                    if (array[i]!!.startsWith("\"") && array[i]!!.endsWith("\"")) {
                        array[i] = array[i]!!.substring(1, array[i]!!.length - 1)
                    }

                    array[i] = array[i]!!.replace("\"\"", "\"")

                    //replace unicode characters
                    val matcher = UNICODE_CHARACTER_PATTERN.matcher(array[i])
                    val buf = StringBuffer(array[i]!!.length)

                    while (matcher.find()) {
                        val character = (matcher.group(1).toInt(16) as Char).toString()
                        matcher.appendReplacement(buf, Matcher.quoteReplacement(character))
                    }

                    matcher.appendTail(buf)

                    array[i] = buf.toString()
                }

                strings.add(array)
            }
            return strings
        }
    }
}
