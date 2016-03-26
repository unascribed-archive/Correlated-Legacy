# ![](https://i.imgur.com/FD1n5Gy.gif) Drive

Drives are what actually store the items in a network. They come in a number of varieties, named after how much storage they have. (See [[Storage Math]] for more information.)

A drive has many lights on it, which indicate its state. Rather than try to explain their location, here's an annotated version of a 1KiB drive:

![Annotated 1KiB drive](https://i.imgur.com/CGHlWVK.png)

* **Tier**: The tier of this drive, such as red for 1KiB or orange for 4KiB.
* **Usage**: Fades from green to red, based on the current amount of bits and types used on the drive.
* **Partition Mode**: Teal for unpartitioned, white for whitelist mode.
* **Priority**: All gray for default, varying amounts of green or red lights for low or high priority (respectively)

The Tier and Usage lights are visible while the drive is in the drive bay. The Tier light is barely visible on the top of the drive, and the Usage light is visible on the front.