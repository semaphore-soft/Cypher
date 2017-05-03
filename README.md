Augmented Adventure - UWyo COSC 4950/5
=========================================
**Semaphore Soft/Augmented Adventure Team Members**

Rick Matza (scorple), Evan Turner (bagelhobbit), Joe Eleshuk (jeleshuk), and David Reynolds (dav97)

Our project is a mobile app for Android that allows users to play a dungeon crawler RPG video game on a table with friends using their phone cameras and screens as a viewport into the game world and printed paper markers to interact with the game in augmented reality.

Players construct a dungeon by placing paper AR markers on the table which correspond with randomly generated dungeon rooms, and opening doors connecting them to existing parts of the dungeon. Players navigate the dungeon by moving markers which serve as player waypoints between adjacent rooms of the dungeon. Players take turns moving and acting; a player's turn ends when the player opens a door, attacks an enemy, takes a defensive stance, performs a special ability, or uses an item. Enemies are randomly generated within dungeon rooms and will take turns acting - including attacking players, defending, performing special moves, and using items - after the last player's turn has been taken. Once all enemy turns have been taken, it will again be the first player's turn and this rotation will persist throughout the game. Enemies will drop any items they were generated with and have not yet used when they are killed. Players may pick up any items dropped by enemies off of the floor of the room they are in. The game will continue until all players have run out of health points and are defeated, or the players defeat the boss enemy of the dungeon.

For grading: The Source directory contains an Android Studio project in the Cypher directory. The actual java files can be found under [/Source/Cypher/app/src/main/java/com/semaphore_soft/apps/cypher/] (Source/Cypher/app/src/main/java/com/semaphore_soft/apps/cypher/)
