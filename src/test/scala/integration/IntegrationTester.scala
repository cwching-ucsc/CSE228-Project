package integration

import cam.{CAM, CAMCmds, CAMParams, TCAM}
import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chiseltest._
import network.{IPv4Addr, IPv4SubnetUtil, IPv6Addr, IPv6SubnetUtil, NetworkAddr}
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Switches are L2 devices (i.e. only look at MAC addresses)
 * Routers are L3 devices (i.e. only look at IP addresses)
 *
 * Assume:
 * - Switch and router are in the same device in this example
 * (like the WiFi router in your home)
 * - LAN has IP range of 192.0.0.0/24, netmask of 255.255.255.0, default gateway of 192.0.0.1
 * - The router also has connected to WAN1 with IP of 172.0.0.5 and WAN2 with IP of 110.0.0.6
 * (i.e. two ISPs (Internet Service Provider) provide internet to your router)
 * - Node {A, B, C} are all connected to switch
 * - Initial MAC lookup table (based on CAM) in Switch is empty
 * - Routing table (based on TCAM) in router is already populated
 * - ARP (Address Resolution Protocol) Cache in Node A is empty when test as a switch, otherwise
 * populated when test as a router
 *
 * Name     IP          MAC               (Port)
 * Router   172.0.0.5   DB:FE:EB:73:37:1D (WAN1)
 * Router   110.0.0.6   CD:7D:16:A6:9B:66 (WAN2)
 * Switch   192.0.0.1   EF:0B:BD:5E:F4:AA (LAN) (Router)
 * Node A   192.0.0.2   6A:47:8B:32:7B:C8 (LAN0)
 * Node B   192.0.0.3   26:C5:6D:A8:D4:CE (LAN1)
 * Node C   192.0.0.4   A2:2C:CF:4E:D0:AB (LAN2)
 *
 * Routing table for IPv4 (TCAM):
 * Port / Index   Content / Target IP (CIDR)
 * 0 (WAN1)       172.000.XXX.XXX (/16)
 * 1 (WAN2)       XXX.XXX.XXX.XXX (/0) (default route)
 * 2 (LAN)        192.000.000.XXX (/24)
 *
 * Routing table for IPv6 (TCAM):
 * Port / Index   Content / Target IP (CIDR)
 * 0 (WAN1)       2041:0000:B40F:0000:0000:0000:AAAA:00XX (/120)
 * 1 (WAN2)       XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX (/0) (default route)
 * 2 (LAN)        FC00:0000:0000:0000:0000:0000:0000:XXXX (/112)
 *
 * MAC Lookup Table (CAM) (when populated):
 * Port / Index   Content / MAC Address
 * 0 (LAN0)       6A:47:8B:32:7B:C8
 * 1 (LAN1)       26:C5:6D:A8:D4:CE
 * 2 (LAN2)       A2:2C:CF:4E:D0:AB
 *
 * Reference:
 * - https://www.youtube.com/watch?v=AhOU2eOpmX0
 * - https://www.youtube.com/watch?v=AzXys5kxpAM
 */
class IntegrationTester extends AnyFlatSpec with ChiselScalatestTester {
  val camParams = CAMParams(4, 48) // MAC lookup table
  val tcamParamsIPv4 = CAMParams(3, 32) // Routing table for IPv4
  val tcamParamsIPv6 = CAMParams(3, 128) // Routing table for IPv6

  def buildWriteCmd(): CAMCmds = {
    new CAMCmds().Lit(
      _.write -> true.B,
      _.read -> false.B,
      _.delete -> false.B,
      _.reset -> false.B)
  }

  def buildReadCmd(): CAMCmds = {
    new CAMCmds().Lit(
      _.write -> false.B,
      _.read -> true.B,
      _.delete -> false.B,
      _.reset -> false.B)
  }

  /**
   * Helper function to generate IPv4Addr in Chisel format
   *
   * @param addr IPv4 address in `String`
   * @param mask Flag on whether perform mask correction
   *             (TCAM ternary bit use 1 to represent X while IPv4 use 0)
   */
  def IPv4(addr: String, mask: Boolean = false): UInt = {
    if (mask) {
      (IPv4Addr("255.255.255.255").toBigInt - IPv4Addr(addr).toBigInt).U
    } else {
      IPv4Addr(addr).toBigInt.U
    }
  }

  /**
   * Helper function to generate IPv6Addr in Chisel format
   *
   * @param addr IPv6 address in `String`
   * @param mask Flag on whether perform mask correction
   *             (TCAM ternary bit use 1 to represent X while IPv6 use 0)
   */
  def IPv6(addr: String, mask: Boolean = false): UInt = {
    if (mask) {
      (IPv6Addr("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF").toBigInt - IPv6Addr(addr).toBigInt).U
    } else {
      IPv6Addr(addr).toBigInt.U
    }
  }

  behavior of "IntegrationTester"
  it should "be able to work as a IPv4 router (Level 3)" in {
    test(new TCAM(tcamParamsIPv4)) { tcam =>

      /**
       * Load routing table into TCAM using preferred index mode
       * as the index of the entry stored in TCAM correspond
       * to the port number in the router
       */
      tcam.io.in.valid.poke(true.B)
      tcam.io.in.bits.cmds.poke(buildWriteCmd())
      tcam.io.in.bits.index.valid.poke(true.B)
      tcam.io.in.bits.index.bits.poke(0.U) // Port 0
      tcam.io.in.bits.content.poke(IPv4("172.0.0.0"))
      tcam.io.in.bits.mask.poke(IPv4("255.255.0.0", mask = true))
      tcam.io.out.valid.expect(true.B)
      tcam.io.out.bits.expect(0.U)

      tcam.clock.step()

      tcam.io.in.bits.index.bits.poke(1.U) // Port 1
      tcam.io.in.bits.content.poke(IPv4("0.0.0.0"))
      tcam.io.in.bits.mask.poke(IPv4("0.0.0.0", mask = true))
      tcam.io.out.valid.expect(true.B)
      tcam.io.out.bits.expect(1.U)

      tcam.clock.step()

      tcam.io.in.bits.index.bits.poke(2.U) // Port 2
      tcam.io.in.bits.content.poke(IPv4("192.0.0.0"))
      tcam.io.in.bits.mask.poke(IPv4("255.255.255.0", mask = true))
      tcam.io.out.valid.expect(true.B)
      tcam.io.out.bits.expect(2.U)

      tcam.clock.step()

      /**
       * [Test 1]
       * Node A wants to send a packet to 1.2.4.8
       * Assume ARP cache in Node A contains MAC address of Router (LAN)
       */
      val p1 = Packet(IPv4Addr("1.2.4.8"))
      tcam.io.in.bits.cmds.poke(buildReadCmd())
      tcam.io.in.bits.content.poke(p1.toIP)
      tcam.io.out.valid.expect(true.B)

      /**
       * Assert router should route this packet to port 1
       */
      tcam.io.out.bits.expect(1.U)
      assert(!IPv4SubnetUtil.isInSubnet(p1.to, IPv4Addr("172.0.0.0"), IPv4Addr("255.255.0.0")))
      assert(!IPv4SubnetUtil.isInSubnet(p1.to, IPv4Addr("192.0.0.0"), IPv4Addr("255.255.255.0")))

      tcam.clock.step()

      /**
       * [Test 2]
       * Node B wants to send a packet to 172.0.1.3
       * Assume ARP cache in Node B contains MAC address of Router (LAN)
       */
      val p2 = Packet(IPv4Addr("172.0.1.3"))
      tcam.io.in.bits.cmds.poke(buildReadCmd())
      tcam.io.in.bits.content.poke(p2.toIP)
      tcam.io.out.valid.expect(true.B)

      /**
       * Assert router should route this packet to port 0
       */
      tcam.io.out.bits.expect(0.U)
      assert(IPv4SubnetUtil.isInSubnet(p2.to, IPv4Addr("172.0.0.0"), IPv4Addr("255.255.0.0")))

      tcam.clock.step()

      /**
       * [Test 3]
       * Node C wants to send a packet to 192.0.0.2
       * Assume ARP cache in Node C contains MAC address of Router (LAN)
       */
      val p3 = Packet(IPv4Addr("192.0.0.2"))
      tcam.io.in.bits.cmds.poke(buildReadCmd())
      tcam.io.in.bits.content.poke(p3.toIP)
      tcam.io.out.valid.expect(true.B)

      /**
       * Assert router should route this packet to port 2
       */
      tcam.io.out.bits.expect(2.U)
      assert(IPv4SubnetUtil.isInSubnet(p3.to, IPv4Addr("192.0.0.0"), IPv4Addr("255.255.255.0")))
    }
  }

  it should "be able to work as a IPv6 router (Level 3)" in {
    test(new TCAM(tcamParamsIPv6)) { tcam =>
      val sub0 = Subnet(
        "2041:0000:B40F:0000:0000:0000:AAAA:0000",
        "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FF00"
      )

      val sub1 = Subnet(
        "0000:0000:0000:0000:0000:0000:0000:0000",
        "0000:0000:0000:0000:0000:0000:0000:0000"
      )

      val sub2 = Subnet(
        "FC00:0000:0000:0000:0000:0000:0000:0000",
        "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:0000"
      )

      /**
       * Load routing table into TCAM using preferred index mode
       * as the index of the entry stored in TCAM correspond
       * to the port number in the router
       */
      tcam.io.in.valid.poke(true.B)
      tcam.io.in.bits.cmds.poke(buildWriteCmd())
      tcam.io.in.bits.index.valid.poke(true.B)
      tcam.io.in.bits.index.bits.poke(0.U) // Port 0
      tcam.io.in.bits.content.poke(IPv6(sub0.ip))
      tcam.io.in.bits.mask.poke(IPv6(sub0.mask, mask = true))
      tcam.io.out.valid.expect(true.B)
      tcam.io.out.bits.expect(0.U)

      tcam.clock.step()

      tcam.io.in.bits.index.bits.poke(1.U) // Port 1
      tcam.io.in.bits.content.poke(IPv6(sub1.ip))
      tcam.io.in.bits.mask.poke(IPv6(sub1.mask, mask = true))
      tcam.io.out.valid.expect(true.B)
      tcam.io.out.bits.expect(1.U)

      tcam.clock.step()

      tcam.io.in.bits.index.bits.poke(2.U) // Port 2
      tcam.io.in.bits.content.poke(IPv6(sub2.ip))
      tcam.io.in.bits.mask.poke(IPv6(sub2.mask, mask = true))
      tcam.io.out.valid.expect(true.B)
      tcam.io.out.bits.expect(2.U)

      tcam.clock.step()

      /**
       * [Test 1]
       * Node A wants to send a packet to 2001:0000:B40F:0000:0000:0000:AAAA:AAAA
       * Assume ARP cache in Node A contains MAC address of Router (LAN)
       */
      val p1 = Packet(IPv6Addr("2001:0000:B40F:0000:0000:0000:AAAA:AAAA"))
      tcam.io.in.bits.cmds.poke(buildReadCmd())
      tcam.io.in.bits.content.poke(p1.toIP)
      tcam.io.out.valid.expect(true.B)

      /**
       * Assert router should route this packet to port 1
       */
      tcam.io.out.bits.expect(1.U)
      assert(!IPv6SubnetUtil.isInSubnet(p1.to, IPv6Addr(sub0.ip), IPv6Addr(sub0.mask)))
      assert(!IPv6SubnetUtil.isInSubnet(p1.to, IPv6Addr(sub2.ip), IPv6Addr(sub2.mask)))

      tcam.clock.step()

      /**
       * [Test 2]
       * Node B wants to send a packet to 2041:0000:B40F:0000:0000:0000:AAAA:00CD
       * Assume ARP cache in Node B contains MAC address of Router (LAN)
       */
      val p2 = Packet(IPv6Addr("2041:0000:B40F:0000:0000:0000:AAAA:00CD"))
      tcam.io.in.bits.cmds.poke(buildReadCmd())
      tcam.io.in.bits.content.poke(p2.toIP)
      tcam.io.out.valid.expect(true.B)

      /**
       * Assert router should route this packet to port 0
       */
      tcam.io.out.bits.expect(0.U)
      assert(IPv6SubnetUtil.isInSubnet(p2.to, IPv6Addr(sub0.ip), IPv6Addr(sub0.mask)))

      tcam.clock.step()

      /**
       * [Test 3]
       * Node C wants to send a packet to FC00:0000:0000:0000:0000:0000:0000:0002
       * Assume ARP cache in Node C contains MAC address of Router (LAN)
       */
      val p3 = Packet(IPv6Addr("FC00:0000:0000:0000:0000:0000:0000:0002"))
      tcam.io.in.bits.cmds.poke(buildReadCmd())
      tcam.io.in.bits.content.poke(p3.toIP)
      tcam.io.out.valid.expect(true.B)

      /**
       * Assert router should route this packet to port 2
       */
      tcam.io.out.bits.expect(2.U)
      assert(IPv6SubnetUtil.isInSubnet(p3.to, IPv6Addr(sub2.ip), IPv6Addr(sub2.mask)))
    }
  }

  it should "be able to work as a switch (Level 2)" in {
    test(new CAM(camParams)) { cam =>

      /**
       * Node A wants to send a dataframe to Node C
       * Assume ARP cache in Node A is empty
       *
       * The destination of this dataframe is a broadcast MAC address (ARP)
       * since Node A doesn't know Node C's MAC address
       * (Node A only knows Node C's IP address)
       */
      val frame1 = DataFrame(0x6A478B327BC8L, 0xFFFFFFFFFFFFL)

      /**
       * [Test 1]
       * Switch receives frame1, stores Node A's MAC and send the dataframe to all other switch ports
       * Node A is connect to LAN0 in the switch
       */
      cam.io.in.valid.poke(true.B)
      cam.io.in.bits.index.valid.poke(true.B)
      cam.io.in.bits.index.bits.poke(0.U) // Port 0
      cam.io.in.bits.cmds.poke(buildWriteCmd())
      cam.io.in.bits.content.poke(frame1.fromMAC.U)
      cam.io.out.valid.expect(true.B)
      cam.io.out.bits.expect(0.U)

      cam.clock.step()

      /**
       * Only Node C respond to Node A's ARP request
       * Node C want to send a dataframe to Node A
       */
      val frame2 = DataFrame(0xA22CCF4ED0ABL, 0x6A478B327BC8L)

      /**
       * [Test 2]
       * Switch receives frame2, stores Node C's MAC
       * Node C is connect to LAN2 in the switch
       */
      cam.io.in.bits.index.bits.poke(2.U) // Port 2
      cam.io.in.bits.cmds.poke(buildWriteCmd())
      cam.io.in.bits.content.poke(frame2.fromMAC.U)
      cam.io.out.valid.expect(true.B)
      cam.io.out.bits.expect(2.U)

      cam.clock.step()

      /**
       * [Test 3]
       * Switch then find the port number of Node A in CAM and forward the dataframe to it
       */
      cam.io.in.bits.cmds.poke(buildReadCmd())
      cam.io.in.bits.content.poke(frame2.toMAC.U)
      cam.io.out.valid.expect(true.B)
      cam.io.out.bits.expect(0.U) // Port 0

      cam.clock.step()

      /**
       * Node B wants to send a dataframe to Node C
       * Assume ARP cache in Node B already contains Node C's MAC address
       */
      val frame3 = DataFrame(0x26C56DA8D4CEL, 0xA22CCF4ED0ABL)

      /**
       * [Test 4]
       * Switch receives frame3, stores Node B's MAC
       * Node B is connect to LAN1 in the switch
       */
      cam.io.in.bits.index.bits.poke(1.U) // Port 1
      cam.io.in.bits.cmds.poke(buildWriteCmd())
      cam.io.in.bits.content.poke(frame3.fromMAC.U)
      cam.io.out.valid.expect(true.B)
      cam.io.out.bits.expect(1.U)

      cam.clock.step()

      /**
       * [Test 5]
       * Switch then find the port number of Node C in CAM and forward the dataframe to it
       */
      cam.io.in.bits.cmds.poke(buildReadCmd())
      cam.io.in.bits.content.poke(frame3.toMAC.U)
      cam.io.out.valid.expect(true.B)
      cam.io.out.bits.expect(2.U) // Port 2

      /**
       * Node C wants to send a dataframe to Node B
       * Assume ARP cache in Node C already contains Node B's MAC address
       */
      val frame4 = DataFrame(0xA22CCF4ED0ABL, 0x26C56DA8D4CEL)

      /**
       * [Test 6]
       * Switch find the port number of Node B in CAM and forward the dataframe to it
       */
      cam.io.in.bits.cmds.poke(buildReadCmd())
      cam.io.in.bits.content.poke(frame4.toMAC.U)
      cam.io.out.valid.expect(true.B)
      cam.io.out.bits.expect(1.U) // Port 1
    }
  }
}

/**
 * Class used to represent a Subnet
 *
 * @param ip   IP in `String`
 * @param mask Netmask in `String`
 */
case class Subnet(ip: String, mask: String)

/**
 * Class used to represent a Packet in Level 3
 *
 * @param to IP address of sender in `NetworkAddr`
 */
case class Packet[T <: NetworkAddr](to: T) {
  val toIP = to.toBigInt.U
}

/**
 * Class used to represent a DataFrame in Level 2
 *
 * @param fromMAC MAC address of sender in `Long`
 * @param toMAC   MAC address of receiver in `Long`
 */
case class DataFrame(fromMAC: Long, toMAC: Long)
