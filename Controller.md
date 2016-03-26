# ![](https://i.imgur.com/tZwq04a.png) Controller
The Controller manages a network and all it's members, and is responsible for keeping track of all present drive bays and their contained drives, as well as delegating requests to add and remove items to the correct drives. As such, it is *required* that a network have a controller. Unlike AE2, this mod does not have ad-hoc networks.

## Power Usage
On it's own, a Controller uses 32 RF/t. Each member of a network also consumes RF, but does this by simply increasing the consumption rate for the controller itself.

## Item Distribution
A Controller decides which drive to send items to based on both it's distance from the controller and the drive's priority. At the drive bay level, the top-left drive is checked first, then the rest are checked left-to-right top-to-bottom, like shown:

![Drive bay iteration order](https://i.imgur.com/OVSBeve.png)

Drive bays closest to the controller are checked first, as a consequence of how the controller scans the network. Higher priority drives are checked before lower priority ones.

## Network Scanning
Controllers scan in an exapanding diamond around them, stopping when they hit a block that is not a member of the network. An illustrated example:

![Illustration of controller scan order](https://i.imgur.com/w1z4Yei.png)

1. The controller itself.
2. Every block touching a face of the controller.
3. Every block touching the face of a network member found in step 2.
4. Every block touching the face of a network member found in step 3.

Here's an illustration, showing the basic shape of the scanning:

![Illustration of controller scan shape](https://i.imgur.com/I9aDjKx.png)

That is to say, the controller scan order ends up assigning the *manhattan distance* from the controller to the network member as it's index for prioritization.

A controller will stop and error out if it ever tries to scan more than 100 blocks. This is to prevent extremely large networks from crashing and/or lagging the server.