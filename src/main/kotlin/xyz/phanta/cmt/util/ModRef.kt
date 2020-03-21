package xyz.phanta.cmt.util

sealed class ModRef {
    companion object {
        private val SLUG_PATTERN: Regex = Regex("""[a-z\d\-]+""")

        fun parse(ref: String): ModRef = parseGracefully(ref) ?: throw IllegalArgumentException("Invalid mod ref: $ref")

        fun parseGracefully(ref: String): ModRef? = when {
            ref.startsWith('#') -> ref.substring(1).toLongOrNull()?.let { ModProjectId(it) }
            ref.matches(SLUG_PATTERN) -> ModSlug(ref)
            else -> null
        }
    }
}

data class ModProjectId(val id: Long) : ModRef() {
    override fun toString(): String = "#$id"
}

data class ModSlug(val slug: String) : ModRef() {
    override fun toString(): String = slug
}
