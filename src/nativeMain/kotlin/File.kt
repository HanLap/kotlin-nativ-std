import kotlinx.cinterop.*
import platform.posix.*

class File(private val path: String) {

  enum class FileType(name: String) {
    DIRECTORY("Directory"),
    FILE("Regular File"),
    SYMLINK("Symbolic Link"),
    CHARACTER_DEVICE("Character Device"),
    BLOCK_DEVICE("Block Device"),
    UNKNOWN("Unknown")
  }

  private val info: stat by lazy {
    memScoped {
      val info = alloc<stat>()
      stat(path, info.ptr)
      info
    }
  }

  private val fileType: FileType by lazy {
    when (info.st_mode.toInt() and S_IFMT) {
      S_IFREG -> FileType.FILE
      S_IFDIR -> FileType.DIRECTORY
      S_IFLNK -> FileType.SYMLINK
      S_IFCHR -> FileType.CHARACTER_DEVICE
      S_IFBLK -> FileType.BLOCK_DEVICE
      else -> FileType.UNKNOWN
    }
  }

  fun printStat(): Unit = with(info) {
    println(
      """
      Stats $path
      ---------------------
      file type: ${fileType.name}
      blksize:   $st_blksize
      atime:     ${st_atim.tv_sec}
      ctime:     ${st_ctim.tv_sec}
      mtime:     ${st_mtim.tv_sec}
      mode:      $st_mode
      gid:       $st_gid
      uid:       $st_uid
      size:      $st_size
      dev:       $st_dev
    """.trimIndent()
    )
  }

  fun exists(): Boolean =
    fopen(path, "r") != NULL

  fun isDir(): Boolean {
    return false
  }

  fun delete(): Boolean = remove(path) != -1

  fun readLines(): Collection<String>? {
    val fp = fopen(path, "r")
      ?: return null

    val lines = ArrayList<String>()
    memScoped {
      while (true) {
        val ptr = zeroValue<CPointerVar<ByteVar>>().getPointer(this)
        val len = getline(ptr, zeroValue(), fp)

        if (len == -1L) {
          break
        }


        ptr.getPointer(this)[0]
          ?.toKString()
          ?.let(lines::add)
          ?: throw Error("Could not read line")

        free(ptr.getPointer(this)[0])
      }
    }

    fclose(fp)

    return lines
  }

  fun writeText(text: String) {
    val fp = fopen(path, "w")
      ?: throw IOException("could not Open file: $path")

    fputs(text, fp)
      .let { if (it == EOF) throw IOException("could not write to file") }

    fclose(fp)
  }

}