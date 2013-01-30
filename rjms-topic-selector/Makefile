# Makefile for rjms-topic-selector

.PHONY: all clean init dist

all: dist

clean:
	rm -rf rabbitmq-jms-topic-exchange*

rabbitmq-jms-topic-exchange.co: | rabbitmq-jms-topic-exchange
	touch $@

rabbitmq-jms-topic-exchange:
	git clone git://git.springsource.com/rabbit-jms/rabbitmq-jms-topic-exchange.git

init: rabbitmq-jms-topic-exchange.co

dist: init
	make -C rabbitmq-jms-topic-exchange dist