#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"

int oxy_perc = 98;

void oxy_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void oxy_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void oxy_periodic_handler();

PERIODIC_RESOURCE(oxy_sens, "title=\"OS\"; rt=\"p\";i=\"1\"", oxy_get_handler, oxy_post_handler, NULL, NULL, 5*CLOCK_SECOND, oxy_periodic_handler);

void oxy_get_handler(void* request, void* response, uint8_t* buffer, uint16_t preferred_size, int32_t* offset)
{
	/* Populat the buffer with the response payload*/
	char message[20];
	int length = 20;

	sprintf(message, "VALUE:%03u", oxy_perc);
	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

void oxy_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;
     
  len=REST.get_post_variable(request, "value", &val);
     
  if( len > 0 ){
     new_value = atoi(val);	
     oxy_perc = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void oxy_periodic_handler()
{
	--oxy_perc;

	REST.notify_subscribers(&oxy_sens);
}

PROCESS(oxygen_process, "Oxygen Process");
AUTOSTART_PROCESSES(&oxygen_process);

PROCESS_THREAD(oxygen_process, ev, data)
{
	PROCESS_BEGIN();

	/* Initialize Rest engine */
	rest_init_engine();

	/* Activate the application-specific resources */
	rest_activate_resource(&oxy_sens, "oxygen");

	while(1) {
		PROCESS_WAIT_EVENT();
	}

	PROCESS_END();
}