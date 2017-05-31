#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"

#define MAX_TEMP 100
#define MIN_TEMP 10

static int current_temp;


void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[8];
	int length = 8;

	sprintf(message,"temp:%03d",current_temp);

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
     
  len=REST.get_post_variable(request, "temp", &val);
     
  if( len > 0 ){
     temp_temp = atoi(val);	
     if ((temp_temp >= MIN_TEMP) && (temp_temp <= MAX_TEMP))
     {
     	current_temp = temp_temp;
     	REST.set_response_status(response, REST.status.CREATED);
     }
     else
     	REST.set_response_status(response, REST.status.BAD_REQUEST);

     
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

RESOURCE(airConditioner, "title=\"AC\"", get_handler, post_handler, NULL, NULL);

PROCESS(airConditioner_main, "Air Conditioner Main");

AUTOSTART_PROCESSES(&airConditioner_main);

PROCESS_THREAD(airConditioner_main, ev, data){
	PROCESS_BEGIN();

	rest_init_engine();

	rest_activate_resource(&airConditioner, "AirConditioner");



	while(1) 
	{
   		PROCESS_WAIT_EVENT();
	}

	PROCESS_END();
}