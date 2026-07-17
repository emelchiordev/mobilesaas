package re.savio.mobile.util

/** Aligné sur le slugify du frontend web (lettres minuscules, tirets). */
object SavioSlugify {
    fun fromCompanyName(name: String): String {
        var s =
            name
                .lowercase()
                .trim()
                .replace("\\s+".toRegex(), "-")
                .replace("[^a-z0-9-]".toRegex(), "")
                .replace("-+".toRegex(), "-")
        s = s.trim('-')
        return if (s.length >= 2) s else "mon-entreprise"
    }
}
