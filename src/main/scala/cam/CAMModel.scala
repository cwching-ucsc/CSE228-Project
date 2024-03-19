package cam

import chisel3._
import chisel3.util._
import dataclass.data


/**
 * This file contains an implementation of CAM (Content Addressable Memory)
 *
 * References: The following code are modified from previous homeworks
 * (XORCipher.scala, Cache.scala, CacheModel.scala and MalMulSC.scala)
 *
 * @author Tongze Wang, Cheng-Wei Ching
 */



/**
 * Case class designed to encapsulate parameters for CAM
 *
 * @param capacity	Number of bits in CAM
 * @param bitsPerIP	Number of bits per MAC addr in CAM
 */
case class CAMParams_FSM(capacity: Int, bitsPerIP: Int) {
	require(capacity > bitsPerIP)
	require(isPow2(capacity) && isPow2(bitsPerIP) && (capacity % bitsPerIP == 0))

	val numIPTag = capacity / bitsPerIP
	val numIPTagBits = numIPTag
	val numOffsetBits = bitsPerIP
}

// Refereces: code change from previous homework (CacheModel.scala and MalMulSC.scala)

class FIFOCAMModel(p: CAMParams_FSM) extends Module {
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
	val tagCounter = Counter(p.numIPTag)
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

	switch(state) {
		is(sIdle) {
			writtenValid := false.B
			lookupValid := false.B
			lookupBoolValid := false.B
			io.in.ready := true.B
			when(io.in.fire) {
				dataReg := io.in.bits.loadData
				opReg := io.in.bits.opCode				
				state := sCompute
			}
		}
		is(sCompute) {
			switch(opReg) {
				is(0.U) { // write operation
					when(!validArray(tagCounter.value)) {
						writtenValid := true.B
						memory(tagCounter.value) := dataReg
						validArray(tagCounter.value) := false.B
						writtenResultReg := tagCounter.value
						tagCounter.inc()
						state := sIdle
					}
				}
				is(1.U) { // lookup operation
					(memory.zipWithIndex).foreach { case (result, index) =>
						when(result === dataReg) {
							lookupValid := true.B
							lookupBoolValid := true.B
							lookupBoolReg := true.B
							lookupReg := index.U
							state := sIdle
						}
					}
				}
				is(2.U) { // delete operation
					memory(tagCounter.value) := dataReg
					validArray(tagCounter.value) := true.B
					state := sIdle
				}
			}
		}
	}
	io.in.ready := state === sIdle

}
