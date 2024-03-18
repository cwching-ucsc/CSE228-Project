package integration

import network.IPv4Addr
import org.scalatest.flatspec.AnyFlatSpec

class IPv4IntegrationTester extends AnyFlatSpec {
  behavior of "IPv4IntegrationTester"

  /**
   * Switches are L2 devices (i.e. only look at MAC addresses)
   *
   * Assume we are in a LAN with IP: 192.0.0.0/24
   *
   * Reference: https://www.youtube.com/watch?v=AhOU2eOpmX0
   */
  it should "be able to use CAM in a network switch" in {
    val ip = IPv4Addr("1.2.4.8")
    assert(ip.toString == "1.2.4.8")
  }
}
