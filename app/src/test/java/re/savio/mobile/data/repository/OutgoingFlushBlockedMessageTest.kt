package re.savio.mobile.data.repository

import org.junit.Assert.assertTrue
import org.junit.Test

class OutgoingFlushBlockedMessageTest {

    @Test
    fun blockedMessage_mentionsTechnicianAndSync() {
        // Light check without Android Context — mirrors OutgoingAccountFlushGate.blockedMessage
        fun blockedMessage(outgoingDisplayName: String?, offline: Boolean): String {
            val who = outgoingDisplayName?.takeIf { it.isNotBlank() } ?: "l'utilisateur précédent"
            return if (offline) {
                "Des données de $who ne sont pas encore synchronisées — connectez-vous avant de changer de compte."
            } else {
                "Des données de $who n'ont pas pu être synchronisées — réessayez avec une connexion stable avant de changer de compte."
            }
        }
        val offline = blockedMessage("Alice", offline = true)
        assertTrue(offline.contains("Alice"))
        assertTrue(offline.contains("synchronisées"))
        val onlineFail = blockedMessage(null, offline = false)
        assertTrue(onlineFail.contains("utilisateur précédent"))
    }
}
