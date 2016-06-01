# Data input
gtr <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr.csv", quote="\"")
gtr_2 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr_2.csv", quote="\"")
sd <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sd.csv", quote="\"")

gtrtree <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtrtree.csv", quote="\"")
sdtree <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sdtree.csv", quote="\"")

gtr00 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr00.csv", quote="\"")
gtr01 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr01.csv", quote="\"")
gtr10 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr10.csv", quote="\"")
gtr11 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr11.csv", quote="\"")

gtr000 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr000.csv", quote="\"")
gtr100 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr100.csv", quote="\"")
gtr010 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr010.csv", quote="\"")
gtr001 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr001.csv", quote="\"")
gtr110 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr110.csv", quote="\"")
gtr101 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr101.csv", quote="\"")
gtr011 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr011.csv", quote="\"")
gtr111 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/gtr111.csv", quote="\"")

traitLabOut <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/traitLabOut.csv", quote="\"")
sdtreeborrowing <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/sdtreeborrowing.csv", quote="\"")

speed_gtr_a1 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_gtr_a1.csv", quote="\"")
speed_gtr_a2 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_gtr_a2.csv", quote="\"")
speed_sd_a1 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_sd_a1.csv", quote="\"")
speed_sd_a2 <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/speed_sd_a2.csv", quote="\"")

missing_lang <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/missing_lang.csv", quote="\"")
missing_mc <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/missing_mc.csv", quote="\"")


heights_GTR <- read.csv("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/heights_GTR.csv", stringsAsFactors=FALSE)
quartet_GTR <- read.csv("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/quartet_GTR.csv", stringsAsFactors=FALSE)

quartet_SD <- read.csv("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/quartet_SD.csv", stringsAsFactors=FALSE)
heights_SD <- read.csv("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/heights_SD.csv", stringsAsFactors=FALSE)

quartet_COV <- read.csv("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/quartet_COV.csv", stringsAsFactors=FALSE)
heights_COV <- read.csv("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/heights_COV.csv", stringsAsFactors=FALSE)

randHeight <- read.table("C:/Users/Stuart/workspace/Beast2BorrowingSequenceSimulator/Utilities/Thesis Graph Generation/randHeight.csv", quote="\"", stringsAsFactors=FALSE)

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
par(mfrow=c(1, 2))
plot(density(rbinom(80000,20,0.5), adjust=10), col="green",lwd=2, 
     main=NA, xlab="Number of cogantes", xlim=range(0:20))
lines(density(gtrtree$V1, adjust=10), col="blue", lwd=2)
plot(density(rpois(80000,(0.5/0.5)), adjust=10), col="green",lwd=2, 
     main=NA, xlab="Number of birthed traits", xlim=range(0:20))
lines(density(sdtree$V1, adjust=10), col="blue", lwd=2)
title("Simulation of 100,000 whole tree evolutions under the GTR model", outer=TRUE,line = -2)
par(mfrow=c(1, 1))

# GTR Whole Tree Borrowing Validation (2 Languages)
p = c(0.2222,0.2222,0.2222,0.3333)
cognates = 20
plot(density(rmultinom(400000,cognates,p), adjust=10), col="green",lwd=2, 
     main="Simulation of 100,000 language evolutions under the GTR borrowing model", xlab="Number of cogantes", xlim=range(0:cognates))
p = c(mean(gtr00$V1)/cognates,mean(gtr01$V1)/cognates,mean(gtr10$V1)/cognates,median(gtr11$V1)/cognates)
lines(density(rmultinom(400000,cognates,p), adjust=10), col="blue", lwd=2)
legend('topright',c("Multinomial Distribution","GTR Borrowing Algorithm (2 Languages)"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))

# GTR Whole Tree Borrowing Validation (3 Languages)
p = c(0.0976,0.0976,0.0976,0.0976,0.1220,0.1220,0.1220,0.2439)
cognates = 20
plot(density(rmultinom(800000,cognates,p), adjust=10), col="green",lwd=2, 
     main="Simulation of 100,000 language evolutions under the GTR borrowing model", xlab="Number of cogantes", xlim=range(0:cognates))
p = c(mean(gtr000$V1)/cognates,mean(gtr100$V1)/cognates,mean(gtr010$V1)/cognates,mean(gtr001$V1)/cognates,mean(gtr110$V1)/cognates,mean(gtr101$V1)/cognates,mean(gtr011$V1)/cognates,mean(gtr111$V1)/cognates)
lines(density(rmultinom(800000,cognates,p), adjust=10), col="blue", lwd=2)
legend('topright',c("Multinomial Distribution","GTR Borrowing Algorithm (3 Languages)"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))

# SD Whole Tree Borrowing Validation 
plot(density(traitLabOut$V1, adjust=10), col="green",lwd=2, 
     main="Simulation of 1000 language evolutions TraitLab and Algorithm 8", xlab="Number of birthed traits", xlim=range(0:20))
lines(density(sdtreeborrowing$V1, adjust=10), col="blue", lwd=2)
legend('topright',c("TraitLab","Algorithm 8"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))

# GTR/SD Simple Tree versus Borrowing Tree set to 0.0 algorithms.
par(mfrow=c(2, 1))
plot(density(speed_gtr_a1$V1, adjust=10), col="green",lwd=2, 
     main="Simulation of 10,000 whole tree evolutions under the GTR model", xlab="Speed (ms)", xlim=range(0:20))
lines(density(speed_gtr_a2$V1, adjust=10), col="blue", lwd=2)
legend('topright',c("Simple Tree","Borrowing set to 0.0"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))
plot(density(speed_sd_a1$V1, adjust=10), col="green",lwd=2, 
     main="Simulation of 10,000 whole tree evolutions under the Stochastic-Dollo model", xlab="Speed (ms)", xlim=range(0:20))
lines(density(speed_sd_a2$V1, adjust=10), col="blue", lwd=2)
legend('topright',c("Simple Tree","Borrowing set to 0.0"), lty=c(1,1), lwd=c(2,2),col=c("green","blue"))
par(mfrow=c(1, 1))

# Missing Language Validation.
par(mfrow=c(1, 2))
plot(density(rbinom(100000,100,0.5), adjust=10), col="green",lwd=2, 
     main=NA, xlab="Number of missing languages", xlim=range(0:100))
lines(density(missing_lang$V1, adjust=10), col="blue", lwd=2)
plot(density(rbinom(100000,100,0.5), adjust=10), col="green",lwd=2, 
     main=NA, xlab="Number of missing meaning classes", xlim=range(0:100))
lines(density(missing_mc$V1, adjust=10), col="blue", lwd=2)
title("Simulation of 100,000 missing language and missing meaning class models", outer=TRUE,line = -2)
par(mfrow=c(1, 1))

# GTR Quartet
gtr_quart <- data.matrix(gtr_quart)
plot(gtr_quart[1:9,], xaxt="n", main="Quartet Distances under various levels of GTR borrowing", xlab="Borrowing (%)", ylab="Quartet Distance")
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))
# GTR Height
gtr_heights <- data.matrix(gtr_heights)
diff_fun <- function(x) abs(7080.7-x)
diffs <- sapply(gtr_heights[,2], diff_fun)
plot(diffs, type="l", xaxt="n", main="Differences in tree height under various levels of GTR borrowing", xlab="Borrowing (%)", ylab="Height Difference (years)")
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))

# Height and Quartet Differences
quartet_GTR <- data.matrix(quartet_GTR)
heights_GTR <- data.matrix(heights_GTR)
boxplot(quartet_GTR[,1:9], main="Quartet Distance under various levels of GTR borrowing", ylab="Quartet Distance", xlab="Borrowing Rate (%)", xaxt="n", outline=FALSE)
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))
boxplot(heights_GTR[,1:9], main="Height differentials under various levels of GTR borrowing", ylab="Difference (%)", xlab="Borrowing Rate (%)", xaxt="n", outline=FALSE)
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))

quartet_SD <- data.matrix(quartet_SD)
heights_SD <- data.matrix(heights_SD)
boxplot(quartet_SD[,1:9], main="Quartet Distance under various levels of SD borrowing (GTR Inference)", ylab="Quartet Distance", xlab="Borrowing Rate (%)", xaxt="n", outline=FALSE)
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))
boxplot(heights_SD[,1:9], main="Height differentials under various levels of SD borrowing (GTR Inference)", ylab="Difference (%)", xlab="Borrowing Rate (%)", xaxt="n", outline=FALSE)
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))

quartet_COV <- data.matrix(quartet_COV)
heights_COV <- data.matrix(heights_COV)
boxplot(quartet_COV, main="Quartet Distance under various levels of SD borrowing (Covarion Inference)", ylab="Quartet Distance", xlab="Borrowing Rate (%)", xaxt="n", outline=FALSE)
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))
boxplot(heights_COV, main="Height differentials under various levels of SD borrowing (Covarion Inference)", ylab="Difference (%)", xlab="Borrowing Rate (%)", xaxt="n", outline=FALSE)
axis(1, at=1:9, labels=c(0,1,5,10,15,20,30,40,50))