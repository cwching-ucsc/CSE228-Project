package chiselRouter

import org.scalatest.flatspec.AnyFlatSpec

class SubnetUtilTester extends AnyFlatSpec{
  behavior of "SubnetUtil"
  it should "be able to calculate subnet start address based on IP and mask" in {
    val ip = IPv4Addr("1.2.4.8")
    val mask = IPv4Addr("255.255.255.0")
    val target = IPv4Addr("1.2.4.0")
    assert(SubnetUtil.getStartAddr(ip, mask) == target)
  }
}
