

while true
do
	for file in /stucco/collectors/config/collectorfiles/collectors.yml.*
	do
		supervisorctl stop stucco-doc
		supervisorctl stop stucco-scheduler
	
		cp $file /stucco/collectors/config/collectors.yml

		sleep 10s

		supervisorctl start stucco-doc
		supervisorctl start stucco-scheduler

		sleep 3m
	done
done

