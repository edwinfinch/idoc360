#include <pebble.h>

TextLayer *text_date_layer;
TextLayer *text_time_layer;
Layer *line_layer;

void line_layer_update_callback(Layer *layer, GContext* ctx) {
	graphics_context_set_fill_color(ctx, GColorWhite);
	graphics_fill_rect(ctx, layer_get_bounds(layer), 0, GCornerNone);
}

void handle_minute_tick(struct tm *tick_time, TimeUnits units_changed) {
	static char time_text[] = "00:00";
	static char date_text[] = "Xxxxxxxxx 00";

	char *time_format;

	if (!tick_time) {
	time_t now = time(NULL);
	tick_time = localtime(&now);
	}

	strftime(date_text, sizeof(date_text), "%B %e", tick_time);
	text_layer_set_text(text_date_layer, date_text);


	if (clock_is_24h_style()) {
		time_format = "%R";
	} 
	else {
		time_format = "%I:%M";
	}

	strftime(time_text, sizeof(time_text), time_format, tick_time);

	if (!clock_is_24h_style() && (time_text[0] == '0')) {
		memmove(time_text, &time_text[1], sizeof(time_text) - 1);
	}

	text_layer_set_text(text_time_layer, time_text);
}

void simplicity_unload(Window *window) {
	text_layer_destroy(text_date_layer);
	text_layer_destroy(text_time_layer);
	layer_destroy(line_layer);
	tick_timer_service_unsubscribe();
}

void simplicity_load(Window *window) {
	window_set_background_color(window, GColorBlack);

	Layer *window_layer = window_get_root_layer(window);

	text_date_layer = text_layer_create(GRect(8, 68, 144-8, 168-68));
	text_layer_set_text_color(text_date_layer, GColorWhite);
	text_layer_set_background_color(text_date_layer, GColorClear);
	text_layer_set_font(text_date_layer, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
	layer_add_child(window_layer, text_layer_get_layer(text_date_layer));

	text_time_layer = text_layer_create(GRect(7, 92, 144-7, 168-92));
	text_layer_set_text_color(text_time_layer, GColorWhite);
	text_layer_set_background_color(text_time_layer, GColorClear);
	text_layer_set_font(text_time_layer, fonts_get_system_font(FONT_KEY_ROBOTO_BOLD_SUBSET_49));
	layer_add_child(window_layer, text_layer_get_layer(text_time_layer));

	GRect line_frame = GRect(8, 97, 139, 2);
	line_layer = layer_create(line_frame);
	layer_set_update_proc(line_layer, line_layer_update_callback);
	layer_add_child(window_layer, line_layer);

	tick_timer_service_subscribe(MINUTE_UNIT, handle_minute_tick);
	handle_minute_tick(NULL, MINUTE_UNIT);
}