import api.*
import apip.*
import customerp.*

package ohmpTest prefix t1 {
    
    category dat {
        trackinggroup ReadWriteTracking trackingupdater de.jpaw.bonaparte.refsp.impl.ReadWriteTrackingUpdater
    }
    
    entity CustomerEntity category dat is CustomerDTO ohm {
        tenantClass TenantDiscriminator
        index unique CustomerKey (tenantRef, customerId)
    } 
}