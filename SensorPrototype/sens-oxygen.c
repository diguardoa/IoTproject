#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"
#include "net/rpl/rpl.h"

#define TIME_SAMPLING 100
#define STARTING_OXY 200

// First order variable
static int u_k_1 = STARTING_OXY;
static int u_k = STARTING_OXY;
static int oxy_k = STARTING_OXY;	// temperature at current time
static int oxy_k_1 = STARTING_OXY;	// temperature last sample

int pat_id = 0;

void id_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void id_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void oxy_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void oxy_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static void oxy_periodic_handler();

PERIODIC_RESOURCE(oxy_sens, "title=\"OxyS\";rt=\"S\";obs", oxy_get_handler, oxy_post_handler, NULL, NULL, TIME_SAMPLING, oxy_periodic_handler);

//RESOURCE(set_temp_environment, "title=\"Set_OxyS\";rt=\"P\"", NULL, oxy_post_handler, NULL, NULL);

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


void oxy_get_handler(void* request, void* response, uint8_t* buffer, uint16_t preferred_size, int32_t* offset)
{
	/* Populat the buffer with the response payload*/
	char message[23];
	int length = 23;
	int t_oxy = (int) oxy_k;

	sprintf(message, "{'e':'%03d','u':'%'}", t_oxy);
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
     
  len=REST.get_post_variable(request, "e", &val);
     
  if( len > 0 ){
     new_value = atoi(val);	
     u_k = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void oxy_periodic_handler()
{
	oxy_k_1 = oxy_k;
	oxy_k = (9 * oxy_k_1) + (u_k_1); // A = 0.9, B = 0.1
	oxy_k = oxy_k / 10;
	if ((oxy_k) != (oxy_k_1))
		REST.notify_subscribers(&oxy_sens);
	u_k_1 = u_k;
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
	rest_activate_resource(&Id, "id");

	while(1) {
		PROCESS_WAIT_EVENT();
	}

	PROCESS_END();
}