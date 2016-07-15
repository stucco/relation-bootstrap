
The collector responsible for collecting more documents from various websites and RSS feeds fails to collect any documents when it is fed a large, manually constructed list of sites to work from.  Here are instructions for how to get around that problem:

	1. Place the contents of this directory (collectorfiles) in /stucco/collectors/config/collectorfiles/ .
	2. Give yourself write permission on all files in /stucco/collectors/config/collectorfiles/ as well as the file /stucco/collectors/config/collectors.yml .  Performing the command “sudo chmod a+rwx *” in both the directories containing these files would be overkill, but would work.
	3. In order to make the next step work, you may need to make the sudo command last forever.  To do this, follow the instructions at: http://lifehacker.com/make-sudo-sessions-last-longer-in-linux-1221545774
	4. In the /stucco/collectors/config/collectorfiles directory, run “sudo ./IterativeCollectorResetter.sh &”.

The shell script iteratively shuts down the collector, replaces the collectors.yml file with a new collectors.yml file, then restarts the collector. 
