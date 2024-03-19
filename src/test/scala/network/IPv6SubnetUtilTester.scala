package network

import org.scalatest.flatspec.AnyFlatSpec

class IPv6SubnetUtilTester extends AnyFlatSpec {
  behavior of "SubnetUtil"
  it should "be able to calculate subnet start address based on IP and mask (CIDR: /112)" in {
    val ip = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0321")
    val mask = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:0000")
    val target = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0000")
    assert(IPv6SubnetUtil.getStartAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet end address based on IP and mask (CIDR: /112)" in {
    val ip = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0321")
    val mask = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:0000")
    val target = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:FFFF")
    assert(IPv6SubnetUtil.getEndAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet start address based on IP and mask (CIDR: /96)" in {
    val ip = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0321")
    val mask = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:0000:0000")
    val target = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0000:0000")
    assert(IPv6SubnetUtil.getStartAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet end address based on IP and mask (CIDR: /96)" in {
    val ip = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0321")
    val mask = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:0000:0000")
    val target = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:FFFF:FFFF")
    assert(IPv6SubnetUtil.getEndAddr(ip, mask) == target)
  }

  it should "be able to calculate subnet range based on IP and mask (CIDR: /126)" in {
    val ip = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0321")
    val mask = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFC")
    val target = Vector(
      IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0320"),
      IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0321"),
      IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0322"),
      IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0323")
    )
    assert(IPv6SubnetUtil.getRangeVector(ip, mask).equals(target))
  }

  it should "be able to check if an IP is in the subnet (CIDR: /126)" in {
    val ip = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0321")
    val mask = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFC")
    val target = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:0322")
    assert(IPv6SubnetUtil.isInSubnet(target, ip, mask))
  }

  it should "be able to check if an IP is a broadcast address (CIDR: /126)" in {
    val ip = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:FFFD")
    val mask = IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFC")
    val target = IPv6Addr("2001:0DB8:85A3:0000:0000:8A2E:0123:FFFF")
    assert(IPv6SubnetUtil.isBroadCastAddr(target, ip, mask))
  }
}
