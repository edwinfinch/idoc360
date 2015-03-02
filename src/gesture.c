#include <pebble.h>
#include "gesture.h"
#include "data_framework.h"
bool releaseTimerEnded = true;
AppTimer *releaseTimer;

bool isLow(int16_t i){
	if(i < 0){
		i = i*(-1);
	}
	if(i > 600 && i < 900){
		return true;
	}
	return false;
}

bool isHigh(int16_t i){
	if(i < 0){
		i = i*(-1);
	}
	if(i > 1900){
		return true;
	}
	return false;
}

bool find_high(int16_t data[20]){
	int16_t i;
	for(i = 0; i != 19; i++){
		if(isHigh(data[i])){
			return true;
		}
	}
	return false;
}

bool find_low(int16_t data[20]){
	int16_t i;
	for(i = 0; i != 19; i++){
		if(isLow(data[i])){
			return true;
		}
	}
	return false;
}

void release_timer_callback(){
	releaseTimerEnded = true;
}

void accel_handler(AccelData *data, uint32_t samples){
	int16_t x_data[20];
	int i;
	for(i = 0; i < (int)samples; i++){
		x_data[i] = data[i].x;
	}
	if(find_high(x_data) && find_low(x_data) && releaseTimerEnded){
		releaseTimerEnded = false;
		vibes_double_pulse();
		releaseTimer = app_timer_register(1500, release_timer_callback, NULL);
		APP_LOG(APP_LOG_LEVEL_INFO, "Hard wave detected");
		gesture_fired();
	}
}

void start_gesture_service() {
	accel_data_service_subscribe(20, accel_handler);
}

void end_gesture_service(){
	accel_data_service_unsubscribe();
}