package network

import org.scalatest.flatspec.AnyFlatSpec

class IPv4AddrTester extends AnyFlatSpec {
  behavior of "NetworkAddr"
  it should "be able to parse an IPv4 address from String" in {
    val ip = IPv4Addr("1.2.4.8")
    assert(ip.toString == "1.2.4.8")
  }

  it should "be able to parse an IPv4 address from String when stored as negative" in {
    val ip = IPv4Addr("192.168.0.1")
    assert(ip.toString == "192.168.0.1")
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

  it should "be able to parse an IPv4 address from BigInt" in {
    val ip = IPv4Addr(BigInt(258))
    assert(ip.toString == "0.0.1.2")
  }

  it should "be able to parse an IPv4 address from BigInt when stored as negative" in {
    val ip = IPv4Addr(BigInt(1, Array(192.toByte, 168.toByte, 0.toByte, 1.toByte)))
    assert(ip.toString == "192.168.0.1")
  }

  it should "be able to parse two extreme IPv4 addresses from BigInt" in {
    val ip1 = IPv4Addr(BigInt(1, Array.fill(4)(0.toByte)))
    val ip2 = IPv4Addr(BigInt(1, Array.fill(4)(255.toByte)))
    assert(ip1.toString == "0.0.0.0")
    assert(ip2.toString == "255.255.255.255")
  }

  it should "be able to return correct BigInt from an IPv4 address" in {
    val ip = IPv4Addr("0.0.1.2")
    assert(ip.toBigInt == BigInt(258))
  }

  it should "be able to return correct BigInt from an IPv4 address when stored as negative" in {
    val ip = IPv4Addr("192.168.0.1")
    assert(ip.toBigInt == BigInt(1, Array(192.toByte, 168.toByte, 0.toByte, 1.toByte)))
  }
}
