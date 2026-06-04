package re.melchior.saviomobile.util

import java.time.ZoneId

/** Fuseau métier aligné sur `APP_TIMEZONE` côté API (défaut Europe/Paris). */
object SavioTimeZone {
    val appZone: ZoneId = ZoneId.of("Europe/Paris")
}
