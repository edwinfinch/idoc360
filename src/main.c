#include <pebble.h>
#include "blks.h"
#include "main.h"
#include "double_tap.h"
#include "extras.h"

void select(ClickRecognizerRef click, void *context){
	window_stack_push(menu_window, true);
}

void process_tuple(Tuple *t){
	int key = t->key;
	int value = t->value->int32;
	//if(settings.debug){APP_LOG(APP_LOG_LEVEL_INFO, "key: %d, data %d", key, value);}
	switch(key){
		case 0:
			switch(value){

			}
			break;
	}
}

void inbox(DictionaryIterator *iter, void *context){
	Tuple *t = dict_read_first(iter);
	if(t)
	{
		process_tuple(t);
	}
	while(t != NULL)
	{
		t = dict_read_next(iter);
		if(t)
		{
			process_tuple(t);
		}
	}
}

void override_gesture(ClickRecognizerRef click, void *context){
	if(settings.override){
		switch(settings.gesture){
			case 0:
				double_tap();
				break;
			case 1:
				break;
		}
	}
}

void click_prov(void *context){
	window_single_click_subscribe(BUTTON_ID_SELECT, select);
	window_single_click_subscribe(BUTTON_ID_BACK, NULL);
	window_long_click_subscribe(BUTTON_ID_UP, 2000, override_gesture, NULL);
}

void redraw_menu(int menu){
	switch(menu){
		case 0:
			layer_mark_dirty(simple_menu_layer_get_layer(main_menu));
			break;
		case 1:
			layer_mark_dirty(simple_menu_layer_get_layer(settings_menu));
			break;
	}
}

void callback(int index, void *ctx){
	
}

void override_callback(int index, void *ctx){
	settings.override = !settings.override;
	if(settings.override){
		s_menu_settings_items[1].subtitle = "Long press up";
	}
	else{
		s_menu_settings_items[1].subtitle = "Disabled.";
	}
	redraw_menu(1);
}

void bt_callback(int index, void *ctx){
	settings.btalerts = !settings.btalerts;
	if(settings.btalerts){
		s_menu_settings_items[0].subtitle = "Enabled.";
	}
	else{
		s_menu_settings_items[0].subtitle = "Disabled.";
	}
	redraw_menu(1);
}

void settings_callback(int index, void *ctx){
	window_stack_push(settings_window, true);
}

void aboot_callback(int index, void *ctx){
	window_stack_push(aboot_window, true);
}

void window_load_settings_menu(Window *w){
	Layer *window_layer = window_get_root_layer(settings_window);
	GRect bounds = layer_get_frame(window_layer);
	
	s_menu_settings_items[0] = (SimpleMenuItem){
		.title = "Bluetooth Alerts",
		.callback = bt_callback,
	};
	s_menu_settings_items[1] = (SimpleMenuItem){
		.title = "Override Gesture",
		.callback = override_callback,
	};
	
	s_menu_sections[0] = (SimpleMenuSection){
		.num_items = 2,
		.items = s_menu_settings_items,
	};

	if(settings.override){
		s_menu_settings_items[1].subtitle = "Long press up";
	}
	else{
		s_menu_settings_items[1].subtitle = "Disabled.";
	}
	if(settings.btalerts){
		s_menu_settings_items[0].subtitle = "Enabled.";
	}
	else{
		s_menu_settings_items[0].subtitle = "Disabled.";
	}
	
	settings_menu = simple_menu_layer_create(bounds, settings_window, s_menu_sections, 1, NULL);
	layer_add_child(window_layer, simple_menu_layer_get_layer(settings_menu));
}

void window_unload_settings_menu(Window *w){
	simple_menu_layer_destroy(settings_menu);
}

void window_load_menu(Window *w){
	Layer *window_layer = window_get_root_layer(menu_window);
	GRect bounds = layer_get_frame(window_layer);
	
	menu_settings_items[0] = (SimpleMenuItem){
		.title = "Watchface",
		.callback = callback,
		.icon = wf_icon
	};
	menu_settings_items[1] = (SimpleMenuItem){
		.title = "Gesture",
		.callback = callback,
		.icon = gesture_icon
	};
	menu_settings_items[2] = (SimpleMenuItem){
		.title = "Debug Log",
		.callback = callback,
		.icon = log_icon
	};
	menu_settings_items[3] = (SimpleMenuItem){
		.title = "About",
		.callback = aboot_callback,
		.icon = aboot_icon
	};
	menu_settings_items[4] = (SimpleMenuItem){
		.title = "Settings",
		.callback = settings_callback,
		.icon = settings_icon,
	};
	
	menu_sections[0] = (SimpleMenuSection){
		.num_items = 5,
		.items = menu_settings_items,
	};
	
	main_menu = simple_menu_layer_create(bounds, menu_window, menu_sections, 1, NULL);
	layer_add_child(window_layer, simple_menu_layer_get_layer(main_menu));
}

void window_unload_menu(Window *w){
	simple_menu_layer_destroy(main_menu);
}

void window_load_debug(Window *w){

}

void window_unload_debug(Window *w){

}

void window_load_aboot(Window *w){
	Layer *window_layer = window_get_root_layer(w);
	aboot_edwin = text_layer_init(GRect(0, 10, 144, 168), GColorClear, GTextAlignmentCenter, 1);
	aboot_version = text_layer_init(GRect(0, 110, 144, 168), GColorClear, GTextAlignmentCenter, 2);
	aboot_theme = inverter_layer_create(GRect(0, 0, 144, 168));
	
	text_layer_set_text(aboot_edwin, "Created by Edwin Finch");
	text_layer_set_text(aboot_version, "v. 0.5 alpha");
	
	layer_add_child(window_layer, text_layer_get_layer(aboot_edwin));
	layer_add_child(window_layer, text_layer_get_layer(aboot_version));
	layer_add_child(window_layer, inverter_layer_get_layer(aboot_theme));
}

void window_unload_aboot(Window *m8){
	text_layer_destroy(aboot_edwin);
	text_layer_destroy(aboot_version);
	inverter_layer_destroy(aboot_theme);
}

void init_gesture(){
	accel_tap_service_unsubscribe();
	switch(settings.gesture){
		case 0:
			init_double_tap();
			break;
		case 1:
			//Add more gestures...
			break;
	}
}

void init(){
	blks_window = window_create();
	window_set_fullscreen(blks_window, true);
	load_blks(blks_window);

	window_set_click_config_provider(blks_window, click_prov);

	int result = persist_read_data(0, &settings, sizeof(settings));
	APP_LOG(APP_LOG_LEVEL_INFO, "iDoc360: Read %d bytes from settings.", result);

	init_gesture();

	window_stack_push(blks_window, true);

	app_message_register_inbox_received(inbox);
	app_message_open(512, 512);

	wf_icon = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_WATCHFACE_ICON);
	settings_icon = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_SETTINGS_ICON);
	log_icon = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_LOG_ICON);
	aboot_icon = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_ABOOT_ICON);
	gesture_icon = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_GESTURE_ICON);

	//set up other windows and whatnot
	menu_window = window_create();
	window_set_window_handlers(menu_window, (WindowHandlers){
		.load = window_load_menu,
		.unload = window_unload_menu
	});
	settings_window = window_create();
	window_set_window_handlers(settings_window, (WindowHandlers){
		.load = window_load_settings_menu,
		.unload = window_unload_settings_menu
	});
	debug_window = window_create();
	window_set_window_handlers(debug_window, (WindowHandlers){
		.load = window_load_debug,
		.unload = window_unload_debug
	});
	aboot_window = window_create();
	window_set_window_handlers(aboot_window, (WindowHandlers){
		.load = window_load_aboot,
		.unload = window_unload_aboot
	});
}

void deinit(){
	int result = persist_write_data(0, &settings, sizeof(settings));
	APP_LOG(APP_LOG_LEVEL_INFO, "iDoc360: Wrote %d bytes to settings.", result);
	unload_blks();
	window_destroy(blks_window);
	window_destroy(menu_window);
	window_destroy(settings_window);
	window_destroy(debug_window);
	window_destroy(aboot_window);
}

int main(){
	init();
	app_event_loop();
	deinit();
}