Changes and documentation of the code

The MemoryGame and Tile classes act as the model. The Tile class just makes the
tile object (mostly the same as the starter code).
The MemoryGame class contains the major business logic of the game. Has methods
such as initializing tiles and checking for matches, just like the starter code.
There are many methods that were added, however. It has methods to update any
business logic, such as the number of seconds that have passed, matches that were made, etc. These are further used by the controller class to update the game. There are also getter methods in the MemoryGame class for the controller class to use.

GameController acts as the controller. It first creates a constructor that gets the MemoryGame as a parameter to access its data. In the play method, when the time gets finished, the view is called to print an output. The timer is started and a while loop begins. The view is called twice to display the board and a sentence. User input is also gotten. If-else statements are written to handle any user queries. Depending on the input, the game could end, a different output could show, etc. However, all of them include calling on the view to produce an output. There is later some error handling in the try-catch statements. Later on, a specific tile is gotten from the model. More game mechanisms occur that either update the model or produce an output.

GameOutput is the view method where the game board in the terminal is printed. There are also methods to print any output and get user input.

Btw I removed most of the files in the source code cuz a lot of them were exclusive
to the repository the source code is from. The game runs fine without them
