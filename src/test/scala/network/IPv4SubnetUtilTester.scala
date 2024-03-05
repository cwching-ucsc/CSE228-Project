package network

import org.scalatest.flatspec.AnyFlatSpec

class IPv4SubnetUtilTester extends AnyFlatSpec {
  behavior of "SubnetUtil"
  it should "be able to calculate subnet start address based on IP and mask (CIDR: /24)" in {
    val ip = IPv4Addr("1.2.4.8")
    val mask = IPv4Addr("255.255.255.0")
    val target = IPv4Addr("1.2.4.0")
    assert(IPv4SubnetUtil.getStartAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet end address based on IP and mask (CIDR: /24)" in {
    val ip = IPv4Addr("1.2.4.8")
    val mask = IPv4Addr("255.255.255.0")
    val target = IPv4Addr("1.2.4.255")
    assert(IPv4SubnetUtil.getEndAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet start address based on IP and mask (CIDR: /30)" in {
    val ip = IPv4Addr("128.114.59.212")
    val mask = IPv4Addr("255.255.255.252")
    val target = IPv4Addr("128.114.59.212")
    assert(IPv4SubnetUtil.getStartAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet end address based on IP and mask (CIDR: /30)" in {
    val ip = IPv4Addr("128.114.59.212")
    val mask = IPv4Addr("255.255.255.252")
    val target = IPv4Addr("128.114.59.215")
    assert(IPv4SubnetUtil.getEndAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet range based on IP and mask (CIDR: /30)" in {
    val ip = IPv4Addr("128.114.59.212")
    val mask = IPv4Addr("255.255.255.252")
    val target = Vector(
      IPv4Addr("128.114.59.212"),
      IPv4Addr("128.114.59.213"),
      IPv4Addr("128.114.59.214"),
      IPv4Addr("128.114.59.215")
    )
    assert(IPv4SubnetUtil.getRangeVector(ip, mask).equals(target))
  }

  it should "be able to check if an IP is in the subnet (CIDR: /30)" in {
    val ip = IPv4Addr("128.114.59.212")
    val mask = IPv4Addr("255.255.255.252")
    val target = IPv4Addr("128.114.59.213")
    assert(IPv4SubnetUtil.isInSubnet(target, ip, mask))
  }

  it should "be able to check if an IP is a broadcast address (CIDR: /30)" in {
    val ip = IPv4Addr("128.114.59.212")
    val mask = IPv4Addr("255.255.255.252")
    val target = IPv4Addr("128.114.59.215")
    assert(IPv4SubnetUtil.isBroadCastAddr(target, ip, mask))
  }
}
