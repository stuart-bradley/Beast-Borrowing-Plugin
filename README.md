# BEAST2 Language Generation

This repository is home to the BEAST2 language generation plugin, which, similar to the `seqgen` (for DNA) module in BEAST2, generates synthetic language data.

**Please note: This plugin is not yet feature complete, and it is in no way guaranteed to work as expected or intended.** 

## Quick Start 

The plugin is not currently in a very exportable format, as it is a work in progress. Inside the repository, you will find all the files to run the plugin in its current state (quirks included).

Simply import the project into Eclipse, along with a copy of the [BEAST2 project](https://github.com/CompEvol/beast2), from here the plugin can be run two different ways.

### Command Line

Like the original `seqgen`, this plugin uses the same format for command line runs:

```
java LanguageSequenceGen <beast file> <nr of instantiations> [<output file>]
```
* The `<beast file>` is an `xml` file that specifics the initial input parameters. An example is provided below. 

* To determine the number of meaning classes, `<nr of instantiations>` is provided, the position of first cognate in each meaning class is provided as an additional sequence at the end. 

* If an `<output file>` is not provided, the output will be written to `std.out`. 

### BeastBorrowingPluginTest

Like most BEAST2 plugins, this plugin has its own testing suite defined in `BeastBorrowingPluginTest`. In this class, the `SeqGenTest()` runs the plugin using arguments defined within the function: 

```
private static void SeqGenTest() {
		String[] args = {"C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/examples/testSeqLangGen.xml","2","C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/examples/output.xml"};
		LanguageSequenceGen.main(args);
}
```

There are a number of other tests in the class that produce `csv` files, which are in turn used to validate various portions of the plugin in R. 

## The BEAST File

The BEAST file outlines how to produce the synthetic data. An example is provided in `/examples/testSeqLangGen.xml`; it is reproduced below:

```
<beast version='2.0'
       namespace='beast.evolution.alignment:beast.evolution.substitutionmodel'>

    <tree id='tree' spec='beast.util.TreeParser' IsLabelledNewick='true' newick='((((human:0.02096625515232275,(chimp:0.014857143159686462,bonobo:0.014857143159686462):0.0061091119926362895):0.012862878672687175,gorilla:0.033829133825009926):0.029471223948245952,orangutan:0.06330035777325588):0.0031773962188650223,siamang:0.0664777539921209)' />


    <run spec="beast.app.seqgen.LanguageSequenceGen" tree='@tree'>
		<root spec='Sequence' value="01010101010100100010101010000100" taxon="root"/>
		
		<subModel spec='ExplicitBinaryStochasticDollo' birth="0.5" death = "0.5" borrowrate ="0.0" borrowzrate="0.0" />	
	</run>
</beast> 
```

* The `tree` takes a newick formatted tree with both branch distances and taxon node names. 
* The `run` initiates the plugin using the `tree` defined above. It also has a number of interior parameters:
  * `root` is the sequence to be placed at the root of the tree. It should consist of present (1) or absent traits (0). The plugin does not handle missing or unknown traits. The `taxon` does not need to be *root*.  