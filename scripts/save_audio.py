#!/home/bubba0077/anaconda3/bin/python

import sys
import os
import subprocess
from datetime import datetime, timedelta, timezone
from contextlib import suppress
from functools import partial

destination_root = "data/audio"
stream_source = "http://corn.kvsc.org:8000/broadband"
clip_length = 330 # in seconds

event_start = datetime.fromisoformat("2021-02-12T18:00:00-05:00")
break1_start = datetime.fromisoformat("2021-02-13T00:00:00-05:00")
break1_end = datetime.fromisoformat("2021-02-13T08:00:00-05:00")
break2_start = datetime.fromisoformat("2021-02-14T00:00:00-05:00")
break2_end = datetime.fromisoformat("2021-02-14T08:00:00-05:00")

# Make sure print statements are flushed immediately, otherwise
#   print statements may be out-of-order with subprocess output
print = partial(print, flush=True)

break1_length = break1_end - break1_start
break2_length = break2_end - break2_start

def delta_to_hours_mins(duration:timedelta) -> (int,int):
    days, seconds = duration.days, duration.seconds    
    hours = days * 24 + seconds // 3600
    minutes = seconds // 60 - 60 * ( seconds // 3600 )
    return(hours, minutes)

def get_time_elapsed(now:datetime) -> timedelta:
    diff = now - event_start
#     print(f'Diff = {diff}')
    if (now > break1_start):
#         print("After break 1")
        diff = diff - break1_length
    if (now > break2_start):
#         print("After break 1")
        diff = diff - break2_length
    return diff

def capture_stream(stream_source:str, destination_root:str, hours:int, minutes:int, length:int) -> None:
    destination = f'{destination_root}/Hour_{hours:02d}/{hours:02d}h_{minutes:02d}m.mp3'
    with suppress(FileExistsError):
        os.mkdir(f'{destination_root}/Hour_{hours:02d}')
#     subprocess.call(f"ffmpeg -v quiet -i {stream_source} -acodec copy -t {length} {destination}", shell=True)
    subprocess.call(f"ffmpeg -i {stream_source} -acodec copy -t {length} {destination}", shell=True)
    

if __name__ == '__main__':
    now = datetime.now(tz=timezone(timedelta(hours=-5)))
    
    if ( now < event_start - timedelta(hours=1) ):
        print("Skipping save before event start")
        quit(0)
    
    if( (now > break1_start and now < break1_end) or (now > break2_start and now < break2_end) ):
        print("Skipping save during breaks")
        quit(0)

    diff = get_time_elapsed(now)
    
    hours, minutes = delta_to_hours_mins(diff)
    
#     print(f'Diff = {diff}')
    print(f'Capturing stream for: {hours:02d}h {minutes:02d}m')
    capture_stream(stream_source, destination_root, hours, minutes, clip_length)
    print('Stream capture complete. Exiting.')
