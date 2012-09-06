#!/bin/bash
rm iarTris.jar 2> /dev/null
rm `find . -d -name '*.class'`
javac IartrisMain.java main/iartris/*.java res/ResClass.java score/iartris/HiScore.java
jar cvfm iarTris.jar manifest.mf IartrisMain.* iartris.DAT ./main/iartris/ ./res/ ./score/iartris/
