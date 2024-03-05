package network

object IPv4SubnetUtil extends SubnetUtil[IPv4Addr] {
  override def getStartAddr(ip: IPv4Addr, mask: IPv4Addr): IPv4Addr = {
    assert(ip.width == mask.width)
    IPv4Addr(ip.addr & mask.addr)
  }

  override def getEndAddr(ip: IPv4Addr, mask: IPv4Addr): IPv4Addr = {
    assert(ip.width == mask.width)
    val endAddrNumber = getStartAddr(ip, mask).addr + IPv4Addr.MAX_NUM - mask.addr
    IPv4Addr(endAddrNumber)
  }

  override def getRangeVector(ip: IPv4Addr, mask: IPv4Addr): Vector[IPv4Addr] = {
    val startAddrNumber = getStartAddr(ip, mask).addr
    val endAddrNumber = getEndAddr(ip, mask).addr
    Vector.range(startAddrNumber, endAddrNumber + 1).map(IPv4Addr(_))
  }

  override def isInSubnet(target: IPv4Addr, ip: IPv4Addr, mask: IPv4Addr): Boolean = {
    getRangeVector(ip, mask).contains(target)
  }

  override def isBroadCastAddr(target: IPv4Addr, ip: IPv4Addr, mask: IPv4Addr): Boolean = {
    target == getEndAddr(ip, mask)
  }
}
