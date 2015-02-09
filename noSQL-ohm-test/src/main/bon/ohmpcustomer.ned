import api.*
import apip.*
import customerp.*

package ohmpTests prefix t1 {
    
    category dat {
        trackinggroup ReadWriteTracking trackingupdater de.jpaw.bonaparte.refsp.impl.ReadWriteTrackingUpdater
    }
    
    entity CustomerEntity category dat is CustomerDTO ohm {
        tenantClass TenantDiscriminator
        pk (objectRef)
        index unique CustomerKey (tenantRef, customerId)
    } 
}