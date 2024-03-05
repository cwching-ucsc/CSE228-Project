package network

import org.scalatest.flatspec.AnyFlatSpec

class NetworkAddrTester extends AnyFlatSpec {
  behavior of "NetworkAddr"
  it should "be able to parse an IPv4 address from String" in {
    val ip = IPv4Addr("1.2.4.8")
    assert(ip.toString == "1.2.4.8")
  }

  it should "be able to parse an IPv4 address from Vec[Int]" in {
    val ip = IPv4Addr(
      Vector(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0)
    )
    assert(ip.toString == "1.2.4.8")
  }

  it should "be able to parse an IPv4 address from Long" in {
    val ip = IPv4Addr(1L)
    assert(ip.toString == "0.0.0.1")
  }

  it should "be able to compare two IPv4 addresses" in {
    val ip1 = IPv4Addr("1.2.4.8")
    val ip2 = IPv4Addr("1.2.4.8")
    val ip3 = IPv4Addr("1.1.1.1")
    assert(ip1 == ip2)
    assert(ip1 != ip3)
  }

  it should "be able to parse two extreme IPv4 addresses" in {
    val ip1 = IPv4Addr("0.0.0.0")
    val ip2 = IPv4Addr("255.255.255.255")
    assert(ip1.toString == "0.0.0.0")
    assert(ip2.toString == "255.255.255.255")
  }
}
