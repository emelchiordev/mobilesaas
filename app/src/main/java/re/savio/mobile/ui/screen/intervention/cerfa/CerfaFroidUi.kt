package re.savio.mobile.ui.screen.intervention.cerfa

import re.savio.mobile.data.local.entity.ColdMeasureEntity

/**
 * Définition des onglets CERFA 15497 (libellé humain + référence officielle).
 */
data class CerfaFroidTabDef(
    val title: String,
    /** Numéro(s) de case CERFA, sans crochets — ex. `7–9`. */
    val cerfaRef: String,
)

val CERFA_FROID_TABS: List<CerfaFroidTabDef> =
    listOf(
        CerfaFroidTabDef("Équipement", "3"),
        CerfaFroidTabDef("Nature", "4"),
        CerfaFroidTabDef("Obs.", "5–6"),
        CerfaFroidTabDef("Fuites", "7–9"),
        CerfaFroidTabDef("Fluides", "10"),
        CerfaFroidTabDef("Transport", "11"),
        CerfaFroidTabDef("Obs. fluides", "12"),
        CerfaFroidTabDef("Bordereau", "13–14"),
    )

enum class CerfaTabFillState {
    /** Pas encore de saisie significative. */
    Todo,
    /** Saisie présente, pas de blocage. */
    Done,
    /** Champ obligatoire manquant (ex. contenant fluide). */
    Missing,
}

/** Mode périodicité : sans système (FREQS*) ou avec détection auto (FREQA*). */
enum class CerfaFrequencyMode {
    Standard,
    Auto,
}

/** Fuites constatées = PASFUITE « N » (oui, il y a des fuites). */
fun cerfaLeaksEnabled(pasfuite: String): Boolean = pasfuite == "N"

/**
 * Nombre de blocs fuite à afficher au chargement (1–3), d’après les données déjà saisies.
 * Au minimum 1 si les fuites sont activées ; les blocs vides 2/3 ne sont pas ouverts par défaut.
 */
fun initialLeakBlockCount(
    fuiteloc1: String,
    fuiterep1: String,
    fuiteloc2: String,
    fuiterep2: String,
    fuiteloc3: String,
    fuiterep3: String,
): Int {
    val has3 = fuiteloc3.isNotBlank() || fuiterep3.isNotBlank()
    val has2 = fuiteloc2.isNotBlank() || fuiterep2.isNotBlank()
    return when {
        has3 -> 3
        has2 -> 2
        else -> 1
    }
}

fun initialLeakBlockCount(state: ColdMeasureEntity): Int =
    initialLeakBlockCount(
        state.fuiteloc1,
        state.fuiterep1,
        state.fuiteloc2,
        state.fuiterep2,
        state.fuiteloc3,
        state.fuiterep3,
    )

/** Indices 1-based des blocs fuite visibles. */
fun visibleLeakBlockIndices(leaksEnabled: Boolean, blockCount: Int): List<Int> {
    if (!leaksEnabled) return emptyList()
    val n = blockCount.coerceIn(1, 3)
    return (1..n).toList()
}

/**
 * Onglet avec champ obligatoire métier non rempli (indicateur navigation).
 * Aujourd’hui : contenants fluides conditionnels.
 */
fun cerfaTabHasIncompleteRequired(tabIndex: Int, state: ColdMeasureEntity): Boolean {
    if (tabIndex != 4) return false
    val rein = state.fluidrein.replace(",", ".").toDoubleOrNull() ?: 0.0
    val recup = state.fluidrecup.replace(",", ".").toDoubleOrNull() ?: 0.0
    val supplyMissing = rein > 0.0 && state.supplyContainerId.isBlank()
    val recoveryMissing = recup > 0.0 && state.recoveryContainerId.isBlank()
    return supplyMissing || recoveryMissing
}

fun cerfaTabHasMeaningfulData(tabIndex: Int, state: ColdMeasureEntity): Boolean {
    fun anyO(vararg v: String) = v.any { it == "O" }
    fun anyText(vararg v: String) = v.any { it.isNotBlank() }
    return when (tabIndex) {
        0 -> anyText(state.frigo, state.charg, state.tonnage)
        1 ->
            anyO(
                state.minter1, state.minter2, state.minter3, state.minter4,
                state.minter5, state.minter6, state.minter7, state.minter8,
            ) || state.minteraut.isNotBlank()
        2 -> anyText(state.obsern1, state.obsern2, state.detm1, state.dett1, state.detd1)
        3 ->
            state.pasfuite.isNotBlank() ||
                state.autofuite == "O" ||
                anyO(state.freqs1, state.freqs2, state.freqs3, state.freqa1, state.freqa2, state.freqa3) ||
                anyText(
                    state.fuiteloc1, state.fuiterep1, state.fuiteloc2, state.fuiterep2,
                    state.fuiteloc3, state.fuiterep3, state.qtefri, state.qtefri2, state.qtefri3,
                )
        4 ->
            anyText(
                state.fluidecv, state.fluidecr, state.fluidecrg, state.fluidrein,
                state.fluidert, state.fluideru, state.fluidrecup,
                state.supplyContainerId, state.recoveryContainerId,
            )
        5 ->
            anyText(
                state.un1078a, state.un1078b, state.nomdec, state.adres1dec, state.adres2dec,
                state.villedec, state.nomtrans, state.adres1trans, state.adres2trans, state.villetrans,
            )
        6 -> anyText(state.fluidobs, state.fluidobs2)
        7 ->
            anyText(
                state.bordeqte, state.bordetrans, state.instatrait, state.coderd,
                state.qterecep, state.frigo2, state.bsff, state.un3161a, state.un3161b,
            )
        else -> false
    }
}

fun cerfaTabFillState(tabIndex: Int, state: ColdMeasureEntity): CerfaTabFillState {
    if (cerfaTabHasIncompleteRequired(tabIndex, state)) return CerfaTabFillState.Missing
    if (cerfaTabHasMeaningfulData(tabIndex, state)) return CerfaTabFillState.Done
    return CerfaTabFillState.Todo
}

fun countMissingRequiredTabs(state: ColdMeasureEntity): Int =
    CERFA_FROID_TABS.indices.count { cerfaTabFillState(it, state) == CerfaTabFillState.Missing }

/** Champs calculés : non éditables au tap. */
fun isCerfaAutoCalculatedField(label: String): Boolean =
    label.contains("(auto)", ignoreCase = true)

fun cerfaFrequencyMode(state: ColdMeasureEntity): CerfaFrequencyMode {
    val autoOn = state.freqa1 == "O" || state.freqa2 == "O" || state.freqa3 == "O"
    if (autoOn) return CerfaFrequencyMode.Auto
    return CerfaFrequencyMode.Standard
}

fun selectedFrequencyMonths(state: ColdMeasureEntity): Int? =
    when {
        state.freqs1 == "O" -> 12
        state.freqs2 == "O" -> 6
        state.freqs3 == "O" -> 3
        state.freqa1 == "O" -> 24
        state.freqa2 == "O" -> 12
        state.freqa3 == "O" -> 6
        else -> null
    }

/**
 * Périodicité recommandée d’après la charge t CO₂e (seuils maquette 5 / 50 / 500).
 * @return mois (12/6/3 standard, ou 24/12/6 auto)
 */
fun recommendedFrequencyMonths(tonnage: String, auto: Boolean): Int {
    val t = tonnage.replace(",", ".").toDoubleOrNull() ?: 0.0
    return if (auto) {
        when {
            t > 500.0 -> 6
            t > 50.0 -> 12
            else -> 24
        }
    } else {
        when {
            t > 500.0 -> 3
            t > 50.0 -> 6
            else -> 12
        }
    }
}

fun frequencyOptions(auto: Boolean): List<Pair<Int, String>> =
    if (auto) {
        listOf(
            24 to "Charge 5 à 50 t CO₂e",
            12 to "Charge 50 à 500 t CO₂e",
            6 to "Charge > 500 t CO₂e",
        )
    } else {
        listOf(
            12 to "Charge 5 à 50 t CO₂e",
            6 to "Charge 50 à 500 t CO₂e",
            3 to "Charge > 500 t CO₂e",
        )
    }
