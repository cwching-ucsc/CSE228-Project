package network

object IPv6SubnetUtil extends TSubnetUtil[IPv6Addr] {
  override def getStartAddr(ip: IPv6Addr, mask: IPv6Addr): IPv6Addr = {
    assert(ip.width == mask.width)
    IPv6Addr(ip.toBigInt & mask.toBigInt)
  }

  override def getEndAddr(ip: IPv6Addr, mask: IPv6Addr): IPv6Addr = {
    assert(ip.width == mask.width)
    val endAddrNumber = getStartAddr(ip, mask).toBigInt + BigInt(1, Array.fill(16)(255.toByte)) - mask.toBigInt
    IPv6Addr(endAddrNumber)
  }

  override def getRangeVector(ip: IPv6Addr, mask: IPv6Addr): Vector[IPv6Addr] = {
    val startAddrNumber = getStartAddr(ip, mask).toBigInt
    val endAddrNumber = getEndAddr(ip, mask).toBigInt
    Vector.range(startAddrNumber, endAddrNumber + 1).map(IPv6Addr(_))
  }

  override def isInSubnet(target: IPv6Addr, ip: IPv6Addr, mask: IPv6Addr): Boolean = {
    getRangeVector(ip, mask).contains(target)
  }

  override def isBroadCastAddr(target: IPv6Addr, ip: IPv6Addr, mask: IPv6Addr): Boolean = {
    target == getEndAddr(ip, mask)
  }
}
