#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"
#include "dev/leds.h"
#include "sys/etimer.h"
#include "net/rpl/rpl.h"

#define ALARM_ON 1
#define ALARM_OFF 0

#define TIME_SAMPLING 100


static int alarm_status = ALARM_OFF;
static int next_status = ALARM_OFF;
static int cycles_without_notify = 0;

static struct etimer led_timer;
int pat_id = 0;

void id_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void id_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static void la_periodic_handler();


PERIODIC_RESOURCE(ledAlarm, "title=\"LedA\";rt=\"A\";obs", get_handler, post_handler, NULL, NULL, TIME_SAMPLING, la_periodic_handler);
RESOURCE(Id, "title=\"PatienId\"rt=\"Id\"", id_get_handler, id_post_handler, NULL, NULL);

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

void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

	/* Populat the buffer with the response payload*/
	char message[26];
	int length = 26;

	if (alarm_status == ALARM_ON)
		sprintf(message, "{'e':'1','u':'status'}");
	else
		sprintf(message, "{'e':'0','u':'status'}");

	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){


  int len;
  const char *val = NULL;
     
  len=REST.get_post_variable(request, "e", &val);
     
  if( len > 0 ){
     next_status = atoi(val);	
     

	if (next_status == ALARM_ON) 
	{
		if(alarm_status == ALARM_OFF) 
			leds_on(LEDS_ALL);
	}
	else
	{
		leds_off(LEDS_ALL);
	}


     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

static void la_periodic_handler()
{
	if (next_status != alarm_status)
	{
		alarm_status = next_status;
		REST.notify_subscribers(&ledAlarm);
	} else 
			cycles_without_notify++;
	
	if (cycles_without_notify == 20)
	{
		cycles_without_notify = 0;
		REST.notify_subscribers(&ledAlarm);
	}
}

PROCESS(ledAlarm_main, "LedAlarm");

AUTOSTART_PROCESSES(&ledAlarm_main);

PROCESS_THREAD(ledAlarm_main, ev, data){
	PROCESS_BEGIN();

	rest_init_engine();

	rest_activate_resource(&ledAlarm, "LedAlarm");
	rest_activate_resource(&Id, "id");

	etimer_set(&led_timer, 2*CLOCK_SECOND);


	while(1) {
   		PROCESS_WAIT_EVENT();
   		if (etimer_expired(&led_timer))
   		{
   			if (alarm_status == ALARM_ON)
	   		{
		   		leds_toggle(LEDS_ALL);	   		 
			}
			etimer_reset(&led_timer);// From the previous expiration time
		}
	}
	PROCESS_END();
}
