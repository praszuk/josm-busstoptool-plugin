# BusStopTool plugin

## Description
Helper plugin which allows adding missing platform from stop_position (including memberships in relations) and vice versa.

## Usage
Example case: You need to create `highway=bus_stop;public_transport=platform` from `public_transport=stop_postion` on the road and 
add it to many relations in correct order. Instead of manually clicking just:
1. Open BusStopTool create platform/stop window from Menu->Selection.
2. Select source object
3. Select destination object
4. Click Create

Tip: You can also select 2 objects (order matters) before opening window.

It will copy common tags (like `name`, `bench`, `shelter`...excluding `public_transport=stop_position`) to destination object and add membership for 
all relations from source object.

If you are creating a platform from stop_position, then it will add membership with role _platform_ AFTER stop member.
If you are creating a stop_position from platform, then it will add membership with role _stop_ BEFORE stop member.
There is one exception. If the role is e.g. `exit_stop_only` then it will create `exit_platform_only` and it should work for all these edge cases.


## License
[GPLv3](LICENSE)