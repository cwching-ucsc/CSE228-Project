# ChiselRouter 🌐

> A network router implementation with TCAM in Chisel.

(TCAM: Ternary Content-Addressable Memory)

## Goal
A working IPv4 router with ARP (Address Resolution Protocol) support. It's
capable of routing data frames / network packets in L2 (Data Link) / L3 (Network)
layers.

(IPv6 support is on our wishlist)

## Documentation
Checkout our ScalaDoc at <https://chisel-router.netlify.app/>

## Build & Run

This project uses `sbt` as the build tool and uses Java version 17 as its SDK. 


To install `sbt`, please refer to the instructions [here](https://www.scala-sbt.org/1.x/docs/Setup.html).

```bash
# Clone repo
git clone https://github.com/wtongze/CSE228-Project.git
cd CSE228-Project

# Compile all source code
sbt compile

# Run all test cases
sbt test

# Test IPv4SubnetUtil
sbt "testOnly *IPv4SubnetUtil*"
```

## Current Status

### Done ✅
- IPv4 Addr Model in Scala
  - `NetworkAddr.scala`
  - Able to represent an IPv4 address
    - In binary
    - In human-readable format (a.b.c.d)
    - Comparator
- IPv4 Subnet Tool
  - `IPv4SubnetUtil.scala`, `SubnetUtil.scala` 
  - Calculate subnet address space
  - Check if an address is in a subnet
  - Check if an address is a broadcast address

### Await Integration Test 🏗
- CAM (Content-Addressable Memory) in Chisel
  - `CAMModel.scala` 
  - Store IP
  - Retrieve value based on IP
  - Clear memory

### Work In Progress 🚧
- IPv6 Addr Model in Scala
  - Similar to IPv4 Addr Model
- TCAM (Ternary Content-Addressable Memory) in Chisel
  - Similar to CAM, but with IP wildcard support

## Development Workflow
`main` branch is only reserved for production-ready code that can pass all the existing test
cases. Development on a new feature will be happened in a different branch. Once the development
has finished, such a branch will be merged back to `main` branch. This approach allows simultaneous
collaboration on the project with minimizing merge conflicts.

## Authors
Tongze Wang, Cheng-Wei Ching
