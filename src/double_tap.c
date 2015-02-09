#include <pebble.h>
#include "double_tap.h"
#include "blks.h"
#include "data_framework.h"

#define TAP_TIME 2000
bool is_tapped_waiting;
AppTimer *tap_timer;

void timer_callback() {
	is_tapped_waiting = false;
}

void handle_tap(AccelAxisType axis, int32_t direction) {
	blks_tap();
	double_tap();
}  

void double_tap() {
	//Todo: BTLE support
	APP_LOG(APP_LOG_LEVEL_INFO, "tap!");
	vibes_double_pulse();
	gesture_fired();
}

void init_double_tap() {
	accel_tap_service_subscribe(handle_tap);
}

void deinit_double_tap(){
	accel_tap_service_unsubscribe();
}