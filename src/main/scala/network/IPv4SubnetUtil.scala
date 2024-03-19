package network

object IPv4SubnetUtil extends TSubnetUtil[IPv4Addr] {
  override def getStartAddr(ip: IPv4Addr, mask: IPv4Addr): IPv4Addr = {
    assert(ip.width == mask.width)
    IPv4Addr(ip.toBigInt & mask.toBigInt)
  }

  override def getEndAddr(ip: IPv4Addr, mask: IPv4Addr): IPv4Addr = {
    assert(ip.width == mask.width)
    val endAddrNumber = getStartAddr(ip, mask).toBigInt + BigInt(1, Array.fill(4)(255.toByte)) - mask.toBigInt
    IPv4Addr(endAddrNumber)
  }

  override def getRangeVector(ip: IPv4Addr, mask: IPv4Addr): Vector[IPv4Addr] = {
    val startAddrNumber = getStartAddr(ip, mask).toBigInt
    val endAddrNumber = getEndAddr(ip, mask).toBigInt
    Vector.range(startAddrNumber, endAddrNumber + 1).map(IPv4Addr(_))
  }

  override def isInSubnet(target: IPv4Addr, ip: IPv4Addr, mask: IPv4Addr): Boolean = {
    getRangeVector(ip, mask).contains(target)
  }

  override def isBroadCastAddr(target: IPv4Addr, ip: IPv4Addr, mask: IPv4Addr): Boolean = {
    target == getEndAddr(ip, mask)
  }
}
