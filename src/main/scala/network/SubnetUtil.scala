package network

object SubnetUtil {
  def getStartAddr(ip: IPv4Addr, mask: IPv4Addr): IPv4Addr = {
    assert(ip.width == mask.width)
    new IPv4Addr(ip.addr & mask.addr)
  }
}
