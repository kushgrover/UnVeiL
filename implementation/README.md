Requirements: 
    Java 15.0.2 or higher


Usage on Linux:
(Tested on Ubuntu 20.04.2 and higher)


For user defined environments, three input files are required ($DIR is the directory where these files are stored):
    $DIR/file_name.env -> specifying the environment
    $DIR/file_name.lb -> specifying the labelling
    $DIR/file_name.pr -> specifying the property in LTL (optional if given the automaton file in HOA forma)

As an example, we'll use the files in the directory "Examples/6rooms/" named 'example'

To run the planner on the given input, simply enter the command:

```console
java -jar planning.jar $DIR/file_name
```
For our example, this would correspond to
```console
java -jar planning.jar Examples/6rooms/example
```
For running it on a randomly generated environment:
```console
java -jar planning.jar --random-env
```
For storing output in a particular directory $OUTPUT (default is "temp/"):
```console
java -jar planning.jar --output-dir $OUTPUT
```
For our example, it can be:
```console
java -jar planning.jar Examples/6rooms/example --output-dir output/
```


There are several other command line switches available:
*. --debug : For verbose output
*. --no-advice : For no biasing during sampling
*. --first-expl-then-plan : First explore the whole environment, then plan in a completely known environment
*. --see-through-obstacles : Add tables as see through obstacles if used with --random-env
*. --plot : Output the plot showing the path traversed and the final path

Apart from these, you can modify several other parameters used in the algorithm as well:
*. --sensing-radius <value> : Sensing readius of the robot
*. --cell-size <value> : Size of each cell used in discretization 
*. --batch-size <value> : Sizeof each batch
*. --bias-prob <value> : Extent with how much biasing should be done
*. --property-file <file_name>.hoa : Auotomaton representing the property in HOA format. If specified, .pr file will be ignored


For producing results in the directory Final_results, run:
```console
./experiments.sh 100 3
```
First number specifies how many random environments to be used and the second number specifies how much repetition of each environment should be run. The results will be written in a new directory "results".

