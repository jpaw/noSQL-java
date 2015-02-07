import api.*
import customer.*

package ohmTests1 prefix t1 {
    
    category dat {
        trackinggroup ReadWriteTracking
    }
    
    entity CustomerEntity category dat is CustomerDTO ohm {
        tenantClass TenantDiscriminator
        pk (objectRef)
        index unique "CustomerKey" (tenantRef, customerId)
    } 
}