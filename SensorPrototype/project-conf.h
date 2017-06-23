/* IP buffer size must match all other hops, in particular the border router. */

/*
#undef UIP_CONF_IPV6_RPL
#define UIP_CONF_IPV6_RPL	0

#undef UIP_CONF_ND6_SEND_NA 
#define UIP_CONF_ND6_SEND_NA	1
§*/
#define TIME_SAMPLING 0.7*CLOCK_SECOND

#undef UIP_CONF_BUFFER_SIZE
#define UIP_CONF_BUFFER_SIZE           380
 
#undef NETSTACK_CONF_RDC
#define NETSTACK_CONF_RDC              nullrdc_driver

#undef RPL_CONF_DIO_REDUNDANCY
#define RPL_CONF_DIO_REDUNDANCY	3

/* Disabling TCP on CoAP nodes. */
#undef UIP_CONF_TCP
#define UIP_CONF_TCP                   0

#undef NETSTACK_CONF_MAC
#define NETSTACK_CONF_MAC    			nullmac_driver

/* Increase rpl-border-router IP-buffer when using more than 64. */
#undef REST_MAX_CHUNK_SIZE
#define REST_MAX_CHUNK_SIZE        		80

/* Multiplies with chunk size, be aware of memory constraints. */
#undef COAP_MAX_OPEN_TRANSACTIONS
#define COAP_MAX_OPEN_TRANSACTIONS     4