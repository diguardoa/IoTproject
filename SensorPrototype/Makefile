CONTIKI_PROJECT = sens-temp sens-heartrate sens-oxygen

SMALL=1

UIP_CONF_IPV6=1

CFLAGS+= -DRPL_CONF_DIO_REDUNDANCY=1

CONTIKI=/home/user/contiki
CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"
CFLAGS += -DUIP_CONF_IPV6_RPL

APPS += er-coap
APPS += rest-engine
APPS += servreg-hack

CONTIKI_WITH_IPV6 = 1
include $(CONTIKI)/Makefile.include
