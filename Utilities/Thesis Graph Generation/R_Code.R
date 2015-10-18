# Data input

gtr <- read.table("~/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/gtr.csv", quote="\"", comment.char="")
gtr_2 <- read.table("~/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/gtr_2.csv", quote="\"", comment.char="")

gtr <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr.csv", quote="\"")
gtr_2 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr_2.csv", quote="\"")
sd <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sd.csv", quote="\"")
gtrtree <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtrtree.csv", quote="\"")
sdtree <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sdtree.csv", quote="\"")

# GTR Lineage Validation
plot(density(rbinom(100000,20,0.5), adjust=10), col="green",lwd=2, 
     main="Simulation of 100,000 language evolutions under the GTR model", xlab="Number of cogantes", xlim=range(0:20))
lines(density(gtr$V1, adjust=10), col="blue", lwd=2)
lines(density(gtr_2$V1, adjust=10), col="red", lwd=2)
legend('topright',c("Binomial Distribution","Algorithm 1", "Algorithm 2"), lty=c(1,1,1), lwd=c(2,2,2),col=c("green","blue","red"))

# SD Lineage Validation
plot(density(rpois(100000,(0.5/0.5)), adjust=10), col="green",lwd=2, 
     main="Simulation of 100,000 language evolutions under the Stochastic-Dollo Algorithm", xlab="Number of birthed traits", xlim=range(0:20))
lines(density(sd$V1, adjust=10), col="blue", lwd=2)
legend('topright',c("Poisson Distribution","SD Algorithm"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))

# GTR/SD Simple Tree Validation
par(mfrow=c(2, 1))
plot(density(rbinom(800000,20,0.5), adjust=10), col="green",lwd=2, 
     main="Simulation of 100,000 whole tree evolutions under the GTR model", xlab="Number of cogantes", xlim=range(0:20))
lines(density(gtrtree$V1, adjust=10), col="blue", lwd=2)
legend('topright',c("Binomial Distribution","GTR Algorithm"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))
plot(density(rpois(800000,(0.5/0.5)), adjust=10), col="green",lwd=2, 
     main="Simulation of 100,000 whole tree evolutions under the Stochastic-Dollo model", xlab="Number of birthed traits", xlim=range(0:20))
lines(density(sdtree$V1, adjust=10), col="blue", lwd=2)
legend('topright',c("Poisson Distribution","SD Algorithm"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))
par(mfrow=c(1, 1))

trans.mat <- matrix(c(
  0,1,0,0,
  0.5,0,0,0.5*0.5,
  0,0,0,1,
  0,0.5*0.5,0.5,0
)
, 4,4, byrow = TRUE)

# GTR Whole Tree Borrowing Validation
plot(density(rbinom(200000,20,0.4814815), adjust=10), col="green",lwd=2, 
     main="Simulation of 100,000 language evolutions under the GTR model", xlab="Number of cogantes", xlim=range(0:20))
lines(density(gtrborrowtree$V1, adjust=10), col="blue", lwd=2)
legend('topright',c("Binomial Distribution","Algorithm 1"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))