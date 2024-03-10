package cam

import chisel3._
import chisel3.util._

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
		// val found = Output(Bool()) // TODO: change it to io.out.valid
		// val foundAddr = Output(UInt((p.numIPTag).W))

		val writtenIndex = Valid((UInt(p.numIPTagBits.W)))
		val lookupResult = Valid(UInt(p.numIPTagBits.W))
		val lookupFound = Valid(Bool())

	})

	val dataReg = Reg(UInt(p.numOffsetBits.W))
	val opReg = Reg(UInt(2.W))
	val memory = Reg(Vec(p.numIPTag, UInt(p.numOffsetBits.W)))
	val validArray = RegInit(VecInit(Seq.fill(p.numIPTag)(false.B)))
	val writePointer = RegInit(0.U(log2Ceil(p.numIPTag).W))
	// TODO: to check how to use dut.io.resultVec(index).expect(value) to check the expect.
	val lookupReg = Reg(UInt(p.numIPTagBits.W))
	val lookupBoolReg = RegInit(false.B)
	val writtenResultReg = Reg(UInt(p.numIPTagBits.W))


	val lookupValid = RegInit(false.B)
	val lookupBoolValid = RegInit(false.B)
	val writtenValid = RegInit(false.B)
	

	io.lookupResult.valid := lookupValid
	io.writtenIndex.valid := writtenValid
	io.lookupFound.valid := lookupBoolValid

	io.lookupResult.bits := lookupReg
	io.writtenIndex.bits := writtenResultReg
	io.lookupFound.bits := lookupBoolReg


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
					writtenResultReg := writePointer
					validArray(writePointer) := false.B
					writePointer := Mux(writePointer === (p.numIPTag.U - 1.U), 0.U, writePointer + 1.U)
					writtenValid := true.B
					state := sIdle
					
				} //TODO: what if the current position is not valid but the data needs to be write?
			}
			is(1.U) { // lookup operation
				// val lookupResults = memory.zip(validArray).map { case (data, valid) =>
				// 	valid && (data === dataReg)
				// }

				(memory.zipWithIndex).foreach { case (result, index) =>
					when(result === dataReg) {
						lookupValid := true.B
						lookupBoolValid := true.B
						lookupBoolReg := true.B
						lookupReg := index.U
						state := sIdle
						// lock
					}
				}
				

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
		io.in.ready := true.B
		when(io.in.fire) {
			dataReg := io.in.bits.loadData
			opReg := io.in.bits.opCode
			state := sCompute
     }
	}

	io.in.ready := state === sIdle

}
