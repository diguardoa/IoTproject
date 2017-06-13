#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"
#include "net/rpl/rpl.h"

#define TIME_SAMPLING 100
#define STARTING_TEMP 100
static int current_temp = STARTING_TEMP;
static int next_temp = STARTING_TEMP;
int room_id = 0;

void id_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void id_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static void air_periodic_handler();

PERIODIC_RESOURCE(airConditioner, "title=\"AirCon\";rt=\"A\";obs", get_handler, post_handler, NULL, NULL, TIME_SAMPLING, air_periodic_handler);
RESOURCE(Id, "title=\"RoomId\"", id_get_handler, id_post_handler, NULL, NULL);

void id_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[30];
	int length = 30;

	sprintf(message, "{'type':'room', 'id':'%d'}", room_id);
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
     room_id = new_id;
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

	sprintf(message,"{'e':'%03d','u':'C'}",current_temp);

	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{

  int len, temp_temp;
  const char *val = NULL;
     
  len=REST.get_post_variable(request, "e", &val);
     
  if( len > 0 ){
    temp_temp = atoi(val);	
 	next_temp = temp_temp;
 	REST.set_response_status(response, REST.status.CREATED);
    

     
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void air_periodic_handler()
{
	if (next_temp != current_temp)
	{
		current_temp = next_temp;
		REST.notify_subscribers(&airConditioner);
	}
}

PROCESS(airConditioner_main, "Air Conditioner Main");

AUTOSTART_PROCESSES(&airConditioner_main);

PROCESS_THREAD(airConditioner_main, ev, data){
	PROCESS_BEGIN();

	rest_init_engine();

	rest_activate_resource(&airConditioner, "AirConditioner");
	rest_activate_resource(&Id, "id");


	while(1) 
	{
   		PROCESS_WAIT_EVENT();
	}

	PROCESS_END();
}