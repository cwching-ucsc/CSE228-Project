package network

import org.scalatest.flatspec.AnyFlatSpec

class NetworkAddrTester extends AnyFlatSpec {
  behavior of "NetworkAddr"
  it should "be able to parse an IPv4 address" in {
    val ip = IPv4Addr("1.2.4.8")
    assert(ip.toString == "1.2.4.8")
  }

  it should "be able to compare two IPv4 addresses" in {
    val ip1 = IPv4Addr("1.2.4.8")
    val ip2 = IPv4Addr("1.2.4.8")
    val ip3 = IPv4Addr("1.1.1.1")
    assert(ip1 == ip2)
    assert(ip1 != ip3)
    print(ip1)
  }
}
