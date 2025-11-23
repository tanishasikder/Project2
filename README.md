Changes and documentation of the code

The MemoryGame and Tile classes act as the model. The Tile class just makes the 
tile object (mostly the same as the starter code).
The MemoryGame class contains the major business logic of the game. Has methods
such as initializing tiles and checking for matches, just like the starter code.
There are many methods that were added, however. It has methods to update any 
business logic, such as the number of seconds that have passed, matches that were
made, etc. These are further used by the controller class to update the game.
There are also getter methods in the MemoryGame class for the controller class
to use.

The GameController class acts as the controller. It calls the view class to tell
the view class to output certain information. The timer is started from the memory
class, then the game begins. 
In the while loop, the view class is called to display the board and print a 
sentence. Within the if-else statements, the user input is received from the
view class. Depending on the input, different things happen, like printing a 
different statement or calling different methods being called. However, they all 
call the view class or model class to make the game function. Similar things happen for the
rest of the code. One exception is that the controller does call the model to get
a specific tile from the model.

The GameOutput class prints the gameboard, gives a class to print anything, and
get any user input
