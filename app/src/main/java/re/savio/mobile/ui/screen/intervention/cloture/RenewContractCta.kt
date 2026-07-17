package re.savio.mobile.ui.screen.intervention.cloture

fun shouldShowRenewContractCta(renewable: Boolean, hasVeTypeSelected: Boolean): Boolean =
    renewable && hasVeTypeSelected
