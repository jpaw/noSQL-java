import api.*
import apiw.*
import customerw.*

package ohmwTest prefix t1 {
    
    category dat {
        trackinggroup ReadWriteTracking trackingupdater de.jpaw.bonaparte.refsw.impl.ReadWriteTrackingUpdater
    }
    
    entity CustomerEntity category dat is CustomerDTO ohm {
        tenantClass TenantDiscriminator
        pk (objectRef)
        index unique CustomerKey (tenantRef, customerId)
    } 
}