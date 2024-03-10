package cam

import chisel3._
import chisel3.util._

/**
 * This file contains an implementation of CAM (Content Addressable Memory)
 *
 * References: The following code are adapted from previous homeworks (CacheModel.scala and MalMulSC.scala)
 */

/**
 * Case class designed to encapsulate parameters for CAM
 *
 * @param entries Number of content entries in CAM
 * @param width   Width of each content entry in CAM
 */
case class CAMParams(entries: Int, width: Int) {
  require(entries > 0 && width > 0)

  val numIPTag = entries
  val numIPTagBits = log2Ceil(numIPTag)
  val numOffsetBits = log2Ceil(width)
}

class FIFOCAMModel(p: CAMParams) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Bundle {
      val opCode = Input(UInt(2.W))
      val loadData = Input(UInt((p.numOffsetBits).W))
    }))
    // val found = Output(Bool()) // TODO: change it to io.out.valid
    // val foundAddr = Output(UInt((p.numIPTag).W))
    val writtenIndex = Valid((UInt(p.numIPTagBits.W)))
    val resultVec = Valid((Vec(p.numIPTag, Bool())))

  })

  val dataReg = Reg(UInt(p.numOffsetBits.W))
  val opReg = Reg(UInt(2.W))
  val memory = Reg(Vec(p.numIPTag, UInt(p.numOffsetBits.W)))
  val validArray = RegInit(VecInit(Seq.fill(p.numIPTag)(false.B)))
  val writePointer = RegInit(0.U(log2Ceil(p.numIPTag).W))
  // val resultReg = RegInit(VecInit(Seq.fill(p.numIPTag)(false.B)))
  // TODO: to check how to use dut.io.resultVec(index).expect(value) to check the expect.
  val resultReg = VecInit(Seq.fill(p.numIPTag)(false.B))
  val writtenResultReg = RegInit(false.B)

  val writtenValid = RegInit(false.B)
  val lookupValid = RegInit(false.B)

  io.resultVec.valid := lookupValid
  io.writtenIndex.valid := writtenValid

  io.resultVec.bits := resultReg
  io.writtenIndex.bits := writtenResultReg


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
          writtenValid := true.B
        } //TODO: what if the current position is not valid but the data needs to be write?
      }
      is(1.U) { // lookup operation
        val lookupResults = memory.zip(validArray).map { case (data, valid) =>
          valid && (data === dataReg)
        }

        resultReg := lookupResults
        lookupValid := true.B


        // Initialize found flag and found address
        // io.found := false.B
        // io.foundAddr := 0.U

        // // Manually iterate to find the index of the first match
        // val foundIndex = Wire(UInt(log2Ceil(p.numIPTag).W))
        // foundIndex := 0.U
        // (lookupResults.zipWithIndex.reverse).foreach { case (result, index) =>
        // 	when(result) {
        // 	io.found := true.B
        // 	foundIndex := index.U
        // 	}
        // }

        // Update the foundAddr with the foundIndex if found
        // io.foundAddr := Mux(io.found, foundIndex, 0.U)

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
  }.elsewhen(state === sIdle) {
    when(io.in.valid) {
      io.in.ready := true.B
      dataReg := io.in.bits.loadData
      state := sCompute
    }
  }

  io.in.ready := state === sIdle

}
