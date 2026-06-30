package re.melchior.saviomobile.ui.screen.intervention.cloture

fun shouldShowRenewContractCta(renewable: Boolean, hasVeTypeSelected: Boolean): Boolean =
    renewable && hasVeTypeSelected
