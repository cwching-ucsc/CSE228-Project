#!/bin/bash
wget -O sbt.zip https://github.com/sbt/sbt/releases/download/v1.9.8/sbt-1.9.8.zip
unzip sbt.zip
./sbt/bin/sbt doc
