#!/bin/bash

# rsync -avz -e "ssh" /home/bubba0077/trivia/data "Walter Kolczynski"@bubbaland.net:/cygdrive/c/Apache/html/trivia
rsync -avz -e "ssh" /home/bubba0077/trivia/data/saves "Walter Kolczynski"@bubbaland.net:/cygdrive/c/Apache/html/trivia/data/
rsync -avz -e "ssh" /home/bubba0077/trivia/data/charts "Walter Kolczynski"@bubbaland.net:/cygdrive/c/Apache/html/trivia/data/
