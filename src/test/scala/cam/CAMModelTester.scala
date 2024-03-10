package cam


import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.util.log2Ceil

// case class CAMParams(capacity: Int, bitsPerIP: Int) {
// 	require(capacity > bitsPerIP)
// 	require(isPow2(capacity) && isPow2(bitsPerIP) && (capacity % bitsPerIP == 0))

// 	val numIPTag = capacity / bitsPerIP
// 	val numIPTagBits = log2Ceil(numIPTag)
// 	val numOffsetBits = log2Ceil(bitsPerIP)
// }

class CAMModelTester extends AnyFlatSpec with ChiselScalatestTester {
    def doCAMModelTest(cap: Int, IP: Int): Unit = {
      val p = CAMParams(cap, IP)
      test(new FIFOCAMModel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        
        // === Write ip = 100 into memory ===

        dut.io.in.valid.poke(true.B)
        dut.io.in.ready.expect(true.B)

        dut.io.in.bits.loadData.poke(log2Ceil(100).U)
        dut.io.writtenIndex.valid.expect(false.B)
        dut.io.in.bits.opCode.poke(0.U)

        dut.clock.step()
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.writtenIndex.valid.expect(true.B)
        dut.io.writtenIndex.bits.expect(0.U)

        // === Write ip = 10 into memory ===

        dut.io.in.valid.poke(true.B)
        dut.io.in.ready.expect(true.B)

        dut.io.in.bits.loadData.poke(log2Ceil(10).U)
        dut.io.in.bits.opCode.poke(0.U)

        dut.clock.step()
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.writtenIndex.valid.expect(true.B)
        dut.io.writtenIndex.bits.expect(1.U)

        // === Lookup ip = 10 in memory ===

        dut.io.in.valid.poke(true.B)
        dut.io.in.ready.expect(true.B)

        dut.io.in.bits.loadData.poke(log2Ceil(10).U)
        dut.io.in.bits.opCode.poke(1.U)

        dut.clock.step()
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.lookupFound.bits.expect(true.B)
        dut.io.lookupFound.valid.expect(true.B)
        dut.io.lookupResult.valid.expect(true.B)
        dut.io.lookupResult.bits.expect(1.U)

      }
    }

    behavior of "Add two IPs: 10, 100 into memory"
    it should "add an IP into memory" in {
      doCAMModelTest(128, 32)
    }
}