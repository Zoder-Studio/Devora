package dev.devora.feature.filemanager.domain.model

enum class FileNodeType {
    FILE,
    DIRECTORY,
    SYMLINK
}

/**
 * A single entry in a directory listing. Devora never hides technical
 * project files (spec section 3) — this model exposes hidden status
 * as data, and filtering is a UI-layer decision only.
 */
data class FileNode(
    val name: String,
    val absolutePath: String,
    val type: FileNodeType,
    val isHidden: Boolean,
    val sizeBytes: Long,
    val lastModifiedEpochMillis: Long,
    val permissions: FilePermissions
)

data class FilePermissions(
    val readable: Boolean,
    val writable: Boolean,
    val executable: Boolean
) {
    /** POSIX-style rwx string for the current process's access, e.g. "rw-". */
    fun toRwxString(): String = buildString {
        append(if (readable) 'r' else '-')
        append(if (writable) 'w' else '-')
        append(if (executable) 'x' else '-')
    }
}

data class DirectoryListing(
    val currentPath: String,
    val parentPath: String?,
    val entries: List<FileNode>
)