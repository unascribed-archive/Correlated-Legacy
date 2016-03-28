# ![](https://i.imgur.com/oH28xFM.png) Interface

The Interface exposes the contents of the network to automation, or allows automation to add items to the network.

Using any BuildCraft-compatible wrench, you can reconfigure each face of the interface to choose the mode. There are four modes:

* **Passive** (white) - The contents of the interface are exposed, but the interface itself does not pull or push items. Items can be piped in or out of this side.
* **Active - Push** (orange) - The interface will attempt to push items into the adjacent inventory from it's orange slots. Items cannot be piped into this side.
* **Active - Pull** (blue) - The interface will attempt to pull items from adjacent inventories into it's blue slots, which will then be emptied into the network. Items cannot be piped out of this side.
* **Disabled** (no hole) - The interface will not accept pipe connections on this side and will not perform any action.

Currently, Push mode *does not* push into pipes. It will only push into inventories.

## GUI
![The interface's GUI](https://i.imgur.com/53sVtMy.png)

By right-clicking the interface while not holding a wrench, you will be presented with the above GUI.

* The **blue slots** are the input buffer. Every 16 ticks, the interface will attempt to insert these items into the storage network.
* The **gray slots** are the output filter. These determine what items will be put into what slots in the output buffer.
* The **orange slots** are the output buffer. Every 16 ticks, the interface will attempt to put as many items as possible into each of these slots, taken from the network (including other interfaces, if necessary), as configured in the filter.

The orange slots are also visible from within the storage network, so even if you have an interface with a bunch of items in it's buffer, you can still grab them from a terminal.