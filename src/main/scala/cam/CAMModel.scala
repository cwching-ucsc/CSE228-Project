package cam

import chisel3._
import chisel3.util._

/**
 * This file contains an implementation of CAM (Content Addressable Memory)
 *
 * References: The following code are modified from previous homeworks
 * (XORCipher.scala, Cache.scala, CacheModel.scala and MalMulSC.scala)
 *
 * @author Cheng-wei Ching, Tongze Wang
 */

/**
 * Case class designed to encapsulate parameters for CAM
 *
 * @param entries Number of content entries in CAM
 * @param width   Width of each content entry in CAM
 */
case class CAMParams(entries: Int, width: Int) {
  require(entries > 0 && width > 0)

  /**
   * Width of result flag returned by CAM
   */
  val resultWidth: Int = log2Ceil(entries)
}

class CAMCmds extends Bundle {
  val write: Bool = Input(Bool())
  val read: Bool = Input(Bool())
  val delete: Bool = Input(Bool())
  val reset: Bool = Input(Bool())
}

object CAMState extends ChiselEnum {
  val idle, writing, reading, deleting = Value
}

class CAM(p: CAMParams) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Bundle {
      val cmds = new CAMCmds
      val content = UInt(p.width.W)
    }))

    val out = Valid(UInt(p.resultWidth.W))
    val full = Output(Bool())
    val state = Output(CAMState())
  })

  val memory = Reg(Vec(p.entries, UInt(p.width.W)))
  val emptyFlags = RegInit(VecInit(Seq.fill(p.entries)(true.B)))
  val usedCount = RegInit(0.U(p.resultWidth.W))

  val state = RegInit(CAMState.idle)

  // Set default output
  io.state := state
  io.out.valid := false.B
  io.out.bits := 0.U
  io.in.ready := false.B
  io.full := usedCount === p.entries.U

  switch (state) {
    is (CAMState.idle) {
      // CAM is idle, ready to execute commands
      io.in.ready := true.B

      when (io.in.fire) {
        when (io.in.bits.cmds.write) {
          when (usedCount < p.entries.U) {
            val writeIdx = PriorityEncoder(emptyFlags)
            emptyFlags(writeIdx) := false.B
            memory(writeIdx) := io.in.bits.content
            usedCount := usedCount + 1.U

            io.out.valid := true.B
            io.out.bits := writeIdx
          }
        }
      }
    }
  }
}
