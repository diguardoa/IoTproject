#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"
#include "net/rpl/rpl.h"

#define STARTING_OXY 800

static int current_status= STARTING_OXY;
static int next_status= STARTING_OXY;
int pat_id = 0;

void id_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void id_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static void oxy_periodic_handler();

PERIODIC_RESOURCE(oxigenMaskValve, "title=\"OxyValv\";rt=\"A\";obs", get_handler, post_handler, NULL, NULL, TIME_SAMPLING, oxy_periodic_handler);
RESOURCE(Id, "title=\"PatientId\"rt=\"Id\"", id_get_handler, id_post_handler, NULL, NULL);

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

void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[23];
	int length = 23;

	sprintf(message,"{'e':'%d','u':'%'}",current_status);

	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{

  int len, temp_status;
  const char *val = NULL;
     
  len=REST.get_post_variable(request, "e", &val);
     
  if( len > 0 ){
     temp_status = atoi(val);	
     	next_status = temp_status;
     	REST.set_response_status(response, REST.status.CREATED);
    

     
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void oxy_periodic_handler()
{
	if (next_status != current_status)
	{
		current_status = next_status;
		REST.notify_subscribers(&oxigenMaskValve);
	}
}

PROCESS(oxigenMaskValve_main, "OxV Main");

AUTOSTART_PROCESSES(&oxigenMaskValve_main);

PROCESS_THREAD(oxigenMaskValve_main, ev, data){
	PROCESS_BEGIN();

	rest_init_engine();

	rest_activate_resource(&oxigenMaskValve, "OxigenValve");
	rest_activate_resource(&Id, "id");


	while(1) 
	{
   		PROCESS_WAIT_EVENT();
	}

	PROCESS_END();
}