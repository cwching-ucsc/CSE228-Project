error id: file://<WORKSPACE>/src/main/scala/cam/CAMModel.scala:[2547..2548) in Input.VirtualFile("file://<WORKSPACE>/src/main/scala/cam/CAMModel.scala", "// package CSE228-PROJECT

import chisel3._
import chisel3.util._
import CacheModel.CacheBlockModel
import scala.collection.mutable.ArrayBuffer

// case class cacheParams(capacity: Int, blockSize: Int, associativity: Int, addrLen: Int = 8, bitsPerWord: Int = 8) {
// 	require((1 << addrLen) >= capacity)
// 	require(capacity > blockSize)
// 	require(isPow2(capacity) && isPow2(blockSize) && isPow2(associativity) && isPow2(bitsPerWord))

// 	val numExtMemBlocks = (1 << addrLen) / blockSize
// 	val memBlockAddrBits = log2Ceil(numExtMemBlocks)

// 	val numSets = capacity / blockSize / associativity
// 	val numOffsetBits = log2Ceil(blockSize)
// 	val numIndexBits = log2Ceil(numSets)
// 	val numTagBits = addrLen - (numOffsetBits + numIndexBits)
// }

// bits: 0, 1, ..., 63 -> capacity = 64 bits, cache saves up to 64 bits data.
// bits: 0, 1, ..., 31 | 32, 33, ..., 63 -> dataDepth = 2, cache stores two IPs.
// bits: [00], 0, 1, ..., 31 | [01], 32, 33, ..., 63 -> tagWidth = 2, one bit represents the tag (or key), it can be more, depending on the rize of routing table


case class CAMParams(capacity: Int, bitsPerIP: Int) {
	require(capacity > bitsPerIP)
	require(isPow2(capacity) && isPow2(bitsPerIP) && (capacity % bitsPerIP == 0))

	val numIPTag = capacity / bitsPerIP
	val numIPTagBits = log2Ceil(numIPTag)
	val numOffsetBits = log2Ceil(bitsPerIP)
}


// abstract class CAMModel(p: CAMParams) extends Module {
//   // Insert a new entry into the CAM.
//   // 'data' represents the data to be stored, which might be an IP address or another type of data.
//   // The method might return a Boolean indicating success or failure, or perhaps an optional index/tag if the CAM assigns one.
//   def insert(data: UInt): Boolean

//   // Look up an entry in the CAM based on the provided data.
//   // 'data' is the content to search for in the CAM.
//   // The method might return an optional type indicating the found entry's index/tag or additional data associated with the entry.
//   def lookUp(data: UInt): UInt

//   // Delete an entry from the CAM.
//   // This could be based on the data content itself or an index/tag if the CAM supports direct access by index/tag.
//   // Returns a Boolean indicating success or failure.
//   def delete(data: UInt): Boolean

//   // Update an existing entry in the CAM.
//   // 'oldData' is the existing content to be updated, and 'newData' is the new content to replace it.
//   // Returns a Boolean indicating success or failure.
//   def update(oldData: UInt, newData: UInt): Boolean
// }

class (CAM(p: CAMParams) extends Module) {
	val io = IO(new Bundle {
		val writeEnable = Input(Bool())
		val writeData = Input(UInt((p.numOffsetBits).W))
		val lookupData = Input(UInt((p.numOffsetBits).W))
		val found = Output(Bool())
		val foundAddr = Output(UInt((p.).W))
	})
}

class CAM(val width: Int, val depth: Int) extends Module {
    val io = IO(new Bundle {
        val writeEnable = Input(Bool())
        val writeData = Input(UInt(width.W))
        val writeAddr = Input(UInt(log2Ceil(depth).W))
        val searchData = Input(UInt(width.W))
        val found = Output(Bool())
        val foundAddr = Output(UInt(log2Ceil(depth).W))
    })

    // register init to store data and address
    val memory = Reg(Vec(depth, UInt(width.W)))

    when(writeEnable) {
        memory(io.writeAddr) := io.writeData
    }
}
")
file://<WORKSPACE>/src/main/scala/cam/CAMModel.scala
file://<WORKSPACE>/src/main/scala/cam/CAMModel.scala:59: error: expected identifier; obtained lparen
class (CAM(p: CAMParams) extends Module) {
      ^
#### Short summary: 

expected identifier; obtained lparen