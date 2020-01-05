package tech.flandia_yingm

object SftpUtils {

    fun getFilename(path: String): String {
        val index = path.lastIndexOf("/")
        return if (index == -1) path else path.substring(index + 1)
    }

    fun getRoot(path: String): String {
        val index = path.indexOf("/")
        return if (index == -1) path else path.substring(0, index)
    }

    fun getParent(path: String): String {
        val index = path.lastIndexOf("/")
        return if (index == -1) "." else path.substring(0, index)
    }

    fun getRelative(parent: String, path: String): String {
        return path.substring(parent.length + 1)
    }

    fun getResolve(path1: String, path2: String): String {
        var path1V = path1
        if (path1.endsWith("/")) {
            path1V = path1.substring(0, path1.lastIndexOf("/"))
        }
        return "$path1V/$path2"
    }

}
