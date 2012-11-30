#!/bin/bash

nodes=(100 250 500 1000)
start=$(date)
runs=1
num=0
log=logs/test.log

mkdir logs
echo > ${log}
while [ ${num} -le 3 ]; do
	while [ ${runs} -le 20 ]; do
		startRun=$(date)
		echo "Run #${runs} with ${nodes[$num]} nodes starting: ${startRun}"
		echo "Run #${runs} with ${nodes[$num]} nodes starting: ${startRun}" >> ${log}
		echo "java -cp bin de.tu_darmstadt.kom.mobilitySimulator.Test --numberOfAgents=${nodes[$num]}" >> ${log}
		java -cp bin de.tu_darmstadt.kom.mobilitySimulator.Test --numberOfAgents=${nodes[$num]}
		endRun=$(date)
		echo "Run #${runs} with ${nodes[$num]} nodes finished: ${endRun}"
		echo "Run #${runs} with ${nodes[$num]} nodes finished: ${endRun}" >> ${log}
		echo >> ${log}
		let runs++
	done
	runs=1
	let num++
done
end=$(date)
echo "Test started: ${start}" >> ${log}
echo "Test finished: ${end}" >> ${log}
