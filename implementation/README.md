<h1>Using precompiled JAR</h1>

Requirements: 
    Java 15.0.2 or higher


Usage on Linux:
(Tested on Ubuntu 20.04.2 and higher)


For user defined environments, three input files are required ($DIR is the directory where these files are stored):\
<ol>
<li>$DIR/file_name.env -> specifying the environment</li>
<li>$DIR/file_name.lb -> specifying the labelling</li>
<li>$DIR/file_name.pr -> specifying the property in LTL (optional if given the automaton file in HOA format)</li>
</ol>

As an example, we'll use the files in the directory "Examples/6rooms/" named 'example'

To run the planner on the given input, simply enter the command:

```console
java -jar planning.jar $DIR/file_name
```
For our example, this would correspond to
```console
java -jar planning.jar Examples/random/random
```
For generating a random 6-room office like environment, use python script ran_env_gen.py (use flag --see-through for 
an environment with see through obstacles):
```console
python3 ran_env_gen.py $OUTPUTDIR
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
<ul>
<li> --debug : For verbose output</li>
<li> --no-advice : For no biasing during sampling</li>
<li> --first-expl-then-plan : First explore the whole environment, then plan in a completely known environment</li>
<li> --see-through-obstacles : Add tables as see through obstacles if used with --random-env</li>
<li> --plot : Output the plot showing the path traversed and the final path</li>
</ul>

Apart from these, you can modify several other parameters used in the algorithm as well:
<ul>
<li> --sensing-radius [value] : Sensing radius of the robot. </li>
<li> --cell-size [value] : Size of each cell used in discretization</li> 
<li> --batch-size [value] : Size of each batch </li>
<li> --bias-prob [value] : Extent with how much biasing should be done </li>
<li> --property-file [file_name].hoa : Automaton representing the property in HOA format. If specified, .pr file will 
be ignored </li>
</ul>


For producing results in the directory Final_results, run:
```console
./experiments.sh 100 3
```
First number specifies how many random environments to be used and the second number specifies how much repetition of 
each environment should be run. The results will be written in a new directory "results".


<h1>Build from sources</h1>


<h1>Setting up Intellij</h1>

<h2>Configuring Project</h2>
<ol>
<li>After opening the project in intellij go to 'Project Structure'.</li>
<li>Select 'Project SDK' (Java 11 or above should work).</li>
<li>Check that 'Project compiler output' is the bin folder inside implementation.</li>
<li>Select 'Library' --> '+' --> 'Java'.</li>
<li>Go inside 'lib' folder and select all jar files.</li>
<li>Select 'Modules' --> '+' --> 'New Module' --> 'Next'.</li>
<li>Give Module name 'planning' and Content root as 'implementation' directory.</li>
</ol>

<h2>Add configuration</h2>
<ol>
<li>Select Application.</li>
<li>Select JDK 11 or above.</li>
<li>Select main class as 'mainCL'.</li>
<li>Specify arguments as above, e.g.

```console
Examples/random/random --plot
```
</li>
<li>Inside 'Environment variables' field, write:

```console
LD_LIBRARY_PATH=:lib
```
</li>
</ol>

It should work now otherwise pray to god.

<h2>Exporting JAR</h2>
<ol>
<li>Open 'Project Structure' -> 'Artifacts' --> '+' --> 'JAR' --> 'From modules with dependencies...'</li>
<li>Select 'mainCL' as the mail class and click OK.</li>
<li>Change the 'Output directory' if required.</li>
<li>Go to 'Build' --> 'Build Artifacts'</li>
</ol>

