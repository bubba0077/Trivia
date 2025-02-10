#!/usr/bin/env python3

import os
import subprocess
from datetime import datetime, timedelta
from contextlib import suppress
from functools import partial
from shutil import which

destination_root = f"{os.path.dirname(__file__)}/../data/audio"
stream_source = "https://corn.kvsc.org:443/broadband"
clip_length = 330  # in seconds

# print(destination_root)

event_start = datetime.fromisoformat("2025-02-14T17:00:00-06:00")
# break1_start = datetime.fromisoformat("2022-02-20T19:00:00-06:00")
# break1_end = datetime.fromisoformat("2022-02-20T19:00:00-06:00")
# break2_start = datetime.fromisoformat("2022-02-20T19:00:00-06:00")
# break2_end = datetime.fromisoformat("2022-02-20T19:00:00-06:00")

# Make sure print statements are flushed immediately, otherwise
#   print statements may be out-of-order with subprocess output
print = partial(print, flush=True)

# break1_length = break1_end - break1_start
# break2_length = break2_end - break2_start


def determine_capture_program():
    for exe in "ffmpeg", "avconv":
        if which(exe) is not None:
            return exe
    print("FATAL ERROR: Could not find any capture program")
    exit(1)


def delta_to_hours_mins(duration: timedelta) -> (int, int):
    days, seconds = duration.days, duration.seconds
    hours = days * 24 + seconds // 3600
    minutes = seconds // 60 - 60 * (seconds // 3600)
    return(hours, minutes)


def get_time_elapsed(now: datetime) -> timedelta:
    diff = now - event_start + timedelta(hours=1)
    # # print(f'Diff = {diff}')
    # if (now > break1_start):
    #     # print("After break 1")
    #     diff = diff - break1_length + timedelta(hours=1)
    # if (now > break2_start):
    #     # print("After break 1")
    #     diff = diff - break2_length
    return diff


def capture_stream(stream_source: str, destination_root: str, hours: int, minutes: int, length: int) -> None:
    destination = f'{destination_root}/Hour_{hours:02d}/{hours:02d}h_{minutes:02d}m.mp3'
    with suppress(FileExistsError):
        os.mkdir(f'{destination_root}/Hour_{hours:02d}')
    subprocess.call(f"{determine_capture_program()} -i {stream_source} -acodec copy -t {length} \"{destination}\"", shell=True)


if __name__ == '__main__':
    now = datetime.now().astimezone()
    if (now < event_start - timedelta(hours=1)):
        print(f"{now}: Skipping save before event start")
        quit(0)

    # if((now > break1_start and now < break1_end) or (now > break2_start and now < break2_end)):
    #     print("Skipping save during breaks")
    #     quit(0)

    diff = get_time_elapsed(now)

    hours, minutes = delta_to_hours_mins(diff)

    # print(f'Diff = {diff}')
    print(f'Capturing stream for: {hours:02d}h {minutes:02d}m')
    capture_stream(stream_source, destination_root, hours, minutes, clip_length)
    print('Stream capture complete. Exiting.')
