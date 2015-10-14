# Data input

gtr <- read.table("~/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/gtr.csv", quote="\"", comment.char="")
gtr_2 <- read.table("~/Code/Beast2-plugin/Beast-Borrowing-Plugin/Utilities/Thesis Graph Generation/gtr_2.csv", quote="\"", comment.char="")

# GTR Lineage Validation
plot(density(rbinom(100000,20,0.5), adjust=10), col="green", lty=1, lwd=1, 
     main="Simulation of 100,000 language evolutions under the GTR model", xlab="Number of cogantes", xlim=range(0:20))
lines(density(gtr$V1, adjust=10), col="blue", lty=2,lwd=2)
lines(density(gtr_2$V1, adjust=10), col="red", lty=2,lwd=2)
legend('topright',c("Binomial Distribution","Algorithm 1", "Algorithm 2"), lty=c(1,1,1), lwd=c(1,1,1),col=c("green","blue","red"))
