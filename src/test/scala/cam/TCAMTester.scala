package cam

import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class TCAMTester extends AnyFlatSpec with ChiselScalatestTester {
  // 3 * 4 bit entries
  val p = CAMParams(3, 4)

  def outputCheck(dut: TCAM): Unit = {
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

  behavior of "TCAM"
  it should "able to write 1 entry into memory" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x1.U) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "able to write 2 entries into different slots" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0xA.U) // 1010
      dut.io.in.bits.mask.poke(0x1.U) // 0001

      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(0xB.U) // 1011
      dut.io.in.bits.mask.poke(0x2) // 0010

      outputCheck(dut)
      dut.io.out.bits.expect(1.U)
    }
  }

  it should "able to write 3 entries into different slots and report full" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())

      dut.io.in.bits.content.poke(0xA.U) // 1010
      dut.io.in.bits.mask.poke(0x1.U) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(0xB.U) // 1011
      dut.io.in.bits.mask.poke(0x2) // 0010
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(0xC.U) // 1100
      dut.io.in.bits.mask.poke(0x3) // 0011
      outputCheck(dut)
      dut.io.out.bits.expect(2.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(0xD.U) // 1101
      dut.io.in.bits.mask.poke(0x4) // 0100
      dut.io.full.expect(true.B)
      dut.io.out.valid.expect(false.B)
    }
  }

  it should "able to final available slot to write" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0xA.U) // 1010
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(0xB.U) // 1011
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(0xC.U) // 1100
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(2.U)

      dut.clock.step()

      dut.io.in.bits.content.poke(0xD.U) // 1101
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      dut.io.full.expect(true.B)
      dut.io.out.valid.expect(false.B)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildDeleteCmd())
      dut.io.in.bits.content.poke(0xB.U) // 1011
      dut.io.full.expect(true.B)
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0xE.U) // 1110
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)
    }
  }

  it should "able to write and read 1 entry" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x5.U) // 0101
      dut.io.in.bits.mask.poke(0x3.U) // 0011
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x5.U) // 0101
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "able to write and read 1 entry using wildcard" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x5.U) // 0101
      dut.io.in.bits.mask.poke(0x3.U) // 0011
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x4.U) // 0100
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "report not valid when entry not found in read" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x2.U) // 0010
      dut.io.out.valid.expect(false.B)
    }
  }

  it should "report not valid when entry not found in read using wildcard" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x1.U) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x2.U) // 0010
      dut.io.out.valid.expect(false.B)
    }
  }

  it should "able to delete 1 entry" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x1) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildDeleteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "able to delete 1 entry using wildcard" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x1) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildDeleteCmd())
      dut.io.in.bits.content.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "report not valid when entry not found in delete" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildDeleteCmd())
      dut.io.in.bits.content.poke(2.U)
      dut.io.out.valid.expect(false.B)
    }
  }

  it should "able to reset all entries" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x2.U) // 0010
      dut.io.in.bits.mask.poke(0x0.U) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x2.U) // 0010
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildResetCmd())
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect((p.entries - 1).U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.out.valid.expect(false.B)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x0) // 0000
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }

  it should "able to reset all entries using wildcard" in {
    test(new TCAM(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x1.U) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x2.U) // 0010
      dut.io.in.bits.mask.poke(0x1.U) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x0.U) // 0000
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(0.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x3.U) // 0011
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildResetCmd())
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect((p.entries - 1).U)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildReadCmd())
      dut.io.in.bits.content.poke(0x0.U) // 0000
      dut.io.out.valid.expect(false.B)

      dut.clock.step()

      dut.io.in.bits.cmds.poke(buildWriteCmd())
      dut.io.in.bits.content.poke(0x1.U) // 0001
      dut.io.in.bits.mask.poke(0x1) // 0001
      outputCheck(dut)
      dut.io.out.bits.expect(0.U)
    }
  }
}