# ChiselRouter 🌐

> A network router (and switch) implementation with TCAM and CAM in Chisel.

(TCAM: Ternary Content-Addressable Memory)

## Goal
A working IPv4 / IPv6 router (and switch) with ARP (Address Resolution Protocol) simulation. It's
capable of routing data frames / network packets in L2 (Data Link) / L3 (Network)
layers.

This simulation is in `IntegrationTester.scala`. 

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

# Test Router / Switch Functionality
sbt "testOnly *IntegrationTester*"

# Test IPv4 / IPv6 Address Model
sbt "testOnly *AddrTester*"

# Test IPv4 / IPv6 SubnetUtil
sbt "testOnly *SubnetUtil*"

# Test CAM (FSM)
sbt "testOnly *CAMModelTester*"

# Test CAM (non-FSM)
sbt "testOnly *CAMTester*"

# Test TCAM (non-FSM)
sbt "testOnly *TCAMTester*"
```

## Current Status

### Done ✅
- CAM (Content-Addressable Memory) in Chisel
  - `CAMModel.scala` (FSM), `CAM.scala` (non-FSM)
  - Store entry (Write)
  - Retrieve index based on entry content (Read)
  - Remove entry (Delete)
  - Clear memory (Reset)
- TCAM (Ternary Content-Addressable Memory) in Chisel
  - `TCAM.scala` (non-FSM)
  - Similar to CAM, but with content wildcard support
    - `1` in mask means this bit is a wildcard (X)
    - `0` otherwise
- IPv4 / IPv6 Addr Model in Scala
  - `IPv4Addr.scala`, `IPv6Addr.scala`
  - Able to represent an IPv4 / IPv6 address
    - In `Seq[Short]`
    - Initialize from human-readable format
      - "1.2.4.8"
      - "2041:0000:140F:0000:0000:0000:AAAA:1CCC"
    - Initialize from `BigInt`
    - Comparator
- IPv4 / IPv6 Subnet Tool in Scala
  - `IPv4SubnetUtil.scala`, `IPv6SubnetUtil.scala` 
  - Calculate subnet address space
  - Check if an address is in a subnet
  - Check if an address is a broadcast address

## Development Workflow
`main` branch is only reserved for production-ready code that can pass all the existing test
cases. Development on a new feature will be happened in a different branch. Once the development
has finished, such a branch will be merged back to `main` branch. This approach allows simultaneous
collaboration on the project with minimizing merge conflicts.

We have also enabled CI/CD for automated testing using Github Actions and deploying API documentation
through Netlify.

## Authors
Tongze Wang, Cheng-Wei Ching
