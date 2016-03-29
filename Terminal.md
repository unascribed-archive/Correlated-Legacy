# ![](https://i.imgur.com/aicqVn4.png) Terminal

The Terminal allows you to directly access the storage of a system. When right-clicked, it will open a GUI showing every item in the system, sorted by quantity. The search bar in the top right will allow you to search for items containing a literal case-insensitive string.

Possible actions are:

* **Left-click**, remove 1 stack from the network
* **Right-click**, remove 1 item from the network
* **Shift left-click**, remove 1 stack from the network, directly into inventory
* **Shift right-click**, remove 1 item from the network, directly into inventory
* **Drop key**, remove 1 item from the network, onto the floor
* **Ctrl+drop key**, remove 1 stack from the network, onto the floor

The last one can be very useful for emptying out drives.

## Sorting Modes
![](https://i.imgur.com/i8FKwA2.png)

On either side of the search bar, there are buttons allowing you to set your sorting preference. This is unique per-terminal and per-user.

The left button allows you to set the sorting mode, while the right button sets direction. The possible sorting modes are:

* **Quantity** ( % ) - Items are sorted by the amount stored in the system, and items with the same quantity are sorted alphabetically by name.
* **Originating Mod (Minecraft First)** ( @ ) - Items are sorted alphabetically by which mod they are from, with items from vanilla showing up first. Items from the same mod are sorted alphabetically by name.
* **Originating Mod** ( @! ) - Identical to Originating Mod (Minecraft First), but without vanilla being treated specially.
* **Name** ( AZ ) - Items are sorted alphabetically by name.

## Crafting
The Terminal also has the ability to craft potentially large amounts of items using ingredients from the system. An overview:

![](https://unascribed.com/i/f509c7f3.png)

* The 3x3 at the top is the **crafting grid**, and accepts any recipes that can be used in a vanilla crafting table.
* The **> (right arrow)** button to the right of the crafting grid will put all the items in the grid back into the system when clicked.
* The button to the top-right of the result slot selects the **amount** to craft when shift-clicking, with three possible modes:
  * **One**, represented with one dot. Crafts one item at a time. The default.
  * **Stack**, represented with two dots. Crafts a full stack of the item at a time.
  * **Maximum**, represented with four dots. Crafts as many of the item as possible, until you run out of ingredients, inventory space, or hit the 6400 limit, whichever comes first. To help cut down on mistakes, the mode is reset to One after a crafting operation on this mode.
* The button to the bottom-right of the result slot selects the **target** for shift-clicking. The arrow points toward where it will go, and you can choose between it going to your player's inventory and it going directly into the storage system.