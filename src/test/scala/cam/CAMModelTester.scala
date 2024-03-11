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

    def writeIPIntoMemory(dut: FIFOCAMModel, IP: Int, IPIdx: Int): Unit = {
        require(IP >= 0 && IPIdx >= 0, "IP and IPIdx should be greater or equal to 0.")
        dut.io.in.valid.poke(true.B)
        dut.io.in.bits.loadData.poke(log2Ceil(IP).U)
        dut.io.in.ready.expect(true.B)
        dut.io.in.bits.opCode.poke(0.U)

        dut.clock.step()
        dut.io.in.valid.poke(false.B)
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.writtenIndex.valid.expect(true.B)
        dut.io.writtenIndex.bits.expect(IPIdx.U)

        dut.clock.step()
        dut.io.writtenIndex.valid.expect(false.B)
        dut.io.in.ready.expect(true.B)
    }

    def lookupIPIntoMemory(dut: FIFOCAMModel, IP: Int, IPIdx: Int): Unit = {
      require(IP >= 0 && IPIdx >= 0, "IP and IPIdx should be greater or equal to 0.")
        dut.io.in.valid.poke(true.B)
        dut.io.in.ready.expect(true.B)

        dut.io.in.bits.loadData.poke(log2Ceil(IP).U)
        dut.io.in.bits.opCode.poke(1.U)

        dut.clock.step()
        dut.io.in.ready.expect(false.B)

        dut.clock.step()
        dut.io.lookupFound.bits.expect(true.B)
        dut.io.lookupFound.valid.expect(true.B)
        dut.io.lookupResult.valid.expect(true.B)
        dut.io.lookupResult.bits.expect(IPIdx.U)
    }

    def doCAMModelTest(cap: Int, IPBits: Int, IPsVec: Seq[Int]): Unit = {
      val p = CAMParams(cap, IPBits)
      val _IPsVec = IPsVec
      test(new FIFOCAMModel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        

        IPsVec.zip(_IPsVec.indices).foreach { case (ip, idx) =>
          writeIPIntoMemory(dut, ip, idx)
        }

        IPsVec.zip(_IPsVec.indices).foreach { case (ip, idx) =>
          lookupIPIntoMemory(dut, ip, idx)
        }


      }
    }

    behavior of "Add two IPs: 10, 100 into memory"
    it should "add an IP into memory" in {
      doCAMModelTest(128, 32, Seq(100, 10, 20, 15))
    }
}