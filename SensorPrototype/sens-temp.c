#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"


#define TIME_SAMPLING 100
#define STARTING_TEMPERATURE 20
#define A 0.9048
// A = exp(-(TIME_SAMPLING/TIME_CONST)
#define B (1 - A)

// First order variable
static int u_k_1 = (STARTING_TEMPERATURE+1);
static int u_k = (STARTING_TEMPERATURE+1);
static float temp_k = STARTING_TEMPERATURE;	// temperature at current time
static float temp_k_1 = STARTING_TEMPERATURE;	// temperature last sample
 

void temp_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void temp_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temp_periodic_handler();


PERIODIC_RESOURCE(temp_sens,"title=\"Temperature Sensor\"; rt=\"Temperature\"", temp_get_handler,temp_post_handler,NULL,NULL,
	TIME_SAMPLING,temp_periodic_handler);

void temp_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[20];
	int length = 20;

	sprintf(message, "TEMPERATURE:%03d", ((int) temp_k));
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
     u_k = new_value + 1; 
     /* 
     * because it doesn't reach the final value. It's done only for the coherence of the
     * simulation
     */
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void temp_periodic_handler()
{
	temp_k_1 = temp_k;
	temp_k = (A * temp_k_1) + (B * u_k_1);
	if (((int) temp_k) != ((int) temp_k_1))
		REST.notify_subscribers(&temp_sens);
	u_k_1 = u_k;
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