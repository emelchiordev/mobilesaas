package re.melchior.saviomobile.data.local

object AttestationVeControlPoints {

    enum class LineType { HEAD, SUBHEAD, BODY }

    data class ControlPoint(
        val cle: String,
        val description: String,
        val type: LineType,
        val hasSansObjet: Boolean = true,
    )

    val HYDRAULIQUE = listOf(
        ControlPoint(
            "HYD_HEAD",
            "Réseau hydraulique de chauffage",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "HYD_EMBOUE",
            "Contrôle de l'embouement",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "HYD_PURGE",
            "Purge des bulles d'air (si purgeur(s) fonctionnel(s) et accessible(s))",
            LineType.BODY,
        ),
        ControlPoint(
            "HYD_PRESSION",
            "Contrôle de la pression du réseau hydraulique",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "HYD_CIRCU",
            "Vérification du fonctionnement du(des) circulateur(s)",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "HYD_VASE",
            "Contrôle de la pression de gonflage du(des) vase(s) d'expansion avec regonflage si nécessaire",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "HYD_ISOL",
            "Contrôle de la présence et de l'état d'isolation des réseaux de distribution (hors volume chauffé)",
            LineType.BODY,
        ),
    )

    val REGULATION = listOf(
        ControlPoint(
            "REG_HEAD",
            "Régulation de l'installation de chauffage",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "REG_THERMOSTAT",
            "Équipements de régulation automatique de la température intérieure par pièce ou par zone",
            LineType.BODY,
        ),
        ControlPoint(
            "REG_PROG",
            "Système de programmation de la température intérieure, a minima horaire",
            LineType.BODY,
        ),
        ControlPoint(
            "REG_REGUL",
            "Régulateur du(des) générateur(s) de chaleur a minima de classe IV",
            LineType.BODY,
        ),
        ControlPoint(
            "REG_BATI",
            "Système d'automatisation et de contrôle des bâtiments (tertiaire)",
            LineType.BODY,
        ),
        ControlPoint(
            "REG_SONDES",
            "Fonctionnement des sondes de température",
            LineType.BODY,
        ),
        ControlPoint(
            "REG_DEPART",
            "Température de départ affichée et cohérence de cette température",
            LineType.BODY,
        ),
        ControlPoint(
            "REG_ROBINET",
            "Positionnement et fonctionnement des robinets thermostatiques",
            LineType.BODY,
        ),
        ControlPoint(
            "REG_HORAIRE",
            "Cohérence de la programmation horaire avec les usages du bâtiment",
            LineType.BODY,
        ),
    )

    val PAC_HYDRAULIQUE = listOf(
        ControlPoint(
            "PAC_HYD_HEAD",
            "Distribution hydraulique",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_HYD_EMBOUE",
            "Contrôle de l'embouement",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_HYD_PURGE",
            "Purge des bulles d'air",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_HYD_PRESSION",
            "Contrôle de la pression du réseau hydraulique",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_HYD_CIRCU",
            "Vérification du fonctionnement du(des) circulateur(s)",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_HYD_FILTRE",
            "Vérification et nettoyage du filtre sur la distribution hydraulique si nécessaire",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_HYD_VASE",
            "Contrôle de la pression de gonflage du(des) vases d'expansion",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_HYD_ISOL",
            "Contrôle de la présence et de l'état d'isolation des réseaux de distribution",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_AER_HEAD",
            "Distribution aéraulique",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_AER_GAINES",
            "Vérification de l'état des gaines accessibles",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_AER_VENTIL",
            "Vérification du fonctionnement du ventilateur",
            LineType.BODY,
            hasSansObjet = false,
        ),
    )

    val PAC_REGULATION = listOf(
        ControlPoint(
            "PAC_REG_HEAD",
            "Régulation de l'installation de chauffage ou de froid",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_REG_THERMOSTAT",
            "Équipements de régulation automatique de la température par pièce ou par zone",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_REG_PROG",
            "Système de programmation de la température intérieure, a minima horaire",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_REG_REGUL",
            "Régulateur du(des) générateur(s) de chauffage central à eau a minima de classe IV",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_REG_BATI",
            "Système d'automatisation et de contrôle des bâtiments (tertiaire)",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_REG_DEPART",
            "Température de départ d'eau affichée et cohérence",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_REG_SONDES",
            "Fonctionnement des sondes de température",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_REG_ROBINET",
            "Positionnement et fonctionnement des robinets thermostatiques",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_REG_HORAIRE",
            "Cohérence de la programmation horaire avec les usages du bâtiment",
            LineType.BODY,
        ),
    )

    val GAZ_GENERATEUR = listOf(
        ControlPoint(
            "GAZ_GEN_HEAD",
            "Points de contrôle obligatoires de la chaudière gaz",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "GAZ_GEN_FONCT",
            "Vérification du bon fonctionnement de la chaudière",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "GAZ_GEN_CONDUIT",
            "Nettoyage du conduit de raccordement, si démontable",
            LineType.BODY,
        ),
        ControlPoint(
            "GAZ_GEN_ETAT_CONDUIT",
            "Vérification de l'état, la nature et la géométrie du conduit de raccordement",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "GAZ_GEN_NETTOYAGE",
            "Nettoyage du corps de chauffe, brûleur, veilleuse, ventilateur, électrodes",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "GAZ_GEN_SIPHON",
            "Nettoyage du siphon des condensats (chaudières à condensation)",
            LineType.BODY,
        ),
        ControlPoint(
            "GAZ_GEN_DEBIT",
            "Vérification et réglage éventuel du débit de gaz",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "GAZ_GEN_CIRCU",
            "Vérification du circulateur de l'installation piloté par la chaudière",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "GAZ_GEN_SECU",
            "Vérification des dispositifs de sécurité du générateur",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "GAZ_GEN_VMC",
            "VMC gaz : vérification dispositif individuel de sécurité, DSC, nettoyage conduit",
            LineType.BODY,
        ),
        ControlPoint(
            "GAZ_GEN_3CEP",
            "Conduit 3 CEp : vérification clapet anti-retour et plaque signalétique",
            LineType.BODY,
        ),
        ControlPoint(
            "GAZ_GEN_ECS",
            "Chaudière avec ballon ECS : vérification des anodes et accessoires",
            LineType.BODY,
        ),
    )

    val FIOUL_GENERATEUR = listOf(
        ControlPoint(
            "FIO_GEN_HEAD",
            "Points de contrôle obligatoires de la chaudière fioul",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "FIO_GEN_CONDUIT",
            "Vérification de l'état, de la nature et de la géométrie du conduit de raccordement",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "FIO_GEN_CORPS",
            "Nettoyage du corps de chauffe",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "FIO_GEN_BRULEUR",
            "Démontage et nettoyage du brûleur",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "FIO_GEN_PREFILTRE",
            "Nettoyage du préfiltre fioul domestique",
            LineType.BODY,
        ),
        ControlPoint(
            "FIO_GEN_FILTRE",
            "Nettoyage du filtre de la pompe fioul domestique",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "FIO_GEN_SECU",
            "Vérification fonctionnelle des dispositifs de sécurité du brûleur et de la chaudière",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "FIO_GEN_CIRCU",
            "Vérification fonctionnelle du circulateur de chauffage intégré (si présent)",
            LineType.BODY,
        ),
        ControlPoint(
            "FIO_GEN_ECS",
            "Chaudière avec ballon ECS : vérification des anodes et accessoires",
            LineType.BODY,
        ),
    )

    val BOIS_GENERATEUR = listOf(
        ControlPoint(
            "BOIS_GEN_HEAD",
            "Points de contrôle obligatoires de la chaudière bois",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "BOIS_GEN_CONDUIT",
            "Contrôle du raccordement et de l'étanchéité du conduit d'évacuation",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "BOIS_GEN_JOINTS",
            "Vérification de l'état des joints",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "BOIS_GEN_NETTOYAGE",
            "Nettoyage du corps de chauffe et décendrage approfondi",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "BOIS_GEN_VERIF",
            "Vérification complète de la chaudière",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "BOIS_GEN_ALIM",
            "Vérification du système d'alimentation automatique (si présent)",
            LineType.BODY,
        ),
        ControlPoint(
            "BOIS_GEN_VENTIL",
            "Nettoyage du(des) ventilateur(s) (si présent(s))",
            LineType.BODY,
        ),
        ControlPoint(
            "BOIS_GEN_SECU",
            "Vérification fonctionnelle des dispositifs de sécurité de la chaudière",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "BOIS_GEN_ECS",
            "Chaudière avec ballon ECS : vérification des anodes et accessoires",
            LineType.BODY,
        ),
    )

    val PAC_GENERATEUR = listOf(
        ControlPoint(
            "PAC_GEN_HEAD",
            "Points de contrôle obligatoires du générateur thermodynamique",
            LineType.HEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_GEN_TEMP",
            "Relevé des températures unité intérieure/extérieure et vérification du fonctionnement",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_GEN_INVERSION",
            "Vérification du fonctionnement de l'inversion de cycle",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_GEN_APPOINT",
            "Vérification de l'enclenchement des appoints",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_GEN_AEROTHERM_HEAD",
            "Pour les systèmes aérothermiques uniquement",
            LineType.SUBHEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_GEN_ECHANGEUR",
            "Vérification de l'échangeur de l'unité extérieure et nettoyage si nécessaire",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_GEN_UNITE_INT",
            "Vérification et nettoyage avec désinfection si nécessaire de l'unité intérieure et du filtre",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_GEN_SECU",
            "Vérification des dispositifs de sécurité du générateur thermodynamique",
            LineType.BODY,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_GEN_ETANCHE_HEAD",
            "Points de contrôle obligatoires de l'étanchéité du circuit frigorifique",
            LineType.SUBHEAD,
            hasSansObjet = false,
        ),
        ControlPoint(
            "PAC_GEN_VOYANT",
            "Vérification du voyant de fluide frigorigène",
            LineType.BODY,
        ),
        ControlPoint(
            "PAC_GEN_PRESSION",
            "Relevé des pressions à l'entrée et à la sortie du compresseur sur les manomètres",
            LineType.BODY,
        ),
    )

    fun getPointsForType(type: String): List<ControlPoint> {
        return when (type) {
            "GAZ" -> HYDRAULIQUE + REGULATION + GAZ_GENERATEUR
            "FIOUL" -> HYDRAULIQUE + REGULATION + FIOUL_GENERATEUR
            "BOIS" -> HYDRAULIQUE + REGULATION + BOIS_GENERATEUR
            "PAC" -> PAC_HYDRAULIQUE + PAC_REGULATION + PAC_GENERATEUR
            "PAC_HYBRIDE_GAZ" ->
                PAC_HYDRAULIQUE + PAC_REGULATION + PAC_GENERATEUR + GAZ_GENERATEUR
            "PAC_HYBRIDE_FIOUL" ->
                PAC_HYDRAULIQUE + PAC_REGULATION + PAC_GENERATEUR + FIOUL_GENERATEUR
            else -> emptyList()
        }
    }
}
