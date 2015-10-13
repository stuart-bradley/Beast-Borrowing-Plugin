# Lazy Languages: A Bit of Borrowing

The purpose of this repository is to draw together the various scripts, and documents required for the completion of the thesis in 2016. 

## Cognate Substitution Models

There are a number of substitution models implemented in this repository. All models are implemented as part of the `CognateSet` class inside of `\Utility Classes\`.

### Generalised Time-Reversible

The CTMC can be extended to include unequal rates of mutation (Tavaré, 1986). Instead of assuming all changes are equal, traits can change to particular traits with particular probabilities. 

In linguistics this can be used to model such things as the death of a cognate happening more often than a birth. How this differs from the  Stochastic-Dollo model (below) is that it has a finite number of mutable sites.

#### Psuedocode 

The probability matrix is defined within the `RateMatrix` class. This takes a list of possible characters as a list, and a 2D list of rates (interior lists are rows of the matrix). The class also includes methods for making the probability matrix, and for matching characters to a particular row. 

There are two methods for doing this, the first method means that exact mutations times are kept:

```
for site in sequence:
	t = rand_exp(-Q(site))
	while t < T:
		mutate site based on normalised rates for the site row
		t+= rand_exp(-Q(site))
```

The second method just produces the resulting site change after time T, and specific mutation information is lost:

```
P = expm(Q*T)
for site in sequence:
	site = new site based on probabilities in P(site)
```

Both versions produce the exact same distribution, this is proven by using an equal rate matrix, and a simulation of a 1000 runs. Blue is the first method, and red is the second. 

![GTR Simulation](https://raw.github.com/lutrasdebtra/lazy_languages/master/Images/GTR_sim.png)

Both simulations, as well as the 0's (circles), and 1's (squares) are very close to equilibrium (P=0.5).

### Covarion Process

The Covarion model of evolution simulates situations where certain traits are unable to change due to structural or functional constraints. (Fitch & Markowitz, 1970) state that any mutable (variant) region, can become immutable (invariant) for a given period of time. This has parallels in linguistic evolution, where certain cognates may become immutable due to heavy use, or other constraints.

The model itself is governed by three parameters:
- Mutation rate(s): `r`.
- Variant to invariant rate: `inv`.
- Invariant to variant rate: `var`. 

This means that when an evolutionary event occurs, a trait will either move into or out of existence, or change it's variant state. 

### Psuedocode

The code is the same for the GTR model. The difference occurs in the construction of the rate matrix. In the Covarion model, both invariant and variant versions of each state need to be represented. 

This can easily be done by appending a variant or invariant character to their respective states. For example, a binary rate matrix would look like:

|       | 0 | 1 |
|-------|---|---|
| **0** | - | r |
| **1** | r |   |

This can be extended to a covarion process where characters are represented as either:
- Variant: `01`,`11`
- Invariant: `00`,`10`

So that the resulting matrix now becomes:

|        | 01  | 11  | 00  | 10  |
|--------|-----|-----|-----|-----|
| **01** | -   | r   | inv |     |
| **11** | r   | -   | 0   | inv |
| **00** | var | 0   | -   | 0   |
| **10** | 0   | var | 0   | -   |

### Stochastic Dollo Process

The Stochastic-Dollo process is based upon the Dollo parsimony, where transitions to some states has a minimal probability of occurrence. (Nicholls & Gray, 2008) produced a binary trait model, where transitions proceed: "from absence to presence, to absence only". This is used to model the gain of rare morphological character, which is so rare as to often only occur once in the entire history of life.

This model is used in language evolution to better model the fact that language birthing events are very rare, and are often overshadowed by other mechanisms, such as borrowing. 

Mathematically the model has two parameters:
- Death rate.
- Birth rate.

#### Psuedocode

```
t = rand_exp(birth_rate + (death_rate * language_traits))
	while t < T:
		Randomly pick between a birth event (birth/(birth + death * language_traits)) or a death event (death/(birth + death * language_traits))
		if death:
			pick random trait -> 0.
			Decrement number of traits added to language.
		if birth:
			Add trait to list
			Increment number of traits added to language.
		t += rand_exp(birth_rate + (death_rate * language_traits))
```

## Simulating Trees

Simulating trees of languages (or anything else), is a simple process of iteratively building up mutation events from one language to another.

Starting with some root language, it can split into two distinct languages at some random exponential time. Languages are modeled splitting into two, because this makes simulating the process simpler. Splitting into more than two child languages can be modeled by arbitrarily small times.

Under the GTR or Covarion models of mutation, this is a relatively simple algorithm. However, under the Stochastic-Dollo model, the current total number of cognates must be constantly updated as new birth events occur. For example, given one language:

`{0,1,0}`

If a birth occurs along one branch, it must also be represented (as a `0`) on the other branch:

```
{0,1,0} -> {0,1,0,1} # Birth
{0,1,0} -> {0,1,0,0} # No mutation, but sequence has increased in size.
```

### Psuedocode

The Stocastic-Dollo model differs only in that there is a check to see if a language has as many cognates as the maximum in the set, and if it does not it has `0`'s appended to it. 

```
Add root language to Queue
while tree is below desired height (defined by the number of languages required):
	Node = Queue.pop()
	do 2 times:
		t = rand_exp(rate)
		new_language = Node.mutate
		Add new_language to tree, with edge: (Node, new_language)
		Append new_language to Queue.
```

## Models of Borrowing

Current models of borrowing tend to reasonably simplistic. This is because cognate lists tend to see less inter-language borrowing when compared with languages as a whole. Since borrowing is a relatively rare phenomenon, simple algorithms are completely effective at modeling the process. Global borrowing is the simplest borrowing algorithm used, in essence it is broken down as follows:
1. Borrowing occurs at some rate *b*.
2. When a borrowing event occurs, the trait from one language is passed randomly to another, laterally across the tree.

This process can be extended to approximate language closeness. Local borrowing introduces a limit on how close a common ancestor has to be, *z*, for borrowing to occur. This process occurs as such:
1. Borrowing occurs at some rate *b*.
2. The set of languages in which the giver has a common ancestor with any item in the set at most *z* time units in the past. 
3. When a borrowing event occurs, the trait from one language is passed randomly to a language in the set from (2), laterally across the tree.

Stochastic-Dollo models complicate this slightly. This is because borrowing events are synonymous with birth events, and if the same trait is *birthed* twice, then it violates the Dollo parsimony (Nicholls & Gray, 2008). The borrowing rate is relative to the death rate, *bd*, to account for these additional birth events.  

## References

- Alekseyenko, A. V., Lee, C. J., & Suchard, M. A. (2008). Wagner and Dollo: A Stochastic Duet by Composing Two Parsimonious Solos. Systematic Biology, 57(5), 772-784. doi: 10.1080/10635150802434394
- Atkinson, Q., Nicholls, G., Welch, D., & Gray, R. (2005). From words to dates: water into wine, mathemagic or phylogenetic inference? Transactions of the Philological Society, 103(2), 193-219. doi: 10.1111/j.1467-968X.2005.00151.x
- Bouckaert, R., Lemey, P., Dunn, M., Greenhill, S. J., Alekseyenko, A. V., Drummond, A. J., . . . Atkinson, Q. D. (2012). Mapping the Origins and Expansion of the Indo-European Language Family. Science, 337(6097), 957-960. 
- Gray, R. D. A. Q. D. (2003). Language-tree divergence times support the Anatolian theory of Indo-European origin. Nature, 426(6965), 435-439. doi: 10.1038/nature02029
- Nicholls, G. K., & Gray, R. D. (2008). Dated ancestral trees from binary trait data and their application to the diversification of languages. Journal of the Royal Statistical Society: Series B (Statistical Methodology), 70(3), 545-566. doi: 10.1111/j.1467-9868.2007.00648.x
- Penny, D., McComish, B. J., Charleston, M. A., & Hendy, M. D. (2001). Mathematical Elegance with Biochemical Realism: The Covarion Model of Molecular Evolution. Journal of Molecular Evolution, 53(6), 711-723. doi: http://dx.doi.org/10.1007/s002390010258
- Fitch WM, Markowitz E. (1970). An improved method for determining codon variability in a gene and its applications to the rate of fixation of mutations in evolution. Biochem Genet, 4, 579–593.
- Jukes TH and Cantor CR (1969). Evolution of Protein Molecules. New York: Academic Press. pp. 21–132.
- Tavaré S (1986). Some Probabilistic and Statistical Problems in the Analysis of DNA Sequences. Lectures on Mathematics in the Life Sciences (American Mathematical Society), 17, 57–86.
