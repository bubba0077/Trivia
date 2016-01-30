#!/bin/bash

rsync -avz -e "ssh" /home/bubba0077/trivia/audio /home/bubba0077/trivia/saves /home/bubba0077/trivia/charts "Walter Kolczynski"@bubbaland.net:/cygdrive/c/Apache/html/trivia
rsync -avz -e "ssh" /home/bubba0077/trivia/saves "Walter Kolczynski"@bubbaland.net:/cygdrive/c/Apache/html/trivia
rsync -avz -e "ssh" /home/bubba0077/trivia/charts "Walter Kolczynski"@bubbaland.net:/cygdrive/c/Apache/html/trivia
