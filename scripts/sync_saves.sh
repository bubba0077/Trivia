#!/bin/bash

echo "Sync started at $(date)"
# rsync -avz -e "ssh" /home/bubba0077/trivia/data "Walter"@bubbaland.net:/cygdrive/c/Apache/htdocs/trivia
rsync -avz -e "ssh" /home/bubba0077/trivia/data/saves "Walter"@bubbaland.net:/cygdrive/c/Apache/htdocs/trivia/data/
rsync -avz -e "ssh" /home/bubba0077/trivia/data/charts "Walter"@bubbaland.net:/cygdrive/c/Apache/htdocs/trivia/data/
