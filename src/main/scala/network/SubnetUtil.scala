package network

trait SubnetUtil[T, U <: NetworkAddr[T]] {
  /**
   * Get the start address of the subnet
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `NetworkAddr`
   */
  def getStartAddr(ip: U, mask: U): U

  /**
   * Get the end address of the subnet
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `NetworkAddr`
   */
  def getEndAddr(ip: U, mask: U): U

  /**
   * Get all addresses of the subnet in a `Vector`
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `Vector[NetworkAddr]`
   */
  def getRangeVector(ip: U, mask: U): Vector[U]

  /**
   * Check if given target IP is in the subnet
   * @param target `NetworkAddr` Target
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `Boolean`
   */
  def isInSubnet(target: U, ip: U, mask: U): Boolean

  /**
   * Check if given target IP is a special broadcast address
   * @param target `NetworkAddr` Target
   * @param ip `NetworkAddr` IP
   * @param mask `NetworkAddr` Network mask
   * @return `Boolean`
   */
  def isBroadCastAddr(target: U, ip: U, mask: U): Boolean
}
