Storage in this mod is determined by *Types* and *Bits*. For convenience, bits are usually displayed in the UI as bytes, which are 8 bits each. (i.e. 1 byte == 8 bits)

Every item uses 1 bit, regardless of stack size. As such, 8 items use 8 bits, or 1 byte. 64 items use 64 bits, or 8 bytes. In addition, there is a drive-specific allocation cost for each type. Every type of drive has only 64 types, and as such it's best to make lots of small drives instead of a few large drives. *Only make large drives if you have large amounts of a certain type of item to store.*

|               | Storage (bytes)| Alloc. Cost  | Power Usage |
|---------------|----------------|--------------|-------------|
| ![][1KiB]     | 1,024 (1KiB)   | 8 bytes      | 1 RF/t      |
| ![][4KiB]     | 4,096 (4KiB)   | 32 bytes     | 2 RF/t      |
| ![][16KiB]    | 16,384 (16KiB) | 128 bytes    | 4 RF/t      |
| ![][64KiB]    | 65,536 (64KiB) | 512 bytes    | 8 RF/t      |

[1KiB]: https://i.imgur.com/weGYzSi.png
[4KiB]: https://i.imgur.com/OcxKPvM.png
[16KiB]: https://i.imgur.com/fFUOa22.png
[64KiB]: https://i.imgur.com/SF5OUFj.png

The Void Drive is special, in that it appears to have infinite storage that cannot be accessed. Here's how it fits into the above table:

|               | Storage (bytes)| Alloc. Cost  | Power Usage |
|---------------|----------------|--------------|-------------|
| ![][Void]     | ∞              | 0 bytes      | 4 RF/t      |

**Remember that the Void Drive *deletes all items* that are put into it.** It is not an extremely dense storage solution.

[Void]: https://i.imgur.com/cmrWaky.png