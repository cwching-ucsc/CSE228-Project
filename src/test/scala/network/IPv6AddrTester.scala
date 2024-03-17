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

  it should "be able to parse an IPv6 address from BigInt" in {
    val ip = IPv6Addr(BigInt(1))
    assert(ip.toString == "0000:0000:0000:0000:0000:0000:0000:0001")
  }

  it should "be able to parse an IPv6 address from BigInt when stored as negative" in {
    val ip = IPv6Addr(BigInt(0xffff))
    assert(ip.toString == "0000:0000:0000:0000:0000:0000:0000:FFFF")
  }

  it should "be able to parse two extreme IPv6 addresses from BigInt" in {
    val ip1 = IPv6Addr(BigInt(1, Array.fill(16)(0.toByte)))
    val ip2 = IPv6Addr(BigInt(1, Array.fill(16)(255.toByte)))
    assert(ip1.toString == "0000:0000:0000:0000:0000:0000:0000:0000")
    assert(ip2.toString == "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")
  }

  it should "be able to return correct BigInt from an IPv6 address" in {
    val ip = IPv6Addr("0000:0000:0000:0000:0000:0000:0000:0002")
    assert(ip.toBigInt == BigInt(2))
  }

  it should "be able to return correct BigInt from an IPv4 address when stored as negative" in {
    val ip = IPv6Addr("0000:0000:0000:0000:0000:0000:0000:FFFF")
    assert(ip.toBigInt == BigInt(0xffff))
  }
}
