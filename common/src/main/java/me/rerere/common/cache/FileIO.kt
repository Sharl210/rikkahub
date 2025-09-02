package me.rerere.common.cache

import java.io.File
import java.io.IOException

internal fun ensureParentDir(file: File) {
    val parent = file.parentFile
    if (parent != null && !parent.exists()) {
        if (!parent.mkdirs() && !parent.exists()) {
            throw IOException("Failed to create directory: $parent")
        }
    }
}

internal fun atomicWrite(file: File, content: String) {
    ensureParentDir(file)
    val tmp = File(file.parentFile, file.name + ".tmp")
    tmp.writeText(content)
    if (file.exists()) {
        if (!tmp.renameTo(file)) {
            if (file.delete()) {
                if (!tmp.renameTo(file)) {
                    throw IOException("Failed to replace $file with temp file")
                }
            } else {
                throw IOException("Failed to delete $file for atomic write")
            }
        }
    } else {
        if (!tmp.renameTo(file)) {
            throw IOException("Failed to move temp file to $file")
        }
    }
}

