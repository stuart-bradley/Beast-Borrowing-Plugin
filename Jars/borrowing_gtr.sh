#!/bin/sh
#SBATCH -J BorrowingGTR	# The job name
#SBATCH -A nesi00174		# The account code
#SBATCH --time=01:00:00        # The walltime
#SBATCH --mem-per-cpu=8096 	# Memory in MB ie. ? * 1024
#SBATCH -o gtr_out%a.txt		# The output file
#SBATCH -e gtr_err%a.txt		# The error file

module load Java/1.8.0_40 
srun java -Xmx7168m -jar BorrowingComparisons.jar "BorrowingComparisons/GTR_0.xml" "GTR" $SLURM_ARRAY_TASK_ID