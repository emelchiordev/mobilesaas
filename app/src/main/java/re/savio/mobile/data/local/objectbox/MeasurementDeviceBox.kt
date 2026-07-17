package re.savio.mobile.data.local.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
data class MeasurementDeviceBox(
    @Id var boxId: Long = 0,
    @Unique var id: String = "",
    var brand: String = "",
    var model: String = "",
    var serialNumber: String? = null,
    /** JSON array string e.g. ["cerfa"] */
    var useCasesJson: String = "[\"cerfa\"]",
    var lastControlDate: String? = null,
    @Index var assignedTechnicianId: String? = null,
    var updatedAt: String = "",
)
