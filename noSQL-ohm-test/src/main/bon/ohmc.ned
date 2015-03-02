import api.*
import customerc.*

package ohmcTest prefix t1 {
    
    category dat {
        trackinggroup ReadWriteTracking trackingupdater de.jpaw.bonaparte.refsp.impl.ReadWriteTrackingUpdater
    }
    
    entity CustomerEntity category dat is CustomerDTO ohm {
        ref CustomerRef
        pk CustomerKey
        refp "return ref <= 0L ? null : new CustomerKey(de.jpaw.api.iso.CountryKeyConverter.intToCountryCodeA2((int)ref), (int)(ref >> 32));"
    } 
}