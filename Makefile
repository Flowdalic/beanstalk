GRADLE ?= ./gradlew

.PHONY: run
run:
	$(GRADLE) beanstalk-app:run

.PHONY: check
check:
	$(GRADLE) check
