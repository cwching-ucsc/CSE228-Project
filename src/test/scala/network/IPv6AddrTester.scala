package network

import org.scalatest.flatspec.AnyFlatSpec

class IPv6AddrTester extends AnyFlatSpec {
  behavior of "NetworkAddr"
  it should "be able to parse an IPv6 address from String" in {
    val ip = IPv6Addr("2000:0000:140F:0000:0000:0000:7000:1CCC")
    assert(ip.toString == "2000:0000:140F:0000:0000:0000:7000:1CCC")
  }

  it should "be able to parse an IPv6 address from String when stored as negative" in {
    val ip = IPv6Addr("2041:0000:B40F:0000:0000:0000:AAAA:1CCC")
    assert(ip.toString == "2041:0000:B40F:0000:0000:0000:AAAA:1CCC")
  }

  it should "be able to compare two IPv6 addresses" in {
    val ip1 = IPv6Addr("2000:0000:140F:0000:0000:0000:7000:1CCC")
    val ip2 = IPv6Addr("2000:0000:140F:0000:0000:0000:7000:1CCC")
    val ip3 = IPv6Addr("2041:0000:B40F:0000:0000:0000:AAAA:1CCC")
    assert(ip1 == ip2)
    assert(ip1 != ip3)
  }

  it should "be able to parse two extreme IPv6 addresses" in {
    val ip1 = IPv6Addr("0000:0000:0000:0000:0000:0000:0000:0000")
    val ip2 = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")
    assert(ip1.toString == "0000:0000:0000:0000:0000:0000:0000:0000")
    assert(ip2.toString == "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")
  }
}
