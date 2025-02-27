# BusStopTool plugin

## Description
BusStopTool is a plugin that allows you to add missing public transport objects derived from existing elements.

It can create:
- `public_transport=stop_position` node from a `highway=bus_stop` or `highway=platform`.
- `public_transport=platform` node, way (open/closed) or multipolygon relation from a `stop_position` node.

The plugin inserts missing memberships in route relations for multiple relations with keeping the correct order for the created object.

## Usage
Example scenario: You need to create `highway=bus_stop;public_transport=platform` from `public_transport=stop_position` on the road and 
add it to multiple relations in the correct order. Instead of manually clicking:
1. Open BusStopTool create platform/stop dialog from Menu->Selection.
2. Select the source object
3. Select the destination object (object must exist, but can be empty)
4. Click Create

Tip: You can also select 2 objects before opening the dialog – they will be preselected (order matters).

The tool will copy common tags (such as `name`, `bench`, `shelter`, etc., excluding `public_transport=stop_position`) to the destination object and add membership for 
all relations from the source object.

To use it properly, ensure that the relation follows the PTv2 standard with members ordered as `stop, platform, stop, platform...`.


## License
[GPLv3](LICENSE)