package cam

import chisel3._
import chisel3.util._

/**
 * This file contains an implementation of TCAM (Ternary Content Addressable Memory)
 *
 * References: The following code are modified from previous homeworks
 * (XORCipher.scala, Cache.scala, CacheModel.scala and MalMulSC.scala)
 *
 * @author Tongze Wang, Cheng-wei Ching
 */

class TCAM(p: CAMParams) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Valid(new Bundle {
      /**
       * CAM command that will be executed
       */
      val cmds = new CAMCmds

      /**
       * Content of the entry
       */
      val content = UInt(p.width.W)

      /**
       * Ternary bits of the entry (only used when `cmds` is write command)
       */
      val mask = UInt(p.width.W)

      /**
       * Preferred index to store this entry
       * (only used when it's valid and `cmds` is write command)
       */
      val index = Flipped(Valid(UInt(p.resultWidth.W)))
    }))

    /**
     * Actual index that stores the entry (valid bit decides if the operation is successful or not)
     */
    val out = Valid(UInt(p.resultWidth.W))

    /**
     * Indicator suggests the TCAM is full and has no space to store another entry
     */
    val full = Output(Bool())
  })

  val memory = Reg(Vec(p.entries, UInt(p.width.W)))
  val masks = Reg(Vec(p.entries, UInt(p.width.W)))
  val emptyFlags = RegInit(VecInit(Seq.fill(p.entries)(true.B)))
  val usedCount = RegInit(0.U(p.resultWidth.W))

  // Set default output
  io.out.valid := false.B
  io.out.bits := 0.U
  io.full := usedCount === p.entries.U

  def bitCheckHelper(content: Bool, mask: Bool, target: Bool): Bool = {
    Mux(mask, content === target || !content === target, content === target)
  }

  def checkBits(content: UInt, mask: UInt, target: UInt): UInt = {
    PopCount((0 until p.width).map { i => bitCheckHelper(content(i), mask(i), target(i)) })
  }

  def validHelper(idx: UInt): Bool = {
    checkBits(memory(idx), masks(idx), io.in.bits.content) === p.width.U && !emptyFlags(idx)
  }

  def findMatchIdx: UInt = {
    val resultFlags = (0 until p.entries)
      .map { i => (validHelper(i.U), PopCount(masks(i))) }

    /**
     * Find index from resultFlags that match entry with minimum ternary bits
     */
    def minPriorityEncoder(idx: Int, currMinIdx: Int, count: UInt): UInt = {
      def matchHelper: UInt = {
        Mux(
          resultFlags(idx)._2 < count,
          minPriorityEncoder(idx + 1, idx, resultFlags(idx)._2),
          minPriorityEncoder(idx + 1, currMinIdx, count)
        )
      }

      if (idx < p.entries) {
        Mux(resultFlags(idx)._1, matchHelper, minPriorityEncoder(idx + 1, currMinIdx, count))
      } else {
        currMinIdx.U
      }
    }

    minPriorityEncoder(0, p.entries - 1, p.width.U + 1.U)
  }

  when(io.in.fire) {
    when(io.in.bits.cmds.write) {
      when(usedCount < p.entries.U) {
        val writeIdx = Mux(io.in.bits.index.valid, io.in.bits.index.bits, PriorityEncoder(emptyFlags))
        assert(0.U <= writeIdx && writeIdx < p.entries.U)
        when(emptyFlags(writeIdx)) {
          emptyFlags(writeIdx) := false.B
          memory(writeIdx) := io.in.bits.content
          masks(writeIdx) := io.in.bits.mask
          usedCount := usedCount + 1.U
          io.out.valid := true.B
        } otherwise {
          io.out.valid := false.B
        }
        io.out.bits := writeIdx
      }
    }

    when(io.in.bits.cmds.read || io.in.bits.cmds.delete) {
      val targetIdx = findMatchIdx
      io.out.valid := validHelper(targetIdx)
      io.out.bits := targetIdx

      when(io.in.bits.cmds.delete && io.out.valid) {
        memory(targetIdx) := 0.U
        masks(targetIdx) := 0.U
        emptyFlags(targetIdx) := true.B
        usedCount := usedCount - 1.U
      }
    }

    when(io.in.bits.cmds.reset) {
      memory.foreach { i => i := 0.U }
      masks.foreach { i => i := 0.U }
      emptyFlags.foreach { i => i := true.B }
      usedCount := 0.U
      io.out.valid := true.B
      io.out.bits := (p.entries - 1).U
    }
  }
}
