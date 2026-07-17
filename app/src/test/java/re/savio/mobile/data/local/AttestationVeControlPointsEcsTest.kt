package re.savio.mobile.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttestationVeControlPointsEcsTest {

    @Test
    fun gazPoints_unchanged() {
        val keys = AttestationVeControlPoints.getPointsForType("GAZ").map { it.cle }
        assertTrue(keys.contains("HYD_HEAD"))
        assertTrue(keys.contains("GAZ_GEN_FONCT"))
    }

    @Test
    fun ecsGazNat_includesSanitAndEcsGazControles() {
        val keys =
            AttestationVeControlPoints.getPointsForType("ECS", "GAZ NAT", "CHAUFFE EAU")
                .map { it.cle }
        assertTrue(keys.contains("SANIT_TEMP_CONSIGNE"))
        assertTrue(keys.contains("ECS_ETANCHEITE_GAZ"))
        assertTrue(keys.contains("ECS_BRULEUR"))
        assertFalse(keys.contains("ECS_EVAC_FUMEES"))
        assertFalse(keys.contains("ECS_VENTIL_LOCAL"))
        assertFalse(keys.contains("HYD_HEAD"))
        assertFalse(keys.contains("GAZ_GEN_FONCT"))
        assertFalse(keys.contains("SANIT_ANODE"))
        assertFalse(keys.contains("ECS_ANODE"))
    }

    @Test
    fun ecsGazNat_installationPoints_includeEvacAndVentil() {
        val keys =
            AttestationVeControlPoints.getInstallationPointsForType(
                "ECS",
                "GAZ NAT",
                "CHAUFFE EAU",
            ).map { it.cle }
        assertTrue(keys.contains("ECS_EVAC_FUMEES"))
        assertTrue(keys.contains("ECS_VENTIL_LOCAL"))
        assertFalse(keys.contains("ECS_ETANCHEITE_GAZ"))
        assertFalse(keys.contains("ECS_BRULEUR"))
    }

    @Test
    fun ecsGazProp_includesEcsGazControles() {
        val keys =
            AttestationVeControlPoints.getPointsForType("ECS", "GAZ PROP", "CHAUFFE EAU")
                .map { it.cle }
        assertTrue(keys.contains("ECS_ETANCHEITE_GAZ"))
        assertTrue(keys.contains("ECS_BRULEUR"))
        assertFalse(keys.contains("ECS_EVAC_FUMEES"))
    }

    @Test
    fun ecsGazGeneric_excludesEcsGazPoints() {
        val keys =
            AttestationVeControlPoints.getPointsForType("ECS", "GAZ", "CHAUFFE EAU")
                .map { it.cle }
        assertFalse(keys.contains("ECS_ETANCHEITE_GAZ"))
        assertFalse(keys.contains("GAZ_GEN_FONCT"))
        val installKeys =
            AttestationVeControlPoints.getInstallationPointsForType("ECS", "GAZ", "CHAUFFE EAU")
        assertTrue(installKeys.isEmpty())
    }

    @Test
    fun ecsElec_noInstallationGazPoints() {
        val installKeys =
            AttestationVeControlPoints.getInstallationPointsForType("ECS", "ELEC", "CHAUFFE EAU")
        assertTrue(installKeys.isEmpty())
    }

    @Test
    fun ballonEcsElec_includesEcsAnode() {
        val keys =
            AttestationVeControlPoints.getPointsForType("ECS", "ELEC", "BALLON ECS")
                .map { it.cle }
        assertTrue(keys.contains("ECS_ANODE"))
        assertFalse(keys.contains("SANIT_ANODE"))
        assertFalse(keys.contains("ECS_ETANCHEITE_GAZ"))
    }

    @Test
    fun chauffeEauElec_excludesAnode() {
        val keys =
            AttestationVeControlPoints.getPointsForType("ECS", "ELEC", "CHAUFFE EAU")
                .map { it.cle }
        assertFalse(keys.contains("ECS_ANODE"))
        assertFalse(keys.contains("SANIT_ANODE"))
    }
}
