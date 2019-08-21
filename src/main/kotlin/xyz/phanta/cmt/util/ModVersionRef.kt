package xyz.phanta.cmt.util

sealed class ModVersionRef {
    companion object {
        private val REF_PATTERN: Regex = Regex("""([a-z0-9\-]+)(?:@(\d+))?""")

        fun parse(ref: String): ModVersionRef {
            val match = REF_PATTERN.matchEntire(ref) ?: throw IllegalArgumentException("Invalid mod version ref: $ref")
            return match.groups[2]?.let { ClosedModVersionRef(match.groups[1]!!.value, it.value.toLong()) }
                ?: OpenModVersionRef(match.groups[1]!!.value)
        }
    }

    abstract val slug: String
}

data class OpenModVersionRef(override val slug: String) : ModVersionRef() {
    override fun toString(): String = slug
}

data class ClosedModVersionRef(override val slug: String, val fileId: Long) : ModVersionRef() {
    override fun toString(): String = "$slug@$fileId"
}
