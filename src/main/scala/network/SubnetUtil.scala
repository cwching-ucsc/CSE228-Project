package network

trait SubnetUtil[T <: NetworkAddr] {
  /**
   * Get the start address of the subnet
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `NetworkAddr`
   */
  def getStartAddr(ip: T, mask: T): T

  /**
   * Get the end address of the subnet
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `NetworkAddr`
   */
  def getEndAddr(ip: T, mask: T): T

  /**
   * Get all addresses of the subnet in a `Vector`
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `Vector[NetworkAddr]`
   */
  def getRangeVector(ip: T, mask: T): Vector[T]

  /**
   * Check if given target IP is in the subnet
   * @param target `NetworkAddr` Target
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `Boolean`
   */
  def isInSubnet(target: T, ip: T, mask: T): Boolean

  /**
   * Check if given target IP is a special broadcast address
   * @param target `NetworkAddr` Target
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `Boolean`
   */
  def isBroadCastAddr(target: T, ip: T, mask: T): Boolean
}
