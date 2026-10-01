#!/bin/sh
cp bin/Mobeegal-unsigned.apk bin/Mobeegal.apk
jarsigner -keystore mobiAntZ.keystore -storepass 8002utum -keypass 8002utum bin/Mobeegal.apk mobiAntZ

