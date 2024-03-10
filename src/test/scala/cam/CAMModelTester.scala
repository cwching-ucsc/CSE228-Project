package cam

import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class CAMModelTester extends AnyFlatSpec with ChiselScalatestTester {
  // 3 * 32 bit entries
  val p = CAMParams(3, 32)

  def outputCheck(dut: CAM): Unit = {
    dut.io.full.expect(false.B)
    dut.io.out.valid.expect(true.B)
  }

  def buildWriteCmd(): CAMCmds = {
    new CAMCmds().Lit(
      _.write -> true.B,
      _.read -> false.B,
      _.delete -> false.B,
      _.reset -> false.B)
  }

  def buildReadCmd(): CAMCmds = {
    new CAMCmds().Lit(
      _.write -> false.B,
      _.read -> true.B,
      _.delete -> false.B,
      _.reset -> false.B)
  }

  def buildDeleteCmd(): CAMCmds = {
    new CAMCmds().Lit(
      _.write -> false.B,
      _.read -> false.B,
      _.delete -> true.B,
      _.reset -> false.B)
  }

  def buildResetCmd(): CAMCmds = {
    new CAMCmds().Lit(
      _.write -> false.B,
      _.read -> false.B,
      _.delete -> false.B,
      _.reset -> true.B)
  }

  behavior of "CAM"
  it should "able to write 1 entry into memory" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(1.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "able to write 2 entries into different slots" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(10.U)

      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(20.U)

      outputCheck(dut)
      dut.io.out.bits.expect(1.U)
    }
  }

  it should "able to write 3 entries into different slots and report full" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())

      dut.io.in.bits.content.poke(10.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(20.U)
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(30.U)
      outputCheck(dut)
      dut.io.out.bits.expect(2.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(40.U)
      dut.io.full.expect(true.B)
      dut.io.out.valid.expect(false.B)
    }
  }

  it should "able to final available slot to write" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(10.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(20.U)
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(30.U)
      outputCheck(dut)
      dut.io.out.bits.expect(2.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(40.U)
      dut.io.full.expect(true.B)
      dut.io.out.valid.expect(false.B)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildDeleteCmd())
      dut.io.in.bits.content.poke(20.U)
      dut.io.full.expect(true.B)
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(50.U)
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)
    }
  }

  it should "able to write and read 1 entry" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(1.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(1.U)
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "report not valid when entry not found in read" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(1.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(2.U)
      dut.io.out.valid.expect(false.B)
    }
  }

  it should "able to delete 1 entry" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(1.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildDeleteCmd())
      dut.io.in.bits.content.poke(1.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "report not valid when entry not found in delete" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildDeleteCmd())
      dut.io.in.bits.content.poke(2.U)
      dut.io.out.valid.expect(false.B)
    }
  }

  it should "able to reset all entries" in {
    test(new CAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(1.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(2.U)
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(1.U)
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(2.U)
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildResetCmd())
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect((p.entries - 1).U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(1.U)
      dut.io.out.valid.expect(false.B)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(1.U)
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }
}