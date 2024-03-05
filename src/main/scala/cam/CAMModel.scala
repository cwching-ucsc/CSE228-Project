package cam

import chisel3._
import chisel3.util._


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

// Refereces: code change from previous homework (CacheModel.scala and MalMulSC.scala)

class FIFOCAMModel(p: CAMParams) extends Module {
	val io = IO(new Bundle {
		val in = Flipped(Decoupled(new Bundle {
			val opCode = Input(UInt(2.W))
			val loadData = Input(UInt((p.numOffsetBits).W))			
		}))
		val found = Output(Bool()) // TODO: change it to io.out.valid
		val foundAddr = Output(UInt((p.numIPTagBits).W))
	})

	val dataReg = Reg(UInt(p.numOffsetBits.W))
	val opReg = Reg(UInt(2.W))
	val memory = Reg(Vec(p.numIPTag, UInt(p.numOffsetBits.W)))
	val validArray = RegInit(VecInit(Seq.fill(p.numIPTag)(false.B)))
	val writePointer = RegInit(0.U(log2Ceil(p.numIPTag).W))



	val sIdle :: sCompute :: Nil = Enum(2)
 	val state = RegInit(sIdle)
	

	when(io.in.fire) {
		dataReg := io.in.bits.loadData
		opReg := io.in.bits.opCode
		state := sCompute

	}

	when(state === sCompute) {
		switch(opReg) {
			is(0.U) { // write operation
				when(!validArray(writePointer)) { //check if the current position is valid
					memory(writePointer) := dataReg
					validArray(writePointer) := false.B
					writePointer := Mux(writePointer === (p.numIPTag.U - 1.U), 0.U, writePointer + 1.U)
				} //TODO: what if the current position is not valid but the data needs to be write?
			}
			is(1.U) { // lookup operation
				val lookupResults = memory.zip(validArray).map { case (data, valid) =>
					valid && (data === dataReg)
				}

				// Initialize found flag and found address
				io.found := false.B
				io.foundAddr := 0.U

				// Manually iterate to find the index of the first match
				val foundIndex = Wire(UInt(log2Ceil(p.numIPTag).W))
				foundIndex := 0.U
				(lookupResults.zipWithIndex.reverse).foreach { case (result, index) =>
					when(result) {
					io.found := true.B
					foundIndex := index.U
					}
				}

				// Update the foundAddr with the foundIndex if found
				io.foundAddr := Mux(io.found, foundIndex, 0.U)

				//TODO: what if there are duplicate tags feasible for the lookup?
				//TODO: returning 0 is a good idea if 0 represents a tag as well?
			}
			is(2.U) { // delete operation
				for (i <- 0 until p.numIPTag) {
					when(memory(i.U) === dataReg && validArray(i.U)) {
						validArray(i.U) := false.B
					}
				}
			}
		}
	} .elsewhen (state === sIdle) {
		when(io.in.valid) {
			io.in.ready := true.B
			dataReg := io.in.bits.loadData
			state := sCompute
     }
	}

}
