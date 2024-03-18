package integration

import network.IPv4Addr
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Switches are L2 devices (i.e. only look at MAC addresses)
 * Routers are L3 devices (i.e. only look at IP addresses)
 *
 * Assume:
 * - Switch and router are in the same device in this example
 *   (like the WiFi router in your home)
 * - LAN has IP range of 192.0.0.0/24, netmask of 255.255.255.0, default gateway of 192.0.0.1
 * - The router also has connected to WAN1 with IP of 172.0.0.5 and WAN2 with IP of 110.0.0.6
 *   (i.e. two ISPs (Internet Service Provider) provide internet to your router)
 * - Node {A, B, C} are all connected to switch
 * - Initial MAC lookup table (based on CAM) in Switch is empty
 * - Routing table (based on TCAM) in router is already populated
 * - ARP (Address Resolution Protocol) Cache in Node A is empty
 *
 * Name     IP          MAC
 * Router   172.0.0.5   DB:FE:EB:73:37:1D (WAN1)
 * Router   110.0.0.6   CD:7D:16:A6:9B:66 (WAN2)
 * Switch   192.0.0.1   EF:0B:BD:5E:F4:AA (LAN)
 * Node A   192.0.0.2   6A:47:8B:32:7B:C8
 * Node B   192.0.0.3   26:C5:6D:A8:D4:CE
 * Node C   192.0.0.4   A2:2C:CF:4E:D0:AB
 *
 * Routing table:
 * Port / Index   Content / Target IP
 * 0 (WAN1)       172.XXX.XXX.XXX (172.0.0.0/8)
 * 1 (WAN2)       XXX.XXX.XXX.XXX (0.0.0.0/0) (default route)
 * 2 (LAN)        192.000.000.XXX (192.0.0.0/24)
 *
 * Reference:
 * - https://www.youtube.com/watch?v=AhOU2eOpmX0
 * - https://www.youtube.com/watch?v=AzXys5kxpAM
 */
class IPv4IntegrationTester extends AnyFlatSpec {
  behavior of "IPv4IntegrationTester"
  it should "be able to work with every components" in {
    // Load routing table in TCAM
    // Node A wants to send a message to Node B

  }
}
