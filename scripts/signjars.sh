#! /usr/bin/env bash
jarsigner /mnt/c/Apache/htdocs/trivia/triviaClient.jar -keystore keystore.jks -tsa http://sha256timestamp.ws.symantec.com/sha256/timestamp "Walter Kolczynski"
jarsigner /mnt/c/Users/Walter\ Kolczynski/trivia_run/triviaServer.jar -keystore keystore.jks -tsa http://sha256timestamp.ws.symantec.com/sha256/timestamp "Walter Kolczynski"