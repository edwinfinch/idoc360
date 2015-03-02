#include <pebble.h>
#include "data_framework.h"

bool loggingData = false, currentStatus = false;
TextLayer *logging_layer;

void process_tuple(Tuple *t){
	int key = t->key;
	int value = t->value->int32;
	//if(settings.debug){APP_LOG(APP_LOG_LEVEL_INFO, "key: %d, data %d", key, value);}
	switch(key){
		case 0:
			vibes_long_pulse();
			break;
		case 1:
			break;
	}
	if(isLogging()){
		text_layer_set_text(logging_layer, "Processing data");
	}
}

void inbox(DictionaryIterator *iter, void *context){
	if(isLogging()){
		text_layer_set_text(logging_layer, "Got data!");
	}
	Tuple *t = dict_read_first(iter);
	if(t){
		process_tuple(t);
	}
	while(t != NULL){
		t = dict_read_next(iter);
		if(t){
			process_tuple(t);
		}
	}
	if(isLogging()){
		text_layer_set_text(logging_layer, "Dictionary has been read and all data successfully copied.");
	}
}

void send_light_status(int on){
	DictionaryIterator *iter;
	app_message_outbox_begin(&iter);

	if (iter == NULL) {
		return;
	}
	dict_write_uint8(iter, 0, on);	
	dict_write_end(iter); 
	app_message_outbox_send();
	if(isLogging()){
		text_layer_set_text(logging_layer, "Gesture detected. Sending data to phone app for light control.");
	}
}

void gesture_fired(){
	send_light_status((int)currentStatus);
	currentStatus = !currentStatus;
}

void send_data(uint8_t data){
	DictionaryIterator *iter;
	app_message_outbox_begin(&iter);

	if (iter == NULL) {
		return;
	}
	dict_write_uint8(iter, 0, data);	
	dict_write_end(iter); 
	app_message_outbox_send();
	if(isLogging()){
		//text_layer_set_text(logging_layer, "Gesture detected. Sending random data to phone app for light control.");
	}
}

void logging(bool isLogging, TextLayer *layer){
	loggingData = isLogging;
	logging_layer = layer;
}

bool isLogging(){
	return loggingData;
}