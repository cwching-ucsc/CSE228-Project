//// package CSE228-PROJECT
//
//import chisel3._
//import chisel3.util._
//
//case class cacheParams(capacity: Int, blockSize: Int, associativity: Int, addrLen: Int = 8, bitsPerWord: Int = 8) {
//	require((1 << addrLen) >= capacity)
//	require(capacity > blockSize)
//	require(isPow2(capacity) && isPow2(blockSize) && isPow2(associativity) && isPow2(bitsPerWord))
//
//	val numExtMemBlocks = (1 << addrLen) / blockSize
//	val memBlockAddrBits = log2Ceil(numExtMemBlocks)
//
//	val numSets = capacity / blockSize / associativity
//	val numOffsetBits = log2Ceil(blockSize)
//	val numIndexBits = log2Ceil(numSets)
//	val numTagBits = addrLen - (numOffsetBits + numIndexBits)
//}
//
//// bits: 0, 1, ..., 63 -> capacity = 64 bits, cache saves up to 64 bits data.
//// bits: 0, 1, ..., 31 | 32, 33, ..., 63 -> dataDepth = 2, cache stores two IPs.
//// bits: [0], 0, 1, ..., 31 | [1], 32, 33, ..., 63 -> tagWidth = 1, one bit represents the tag (or key), it can be more, depending on the rize of routing table
//
//
//case class CAMParams(capacity: Int, IPDepth: Int, addrLen: Int = 8, bitsPerIP: Int = 32, entryValidity: Boolean = false) {
//	require((1 << addrLen) >= capacity)
//	require(capacity > dataDepth)
//	require(isPow2(capacity) && isPow2(IPDepth) && isPow2(bitsPerWord))
//
//	val numIPTag =
//
//	val numExtMemBlocks = (1 << addrLen) / blockSize
//	val memBlockAddrBits = log2Ceil(numExtMemBlocks)
//
//	val numOffsetBits = log2Ceil(bitsPerIP)
//	val numIndexBits = log2Ceil(numSets)
//	val numTagBits = addrLen - (numOffsetBits + numIndexBits)
//}
//
//
//
//class CAM(val width: Int, val depth: Int) extends Module {
//    val io = IO(new Bundle {
//        val writeEnable = Input(Bool())
//        val writeData = Input(UInt(width.W))
//        val writeAddr = Input(UInt(log2Ceil(depth).W))
//        val searchData = Input(UInt(width.W))
//        val found = Output(Bool())
//        val foundAddr = Output(UInt(log2Ceil(depth).W))
//    })
//
//    // register init to store data and address
//    val memory = Reg(Vec(depth, UInt(width.W)))
//
//    when(writeEnable) {
//        memory(io.writeAddr) := io.writeData
//    }
//}
