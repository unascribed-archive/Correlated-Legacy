# ![](https://i.imgur.com/FD1n5Gy.gif) Drive

Drives are what actually store the items in a network. They come in a number of varieties, named after how much storage they have. In addition, there is one kind of special drive, the Void Drive, which simply deletes all items that would be stored in it. (See [[Storage Math]] for more information.)

A drive has many lights on it, which indicate its state. Rather than try to explain their location, here's an annotated version of a 1KiB drive:

![Annotated 1KiB drive](https://i.imgur.com/CGHlWVK.png)

* **Tier**: The tier of this drive, such as red for 1KiB or orange for 4KiB.
* **Usage**: Fades from green to red, based on the current amount of bits and types used on the drive.
* **Partition Mode**: Teal for unpartitioned, white for whitelist mode.
* **Priority**: All gray for default, varying amounts of green or red lights for low or high priority (respectively)

The Tier and Usage lights are visible while the drive is in the drive bay. The Tier light is barely visible on the top of the drive, and the Usage light is visible on the front.

## Drive Editor
By right-clicking with a drive in hand, you will open the drive editor. This allows you to see the current items stored in the drive, as well as change the drive's priority and partitioning mode. The bottom left button controls priority, while the bottom right controls the partitioning mode.

All the empty slots in the middle of the UI control the partition. You cannot remove partitioned types unless the drive does not currently have any of that item stored. If the drive is in Not Partitioned mode, items there are none of will be automatically deallocated.