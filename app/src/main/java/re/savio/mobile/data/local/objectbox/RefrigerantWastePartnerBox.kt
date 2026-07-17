package re.savio.mobile.data.local.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
data class RefrigerantWastePartnerBox(
    @Id var boxId: Long = 0,
    @Unique var id: String = "",
    @Index var kind: String = "",
    var label: String = "",
    var siret: String = "",
    var addressLine1: String? = null,
    var addressLine2: String? = null,
    var postalCode: String? = null,
    var city: String? = null,
    var isDefault: Boolean = false,
    var updatedAt: String = "",
)
