import tests1.*

package aesTests1 prefix t1 {

    category empty {
        namespace test
    }

    entity AesTest1Set category empty is AesTest1 aerospike {
        pk (text1, bool1)
//        index "num_idx" (num1)
    }
}
