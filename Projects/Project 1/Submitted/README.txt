I used java 11 to compile and run my program.
The topology files and the java file should all be in the same folder.

To compile the program, you could use one of the following commands:
javac *.java

or:

javac main.java Packet.java Node.java RoutingTable.java

To run the program use the following command, where topology1.txt can be changed to teh desired topolgy file that the program should run with. 
Note: The number 10 is the number of rounds the simulation should run for. User can change it to wahtever other number of round the simulation should run for.

java main topology1.txt 10



