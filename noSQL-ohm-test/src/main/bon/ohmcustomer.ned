import api.*
import customer.*

package ohmTests1 prefix t1 {
    
    category dat {
        trackinggroup ReadWriteTracking trackingupdater de.jpaw.bonaparte.refs.impl.ReadWriteTrackingUpdater
    }
    
    entity CustomerEntity category dat is CustomerDTO ohm {
        tenantClass TenantDiscriminator
        pk (objectRef)
        index unique CustomerKey (tenantRef, customerId)
    } 
}