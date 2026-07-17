package re.savio.mobile.data.local.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
data class RefrigerantContainerBox(
    @Id var boxId: Long = 0,
    @Unique var id: String = "",
    @Index var containerIdentifier: String = "",
    var containerKind: String = "supply",
    @Index var fluidType: String = "",
    var capacityKg: String = "",
    var currentKg: String = "",
    @Index var status: String = "",
    @Index var holderTechnicianId: String? = null,
    var bsffNumber: String? = null,
    var updatedAt: String = "",
)
