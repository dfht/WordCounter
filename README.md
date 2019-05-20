# WordCounter

A simple command line application to compute the occurrences of different words in a file and output the results to System.out. 
Each line shows a word and the number of occurrences of that word in the file. 
The results are printed out in descending order of frequency.

Assuming you have Maven installed, then it can be built (from command line in the project folder) via:

`mvn package`

This will also run the tests of course.

This will build a combined `wordCounter.jar` file in the project lib folder. 
I currently commit this as the latest 'release'.
This is an executable java file that can simply be run (from a folder containing the jar of course) via:

`java -jar wordCounter.jar`

Optionally, arguments can be passed in for the file and the encoding. If the encoding isn't specified then it is 'guessed' from the BOM (via icu4j).
If the file isn't specified, then it will be asked for on the command line.

Full usage is:

`java -jar wordCounter.jar [-help] [-file=filePath] [-encoding=encoding]`


Again, assuming you have Maven installed, you can just run the tests via:

`mvn test`

(from command line in the project folder).





