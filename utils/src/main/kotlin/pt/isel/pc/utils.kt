package pt.isel.pc

import java.io.BufferedWriter

fun BufferedWriter.writeLine(format: String, vararg values : Any?) {
    write(String.format(format, *values))
    newLine()
    flush()
}