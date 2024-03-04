package chiselRouter

object SubnetUtil {
  def getStartAddr(ip: NetworkAddr, mask: NetworkAddr): NetworkAddr = {
    assert(ip.width == mask.width)
    new NetworkAddr(ip.addr & mask.addr)
  }
}
