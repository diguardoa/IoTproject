#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"
#include "net/rpl/rpl.h"

#define STARTING_HR 100

int pat_id = 0;

int hr_current = STARTING_HR;
int hr_next = STARTING_HR;

void id_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void id_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void hr_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void hr_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static void hr_periodic_handler();


PERIODIC_RESOURCE(hr_sens,"title=\"HRS\";rt\"S\";obs", hr_get_handler,hr_post_handler,NULL,NULL,10*CLOCK_SECOND,hr_periodic_handler);

//RESOURCE(set_temp_environment, "title=\"Set_HRS\";rt=\"P\"", NULL, hr_post_handler, NULL, NULL);

RESOURCE(Id, "title=\"PatienId\";rt=\"Id\"", id_get_handler, id_post_handler, NULL, NULL);

void id_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[30];
	int length = 30;

	sprintf(message, "{'type':'pat', 'id':'%d'}", pat_id);
	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

void id_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_id, len;
  const char *val = NULL;
     
  len=REST.get_post_variable(request, "e", &val);
     
  if( len > 0 ){
     new_id = atoi(val);	
     pat_id = new_id;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

void hr_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[26];
	int length = 26;

	sprintf(message, "{'e':'%03u','u':'hr/m'}", hr_current);
	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}


void hr_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;
     
  len=REST.get_post_variable(request, "e", &val);
     
  if( len > 0 ){
     new_value = atoi(val);	
     hr_next = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void hr_periodic_handler()
{
	if (hr_next != hr_current)
	{
		hr_current = hr_next;
		REST.notify_subscribers(&hr_sens);
	}
}

PROCESS(heartRate_process, "HeartRate sensor");
AUTOSTART_PROCESSES(&heartRate_process);

PROCESS_THREAD(heartRate_process, ev, data)
{
	PROCESS_BEGIN();

	/* Initialize Rest engine */
	rest_init_engine();

	/* Activate the application-specific resources */
	rest_activate_resource(&hr_sens, "heart_rate");
	rest_activate_resource(&Id, "id");

	while(1) {
		PROCESS_WAIT_EVENT();
	}

	PROCESS_END();
}