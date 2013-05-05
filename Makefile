default:
	javac ch/blinkenlights/bastp/*.java

clean:
	rm ch/blinkenlights/bastp/*.class

test:
	@echo -n "== size of test directory 't' :  "
	@du -hs t
	@echo -n "== started at: "
	@date
	find t -type f| java ch.blinkenlights.bastp.Test
	@echo -n "== ended at: "
	@date

xtest:
	find /scratch/music/ -type f |grep -i mp3| java ch.blinkenlights.bastp.Test
