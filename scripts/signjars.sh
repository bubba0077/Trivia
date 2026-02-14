#! /usr/bin/env bash

client_location="triviaClient.jar"
server_location="triviaServer.jar"
keystore="keystore.jks"
time_server="https://freetsa.org/tsr"

jarsigner ${client_location} -keystore ${keystore} -tsa ${time_server} "Walter Kolczynski"
jarsigner ${server_location} -keystore ${keystore} -tsa ${time_server} "Walter Kolczynski"
