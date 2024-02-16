#! /usr/bin/env bash

client_location="/mnt/hgfs/C/Apache/htdocs/trivia/triviaClient.jar"
server_location="/mnt/hgfs/C/Users/Walter/trivia_run/scripts/triviaServer.jar"
keystore="keystore.jks"
time_server="http://sha256timestamp.ws.symantec.com/sha256/timestamp"

jarsigner ${client_location} -keystore ${keystore} -tsa ${time_server} "Walter Kolczynski"
jarsigner ${server_location} -keystore ${keystore} -tsa ${time_server} "Walter Kolczynski"