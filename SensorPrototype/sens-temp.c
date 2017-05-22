#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"

int value = 0;
//int end_value = 30;

void temp_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void temp_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temp_periodic_handler();


PERIODIC_RESOURCE(temp_sens,"title=\"Temperature Sensor\"; rt=\"Temperature\"", temp_get_handler,temp_post_handler,NULL,NULL,10*CLOCK_SECOND
	,temp_periodic_handler);

void temp_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[20];
	int length = 20;

	sprintf(message, "VALUE:%03u", value);
	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

void temp_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;
     
  len=REST.get_post_variable(request, "value", &val);
     
  if( len > 0 ){
     new_value = atoi(val);	
     value = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void temp_periodic_handler()
{
	value++;

	REST.notify_subscribers(&temp_sens);
}

PROCESS(temperature_process, "Temperature sensor");

AUTOSTART_PROCESSES(&temperature_process);

PROCESS_THREAD(temperature_process, ev, data)
{
	PROCESS_BEGIN();

	/* Initialize Rest engine */
	rest_init_engine();

	/* Activate the application-specific resources */
	rest_activate_resource(&temp_sens, "temperature");

	while(1) {
		PROCESS_WAIT_EVENT();
	}

	PROCESS_END();
}