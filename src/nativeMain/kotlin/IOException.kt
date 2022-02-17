data class IOException(
  override val message: String? = null,
  override val cause: Throwable? = null
) : RuntimeException(message, cause) {
  constructor(cause: Throwable?): this(null, cause)
}