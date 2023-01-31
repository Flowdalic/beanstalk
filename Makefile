GRADLE ?= ./gradlew

.PHONY: run
run:
	$(GRADLE) beanstalk-app:run

.PHONY: check
check:
	$(GRADLE) $@

.PHONY: clean
clean:
	$(GRADLE) $@

.PHONY: eclipse
eclipse:
	$(GRADLE) $@

.PHONY: jmh
jmh:
	$(GRADLE) beanstalk-core:jmh
