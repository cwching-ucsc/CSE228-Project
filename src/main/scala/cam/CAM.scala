package cam

import chisel3._
import chisel3.util._

/**
 * This file contains an implementation of CAM (Content Addressable Memory)
 *
 * References: The following code are modified from previous homeworks
 * (XORCipher.scala, Cache.scala, CacheModel.scala and MalMulSC.scala)
 *
 * @author Tongze Wang, Cheng-wei Ching
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

class CAM(p: CAMParams) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Valid(new Bundle {
      val cmds = new CAMCmds
      val content = UInt(p.width.W)
    }))

    val out = Valid(UInt(p.resultWidth.W))
    val full = Output(Bool())
  })

  val memory = Reg(Vec(p.entries, UInt(p.width.W)))
  val emptyFlags = RegInit(VecInit(Seq.fill(p.entries)(true.B)))
  val usedCount = RegInit(0.U(p.resultWidth.W))

  // Set default output
  io.out.valid := false.B
  io.out.bits := 0.U
  io.full := usedCount === p.entries.U

  def validHelper(idx: UInt): Bool = {
    memory(idx) === io.in.bits.content && !emptyFlags(idx)
  }

  def findMatchIdx: UInt = {
    val resultFlags = (0 until p.entries)
      .map { i => validHelper(i.U) }
    PriorityEncoder(resultFlags)
  }

  when(io.in.fire) {
    when(io.in.bits.cmds.write) {
      when(usedCount < p.entries.U) {
        val writeIdx = PriorityEncoder(emptyFlags)
        emptyFlags(writeIdx) := false.B
        memory(writeIdx) := io.in.bits.content
        usedCount := usedCount + 1.U

        io.out.valid := true.B
        io.out.bits := writeIdx
      }
    }

    when(io.in.bits.cmds.read || io.in.bits.cmds.delete) {
      val targetIdx = findMatchIdx
      io.out.valid := validHelper(targetIdx)
      io.out.bits := targetIdx

      when(io.in.bits.cmds.delete && io.out.valid) {
        memory(targetIdx) := 0.U
        emptyFlags(targetIdx) := true.B
        usedCount := usedCount - 1.U
      }
    }

    when(io.in.bits.cmds.reset) {
      memory.foreach { i => i := 0.U }
      emptyFlags.foreach { i => i := true.B }
      usedCount := 0.U
      io.out.valid := true.B
      io.out.bits := (p.entries - 1).U
    }
  }
}
