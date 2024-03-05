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
        
        
        dut.io.in.valid.poke(true.B)
        dut.io.in.ready.expect(true.B)

        dut.io.in.bits.loadData.poke(log2Ceil(100).U)
        dut.io.writtenIndex.valid.expect(false.B)
        dut.io.in.bits.opCode.poke(0.U)

        dut.clock.step()
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.writtenIndex.valid.expect(true.B)

        // val expectedValues = Seq(false.B, false.B, false.B, false.B)
        // expectedValues.zipWithIndex.foreach { case (value, index) =>
        //   dut.io.resultVec(index).expect(value)
        // }
      }
    }

    behavior of "Add IP into memory"
    it should "add an IP into memory" in {
      // val p = CAMParams(128, 32)
      // val m = CacheModel(p)()
      // doCAMModelTest()
      doCAMModelTest(128, 32)
    }
}